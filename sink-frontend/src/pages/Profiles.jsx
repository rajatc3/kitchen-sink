import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../pages/Header";
import { fetchProfile, updateProfile } from "../api/profile";

const Profile = () => {
   const [formData, setFormData] = useState({
      firstName: "",
      lastName: "",
      username: "",
      email: "",
      phoneNumber: "",
   });

   const [newPassword, setNewPassword] = useState("");
   const [showPasswordField, setShowPasswordField] = useState(false);
   const [errors, setErrors] = useState({});
   const [error, setError] = useState("");
   const [successMessage, setSuccessMessage] = useState("");
   const [loading, setLoading] = useState(true);
   const [profileImage, setProfileImage] = useState(localStorage.getItem("profileImage") || "https://www.w3schools.com/howto/img_avatar.png");

   const userId = localStorage.getItem("memberId");
   const token = localStorage.getItem("accessToken");
   const userRole = localStorage.getItem("userRole");

   const fetchData = async () => {
      setLoading(true);
      const profileData = await fetchProfile(userId, token);
      if (profileData.error) {
         setError(profileData.error);
      } else {
         setFormData(profileData);
      }
      setLoading(false);
   };

   useEffect(() => {
      fetchData();
   }, []);

   const validateForm = () => {
      let newErrors = {};

      if (!formData.firstName.trim()) newErrors.firstName = "First name is required";
      if (!formData.lastName.trim()) newErrors.lastName = "Last name is required";
      if (!/^[6789]\d{9}$/.test(formData.phoneNumber)) {
         newErrors.phoneNumber = "Enter a valid 10-digit Indian phone number.";
      }

      setErrors(newErrors);
      return Object.keys(newErrors).length === 0;
   };

   const handleUpdate = async () => {
      setError("");
      setSuccessMessage("");

      if (!validateForm()) return;

      const updatePayload = { ...formData };
      if (newPassword.trim()) updatePayload.password = newPassword;

      const result = await updateProfile(userId, token, updatePayload);

      if (result.error) {
         setError("Failed to update profile. Please try again.");
      } else {
         setSuccessMessage("Profile updated successfully!");
         setNewPassword("");
         setShowPasswordField(false);

         // Auto-hide success message after 3 seconds
         setTimeout(() => setSuccessMessage(""), 3000);
      }
   };

   const handleImageUpload = (event) => {
      const file = event.target.files[0];
      if (file) {
         const reader = new FileReader();
         reader.onload = (e) => {
            setProfileImage(e.target.result);
            localStorage.setItem("profileImage", e.target.result);
         };
         reader.readAsDataURL(file);
      }
   };

   if (loading) return <p className="text-white text-center mt-10">Loading...</p>;

   return (
      <div className="min-h-screen flex flex-col items-center text-white p-6 bg-cover bg-center"
         style={{ backgroundImage: "url('https://images.alphacoders.com/114/thumb-1920-1146731.jpg')" }}>
         
         <Header userEmail={formData.email} userRole={userRole} />

         <div className="w-full max-w-3xl bg-white/10 backdrop-blur-lg p-8 rounded-xl shadow-lg border border-white/20 mt-20">
            <h2 className="text-3xl font-bold text-center mb-6">Your Profile</h2>

            <div className="flex justify-center mb-6 relative">
               <label className="cursor-pointer">
                  <div className="w-40 h-40 rounded-full overflow-hidden border-4 border-white shadow-lg transition-transform transform hover:scale-110">
                     <img
                        src={profileImage}
                        alt="Profile"
                        className="w-full h-full object-cover"
                     />
                  </div>
                  <input type="file" accept="image/*" className="hidden" onChange={handleImageUpload} />
               </label>
            </div>

            <div className="grid grid-cols-2 gap-4">
               <div>
                  <label className="block text-sm font-semibold">First Name</label>
                  <input
                     type="text"
                     value={formData.firstName}
                     onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                     className="w-full px-3 py-2 bg-white/20 text-white rounded-lg outline-none backdrop-blur-md"
                  />
               </div>

               <div>
                  <label className="block text-sm font-semibold">Last Name</label>
                  <input
                     type="text"
                     value={formData.lastName}
                     onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                     className="w-full px-3 py-2 bg-white/20 text-white rounded-lg outline-none backdrop-blur-md"
                  />
               </div>

               <div className="col-span-2">
                  <label className="block text-sm font-semibold">Username</label>
                  <input
                     type="text"
                     value={formData.username}
                     readOnly
                     className="w-full px-3 py-2 bg-white/20 text-white rounded-lg outline-none cursor-not-allowed backdrop-blur-md"
                  />
               </div>

               <div className="col-span-2">
                  <label className="block text-sm font-semibold">Email</label>
                  <input
                     type="email"
                     value={formData.email}
                     onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                     className="w-full px-3 py-2 bg-white/20 text-white rounded-lg outline-none backdrop-blur-md"
                  />
               </div>

               <div className="col-span-2">
                  <label className="block text-sm font-semibold">Phone Number</label>
                  <input
                     type="text"
                     value={formData.phoneNumber}
                     onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                     className="w-full px-3 py-2 bg-white/20 text-white rounded-lg outline-none backdrop-blur-md"
                  />
               </div>
            </div>

            {!showPasswordField ? (
               <div className="text-center mt-4">
                  <button onClick={() => setShowPasswordField(true)} className="text-blue-400 hover:underline">
                     Change Password
                  </button>
               </div>
            ) : (
               <div className="mt-4">
                  <label className="block text-sm font-semibold">New Password</label>
                  <input
                     type="password"
                     value={newPassword}
                     onChange={(e) => setNewPassword(e.target.value)}
                     className="w-full px-3 py-2 bg-white/20 text-white rounded-lg outline-none backdrop-blur-md"
                  />
                  <div className="text-center mt-2">
                     <button onClick={() => setShowPasswordField(false)} className="text-gray-300 hover:underline">
                        Nevermind, I'll keep my current password
                     </button>
                  </div>
               </div>
            )}

            {error && (
               <div className="bg-red-500 text-white text-center p-2 rounded-lg mt-4">
                  {error}
               </div>
            )}

            {successMessage && (
               <div className="bg-green-500 text-white text-center p-2 rounded-lg mt-4">
                  {successMessage}
               </div>
            )}

            <div className="flex justify-center mt-6">
               <button
                  onClick={handleUpdate}
                  className="px-6 py-2 bg-green-500 text-white font-semibold rounded-lg hover:bg-green-600 transition">
                  Save Changes
               </button>
            </div>
         </div>
      </div>
   );
};

export default Profile;
