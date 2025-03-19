import { useState } from "react";
import { register, checkUsername } from "../api/auth";
import { useNavigate, Link } from "react-router-dom";
import { Eye, EyeOff, CheckCircle, XCircle, Loader2 } from "lucide-react";

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
   const [showPassword, setShowPassword] = useState(false);
   const [showRepeatPassword, setShowRepeatPassword] = useState(false);
   const [usernameAvailable, setUsernameAvailable] = useState(null);
   const [checkingUsername, setCheckingUsername] = useState(false);
   const navigate = useNavigate();

   const validatePassword = (password) => {
      if (!password) return "Password is required.";
      if (password.length < 8) return "Password must be at least 8 characters.";
      if (!/[A-Z]/.test(password)) return "Must contain at least one uppercase letter.";
      if (!/[a-z]/.test(password)) return "Must contain at least one lowercase letter.";
      if (!/\d/.test(password)) return "Must include at least one number.";
      if (!/[@$!%*?&]/.test(password)) return "Must include one special character (@$!%*?&).";
      return "";
   };

   const checkUsernameAvailability = async (username) => {
      if (!username.trim()) {
         setUsernameAvailable(null);
         return;
      }

      setCheckingUsername(true);
      try {
         const response = await checkUsername(username); // API call
         setUsernameAvailable(response.available); // Assume API returns `{ available: true/false }`
      } catch (error) {
         setUsernameAvailable(null);
      }
      setCheckingUsername(false);
   };

   const validateForm = () => {
      let newErrors = {};

      if (!formData.username.trim()) newErrors.username = "Username is required.";
      if (!formData.firstName.trim()) newErrors.firstName = "First name is required.";
      if (!formData.lastName.trim()) newErrors.lastName = "Last name is required.";

      if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(formData.email)) {
         newErrors.email = "Enter a valid email address (e.g., user@example.com).";
      }

      if (!/^[6789]\d{9}$/.test(formData.phoneNumber)) {
         newErrors.phoneNumber = "Enter a valid 10-digit Indian phone number (starting with 6, 7, 8, or 9).";
      }

      const passwordError = validatePassword(formData.password);
      if (passwordError) {
         newErrors.password = passwordError;
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
            setSuccessMessage("Registration successful! You will be redirected to the login page in 3 seconds.");
            setTimeout(() => {
               navigate("/login");
            }, 3000);
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
               <div className="relative">
                  <label className="block text-gray-200">Username</label>
                  <div className="flex items-center w-full relative">
                     <input
                        type="text"
                        value={formData.username}
                        onChange={(e) => {
                           const username = e.target.value;
                           setFormData({ ...formData, username });
                           setUsernameAvailable(null);
                           clearTimeout(window.usernameCheckTimeout);
                           window.usernameCheckTimeout = setTimeout(() => {
                              checkUsernameAvailability(username);
                           }, 500);
                        }}
                        className="w-full p-2 rounded bg-white/30 text-white pr-10"
                     />
                     {checkingUsername && <Loader2 className="absolute right-2 text-white animate-spin" size={18} />}
                     {usernameAvailable === true && <CheckCircle className="absolute right-2 text-green-500" size={18} />}
                     {usernameAvailable === false && <XCircle className="absolute right-2 text-red-500" size={18} />}
                  </div>
                  <p className="text-gray-400 text-xs mt-1">⚠️ Username is permanent</p>
                  {errors.username && <p className="text-red-500 text-xs">{errors.username}</p>}
               </div>
               <div>
                  <label className="block text-gray-200">Phone Number</label>
                  <input type="text" value={formData.phoneNumber} onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })} className="w-full p-2 rounded bg-white/30 text-white" />
                  {errors.phoneNumber && <p className="text-red-500 text-xs">{errors.phoneNumber}</p>}
               </div>
               {/* Password Field */}
               <div className="relative">
                  <label className="block text-gray-200">Password</label>
                  <div className="flex items-center w-full">
                     <input
                        type={showPassword ? "text" : "password"}
                        value={formData.password}
                        onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                        className="w-full p-2 rounded bg-white/30 text-white pr-10"
                        placeholder="Enter password"
                     />
                     <button
                        type="button"
                        className="absolute right-2 text-white"
                        onClick={() => setShowPassword(!showPassword)}
                     >
                        {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                     </button>
                  </div>
                  {errors.password && <p className="text-red-500 text-xs">{errors.password}</p>}
               </div>
               {/* Repeat Password Field */}
               <div className="relative">
                  <label className="block text-gray-200">Repeat Password</label>
                  <div className="flex items-center w-full">
                     <input
                        type={showRepeatPassword ? "text" : "password"}
                        value={formData.repeatPassword}
                        onChange={(e) => setFormData({ ...formData, repeatPassword: e.target.value })}
                        className="w-full p-2 rounded bg-white/30 text-white pr-10"
                        placeholder="Repeat password"
                     />
                     <button
                        type="button"
                        className="absolute right-2 text-white"
                        onClick={() => setShowRepeatPassword(!showRepeatPassword)}
                     >
                        {showRepeatPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                     </button>
                  </div>
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