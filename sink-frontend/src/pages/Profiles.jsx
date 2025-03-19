import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { fetchProfile, updateProfile } from "../api/profile";
import { FaEdit, FaSave, FaTimes } from "react-icons/fa";
import Header from "../pages/Header";
import Footer from "../pages/Footer";

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
   const [isEditing, setIsEditing] = useState(false);
   const [errors, setErrors] = useState({});
   const [error, setError] = useState("");
   const [successMessage, setSuccessMessage] = useState("");
   const [loading, setLoading] = useState(true);
   const [profileImage, setProfileImage] = useState(localStorage.getItem("profileImage") || "https://www.w3schools.com/howto/img_avatar.png");

   const userId = localStorage.getItem("memberId");
   const token = localStorage.getItem("accessToken");
   const userRole = localStorage.getItem("userRole").toUpperCase();
   const userEmail = localStorage.getItem("userEmail");

   const fetchData = async () => {
      try {
         const profileData = await fetchProfile(token);
         if (profileData.error) {
            setError(profileData.error);
         } else {
            setFormData((prevData) => ({
               ...prevData,
               ...profileData,
            }));
            // Update localStorage with the new profile data
            localStorage.setItem("userEmail", profileData.email);
            localStorage.setItem("firstName", profileData.firstName);
            localStorage.setItem("lastName", profileData.lastName);
            localStorage.setItem("phoneNumber", profileData.phoneNumber);
            localStorage.setItem("memberId", profileData.memberId);
            localStorage.setItem("userRole", profileData.userRole);
         }
      } catch (err) {
         setError("Failed to fetch profile data.");
      } finally {
         setLoading(false);
      }
   };

   useEffect(() => {
      if (!formData.firstName) {
         setLoading(true);  // Only set loading when fetching first time
         fetchData();
      }
   }, []);

   const validateForm = () => {
      let newErrors = {};

      if (!formData.firstName.trim()) newErrors.firstName = "First name is required";
      if (!formData.lastName.trim()) newErrors.lastName = "Last name is required";

      if (!/^[6789]\d{9}$/.test(formData.phoneNumber)) {
         newErrors.phoneNumber = "Enter a valid 10-digit Indian phone number.";
      }

      // Password validation (only if user is updating it)
      if (formData.password) {
         if (formData.password.length < 8) {
            newErrors.password = "Password must be at least 8 characters long.";
         } else if (!/[A-Z]/.test(formData.password)) {
            newErrors.password = "Password must contain at least one uppercase letter.";
         } else if (!/[a-z]/.test(formData.password)) {
            newErrors.password = "Password must contain at least one lowercase letter.";
         } else if (!/\d/.test(formData.password)) {
            newErrors.password = "Password must contain at least one digit.";
         } else if (!/[@$!%*?&]/.test(formData.password)) {
            newErrors.password = "Password must contain at least one special character (@, $, !, %, *, ?, &).";
         }
      }

      setErrors(newErrors);
      return Object.keys(newErrors).length === 0;
   };


   const handleUpdate = async () => {
      setError("");
      setSuccessMessage("");

      if (!validateForm()) return;

      const updatePayload = { ...formData };
      if (showPasswordField && newPassword.trim()) updatePayload.password = newPassword;

      const result = await updateProfile(token, updatePayload);

      if (result.error) {
         setError("Failed to update profile. Reverting changes...");
         fetchData(); // Refresh data to reset form fields
      } else {
         setSuccessMessage("Profile updated successfully!");
         setIsEditing(false);
         setNewPassword("");
         fetchData();
         setShowPasswordField(false);
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

   if (loading) {
      return <p className="text-white text-center mt-10">Loading profile...</p>;
   }

   return (
      <div className="min-h-screen flex flex-col items-center text-white p-6 pt-20 bg-black">
         <Header />

         <div className="w-full max-w-3xl bg-white/10 backdrop-blur-lg p-8 rounded-xl shadow-lg border border-white/20 mt-20">
            <h2 className="text-3xl font-bold text-center mb-6">Your Profile</h2>

            <div className="flex justify-center mb-6 relative">
               <label className={`cursor-pointer ${!isEditing ? "pointer-events-none opacity-100" : ""}`}>
                  <div className="w-40 h-40 rounded-full overflow-hidden border-4 border-white shadow-lg transition-transform transform hover:scale-110 relative">
                     <img src={profileImage} alt="Profile" className="w-full h-full object-cover" />

                     {isEditing && (
                        <div className="absolute bottom-0 w-full bg-black/50 text-white text-center text-sm py-1">
                           Edit
                        </div>
                     )}
                  </div>
                  {isEditing && (
                     <input type="file" accept="image/*" className="hidden" onChange={handleImageUpload} />
                  )}
               </label>
            </div>


            <div className="flex justify-end mb-4">
               {!isEditing ? (
                  <button onClick={() => setIsEditing(true)} className="text-blue-400 hover:underline flex items-center">
                     <FaEdit className="mr-2" /> Edit Profile
                  </button>
               ) : (
                  <button
                     onClick={() => {
                        setIsEditing(false);
                        setNewPassword("");
                        setShowPasswordField(false);
                        setError("");
                        setSuccessMessage("");
                     }}
                     className="text-gray-300 hover:underline flex items-center"
                  >
                     <FaTimes className="mr-2" /> Cancel
                  </button>
               )}
            </div>

            <div className="grid grid-cols-2 gap-4">
               {["firstName", "lastName", "email", "phoneNumber"].map((field, index) => (
                  <div key={index} className={field === "email" || field === "phoneNumber" ? "col-span-2" : ""}>
                     <label className="block text-sm font-semibold capitalize">{field.replace(/([A-Z])/g, ' $1')}</label>
                     <input
                        type={field === "email" ? "email" : "text"}
                        value={formData[field]}
                        onChange={(e) => setFormData({ ...formData, [field]: e.target.value })}
                        className={`w-full px-3 py-2 ${isEditing ? 'bg-white/20' : 'bg-white/10 cursor-not-allowed'} text-white rounded-lg outline-none backdrop-blur-md`}
                        readOnly={!isEditing}
                     />
                  </div>
               ))}

               <div className="col-span-2 relative">
                  <label className="block text-sm font-semibold">Username</label>
                  <div className="relative">
                     <input
                        type="text"
                        value={formData.username}
                        readOnly
                        className="w-full px-3 py-2 bg-gray-800 text-gray-400 rounded-lg outline-none cursor-not-allowed border border-gray-600"
                     />
                     <span className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500">
                        🔒
                     </span>
                  </div>
               </div>

               {isEditing && (
                  <div className="col-span-2">
                     <label className="block text-sm font-semibold">Password</label>
                     {showPasswordField ? (
                        <div>
                           <input
                              type="password"
                              value={newPassword}
                              onChange={(e) => setNewPassword(e.target.value)}
                              className="w-full px-3 py-2 bg-white/20 text-white rounded-lg outline-none backdrop-blur-md"
                           />
                           <button
                              onClick={() => { setShowPasswordField(false); setNewPassword(""); }}
                              className="text-sm text-green-400 hover:underline mt-2 block"
                           >
                              Nevermind, I'll keep the same password
                           </button>
                        </div>
                     ) : (
                        <button
                           onClick={() => setShowPasswordField(true)}
                           className="text-sm text-blue-400 hover:underline"
                        >
                           Change Password
                        </button>
                     )}
                  </div>
               )}
            </div>

            {error && <div className="bg-red-500 text-white text-center p-2 rounded-lg mt-4">{error}</div>}
            {successMessage && <div className="bg-green-500 text-white text-center p-2 rounded-lg mt-4">{successMessage}</div>}

            {isEditing && (
               <div className="flex justify-center mt-6">
                  <button
                     onClick={handleUpdate}
                     className="px-6 py-2 bg-green-500 text-white font-semibold rounded-lg hover:bg-green-600 transition flex items-center">
                     <FaSave className="mr-2" /> Save Changes
                  </button>
               </div>
            )}
         </div>
         <Footer />
      </div>
   );
};

export default Profile;
