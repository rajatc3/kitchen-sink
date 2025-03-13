import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

const Profile = () => {
   const [profile, setProfile] = useState({
      firstName: "",
      lastName: "",
      username: "",
      email: "",
      phoneNumber: "",
   });

   const [editFirstName, setEditFirstName] = useState("");
   const [editLastName, setEditLastName] = useState("");
   const [loading, setLoading] = useState(true);
   const [error, setError] = useState(null);
   const navigate = useNavigate();

   // Get user data from localStorage
   const userId = localStorage.getItem("memberId");
   const token = localStorage.getItem("accessToken");
   const userRole = localStorage.getItem("userRole") || "USER"; 
   const userEmail = localStorage.getItem("userEmail") || "Not Available";

   useEffect(() => {
      if (!token) {
         alert("Unauthorized! Redirecting to login.");
         navigate("/login");
         return;
      }

      const fetchProfile = async () => {
         try {
            const response = await fetch(`http://localhost:8080/api/dashboard/profile/${userId}`, {
               method: "GET",
               headers: {
                  "Authorization": `Bearer ${token}`,
                  "Content-Type": "application/json",
               },
            });

            if (!response.ok) throw new Error("Failed to fetch profile");
            const data = await response.json();
            setProfile(data);
            setEditFirstName(data.firstName);
            setEditLastName(data.lastName);
         } catch (err) {
            setError(err.message);
         } finally {
            setLoading(false);
         }
      };

      fetchProfile();
   }, [navigate, token, userId]);

   const handleUpdate = async () => {
      if (!editFirstName.trim() || !editLastName.trim()) {
         return alert("First Name and Last Name cannot be empty!");
      }

      try {
         const response = await fetch(`http://localhost:8080/api/dashboard/profile/${userId}`, {
            method: "PUT",
            headers: {
               "Authorization": `Bearer ${token}`,
               "Content-Type": "application/json",
            },
            body: JSON.stringify({
               firstName: editFirstName,
               lastName: editLastName,
            }),
         });

         if (!response.ok) throw new Error("Failed to update profile");

         alert("Profile updated successfully!");
         setProfile({ ...profile, firstName: editFirstName, lastName: editLastName });
      } catch (err) {
         setError(err.message);
      }
   };

   const handleLogout = () => {
      localStorage.clear();
      navigate("/login");
   };

   if (loading) return <p className="text-white text-center mt-10">Loading...</p>;
   if (error) return <p className="text-red-500 text-center mt-10">{error}</p>;

   return (
      <div className="min-h-screen flex flex-col items-center text-white p-6 bg-cover bg-center"
         style={{ backgroundImage: "url('https://wallpapercave.com/wp/6SLzBEY.jpg')" }}>

         {/* HEADER - FIXED AT TOP */}
         <header className="fixed top-0 left-0 w-full flex justify-between items-center bg-white/10 backdrop-blur-lg shadow-lg p-4 border-b border-white/20 px-10 z-50">
            <h1 className="text-3xl font-extrabold text-white tracking-wide drop-shadow-md">Kitchen Sink</h1>

            <nav className="flex space-x-24 text-lg font-semibold">
               <button onClick={() => navigate("/members")} className="text-white hover:text-gray-300 transition-all">Home</button>
               <button onClick={() => navigate("/profile")} className="text-white hover:text-gray-300 transition-all">Your Profile</button>
               {userRole === "ADMIN" && <button onClick={() => navigate("/admin")} className="text-white hover:text-gray-300 transition-all">Admin Section</button>}
            </nav>

            <div className="flex items-center space-x-4 bg-white/10 px-4 py-2 rounded-full shadow-md backdrop-blur-lg">
               <span className="text-white font-medium">{userEmail} - <span className="font-bold">{userRole}</span></span>
               <button onClick={handleLogout} className="px-4 py-2 bg-red-600 rounded-lg hover:bg-red-700 transition-all">Logout</button>
            </div>
         </header>

         {/* Profile Section */}
         <div className="w-full max-w-3xl bg-white/10 backdrop-blur-lg p-6 rounded-xl shadow-lg border border-white/20 mt-24">
            <h2 className="text-3xl font-bold text-center mb-6">Profile</h2>

            <div className="space-y-4">
               <div>
                  <label className="block text-lg font-medium">First Name</label>
                  <input type="text" className="w-full p-2 rounded-lg text-black" value={editFirstName} onChange={(e) => setEditFirstName(e.target.value)} />
               </div>

               <div>
                  <label className="block text-lg font-medium">Last Name</label>
                  <input type="text" className="w-full p-2 rounded-lg text-black" value={editLastName} onChange={(e) => setEditLastName(e.target.value)} />
               </div>

               <div>
                  <label className="block text-lg font-medium">Username</label>
                  <input type="text" className="w-full p-2 rounded-lg bg-gray-300 text-black cursor-not-allowed" value={profile.username} readOnly />
               </div>

               <div>
                  <label className="block text-lg font-medium">Email</label>
                  <input type="email" className="w-full p-2 rounded-lg bg-gray-300 text-black cursor-not-allowed" value={profile.email} readOnly />
               </div>

               <div>
                  <label className="block text-lg font-medium">Phone Number</label>
                  <input type="text" className="w-full p-2 rounded-lg bg-gray-300 text-black cursor-not-allowed" value={profile.phoneNumber} readOnly />
               </div>

               <button onClick={handleUpdate} className="w-full bg-green-500 px-4 py-2 rounded-lg hover:bg-green-600 transition-all mt-4">
                  Update Profile
               </button>
            </div>
         </div>
      </div>
   );
};

export default Profile;
