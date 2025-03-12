import { useState } from "react";
import { register } from "../api/auth";
import { useNavigate, Link } from "react-router-dom";

const Register = () => {
   const [formData, setFormData] = useState({
      username: "",
      firstName: "",
      lastName: "",
      email: "",
      phoneNumber: "",
      password: "",
      repeatPassword: ""
   });
   const [errors, setErrors] = useState({});
   const [error, setError] = useState("");
   const [successMessage, setSuccessMessage] = useState("");
   const navigate = useNavigate();

   const validateForm = () => {
      let newErrors = {};


      if (!formData.username.trim()) newErrors.username = "Username is required";
      if (!formData.firstName.trim()) newErrors.firstName = "First name is required";
      if (!formData.lastName.trim()) newErrors.lastName = "Last name is required";

      if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(formData.email)) {
         newErrors.email = "Enter a valid email address (e.g., user@example.com).";
      }

      if (!/^[6789]\d{9}$/.test(formData.phoneNumber)) {
         newErrors.phoneNumber = "Enter a valid 10-digit Indian phone number (starting with 6, 7, 8, or 9).";
      }

      if (formData.password.length < 6) {
         newErrors.password = "Password must be at least 6 characters long.";
      }

      if (formData.password !== formData.repeatPassword) {
         newErrors.repeatPassword = "Passwords do not match.";
      }

      setErrors(newErrors);
      return Object.keys(newErrors).length === 0;
   };

   const handleSubmit = async (e) => {
      e.preventDefault();
      setError("");
      setSuccessMessage("");

      if (!validateForm()) return;

      try {
         const response = await register(formData); // Ensure register() returns a structured response

         if (response.success) {
            setSuccessMessage("Registration successful! You will be redirected to the login page in 5 seconds.");
            setTimeout(() => {
               navigate("/login");
            }, 5000);
         } else {
            // If the backend returns multiple errors in an array
            if (Array.isArray(response.errors) && response.errors.length > 0) {
               setError(response.errors.join(" "));
            } else {
               setError(response.message || "Registration failed due to an unknown error.");
            }
         }
      } catch (err) {
         setError(err.message || "Registration failed due to an unexpected error.");
      }
   };



   return (
      <div className="flex min-h-screen items-center justify-end bg-cover bg-center px-4 sm:px-8"
         style={{ backgroundImage: "url('https://images2.alphacoders.com/132/1325726.png')" }}>
         <div className="w-full sm:w-3/4 md:w-1/2 lg:w-1/3 xl:w-1/4 max-w-md p-6 sm:p-8 bg-white/20 backdrop-blur-lg rounded-2xl shadow-lg border border-white/30">
            <h2 className="text-2xl font-bold text-white text-center">Register</h2>
            <p className="text-gray-300 text-center mb-4">Create your account</p>

            {error && (
               <div className="text-red-500 text-sm text-center">
                  {Array.isArray(error) ? error.map((err, index) => (
                     <p key={index}>{err}</p>
                  )) : <p>{error}</p>}
               </div>
            )}

            {successMessage && <p className="text-green-500 text-sm text-center">{successMessage}</p>}

            <form onSubmit={handleSubmit} className="grid grid-cols-2 gap-4 text-sm">
               <div className="col-span-2">
                  <label className="block text-gray-200">Email</label>
                  <input type="email" value={formData.email} onChange={(e) => setFormData({ ...formData, email: e.target.value })} className="w-full p-2 rounded bg-white/30 text-white" />
                  {errors.email && <p className="text-red-500 text-xs">{errors.email}</p>}
               </div>
               <div>
                  <label className="block text-gray-200">First Name</label>
                  <input type="text" value={formData.firstName} onChange={(e) => setFormData({ ...formData, firstName: e.target.value })} className="w-full p-2 rounded bg-white/30 text-white" />
                  {errors.firstName && <p className="text-red-500 text-xs">{errors.firstName}</p>}
               </div>
               <div>
                  <label className="block text-gray-200">Last Name</label>
                  <input type="text" value={formData.lastName} onChange={(e) => setFormData({ ...formData, lastName: e.target.value })} className="w-full p-2 rounded bg-white/30 text-white" />
                  {errors.lastName && <p className="text-red-500 text-xs">{errors.lastName}</p>}
               </div>
               <div>
                  <label className="block text-gray-200">Username</label>
                  <input type="text" value={formData.username} onChange={(e) => setFormData({ ...formData, username: e.target.value })} className="w-full p-2 rounded bg-white/30 text-white" />
                  {errors.username && <p className="text-red-500 text-xs">{errors.username}</p>}
               </div>
               <div>
                  <label className="block text-gray-200">Phone Number</label>
                  <input type="text" value={formData.phoneNumber} onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })} className="w-full p-2 rounded bg-white/30 text-white" />
                  {errors.phoneNumber && <p className="text-red-500 text-xs">{errors.phoneNumber}</p>}
               </div>
               <div>
                  <label className="block text-gray-200">Password</label>
                  <input type="password" value={formData.password} onChange={(e) => setFormData({ ...formData, password: e.target.value })} className="w-full p-2 rounded bg-white/30 text-white" />
                  {errors.password && <p className="text-red-500 text-xs">{errors.password}</p>}
               </div>
               <div>
                  <label className="block text-gray-200">Repeat Password</label>
                  <input type="password" value={formData.repeatPassword} onChange={(e) => setFormData({ ...formData, repeatPassword: e.target.value })} className="w-full p-2 rounded bg-white/30 text-white" />
                  {errors.repeatPassword && <p className="text-red-500 text-xs">{errors.repeatPassword}</p>}
               </div>
               <div className="col-span-2">
                  <button type="submit" className="w-full bg-indigo-600 text-white py-2 rounded hover:bg-indigo-700 transition-all">Register</button>
               </div>
            </form>

            <p className="text-gray-300 text-center mt-3 text-sm">
               Already have an account? <Link to="/login" className="text-indigo-300">Login here</Link>
            </p>
         </div>
      </div>
   );
};

export default Register;