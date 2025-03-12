import { useState } from "react";
import { login } from "../api/auth";
import { useNavigate } from "react-router-dom";

const Login = () => {
   const [identifier, setIdentifier] = useState(""); // Can be email or username
   const [password, setPassword] = useState("");
   const [error, setError] = useState("");
   const [identifierError, setIdentifierError] = useState("");
   const [passwordError, setPasswordError] = useState("");

   const validateForm = () => {
      let isValid = true;
      setIdentifierError("");
      setPasswordError("");

      // Check if input looks like an email
      if (!identifier.trim()) {
         setIdentifierError("Email or Username is required");
         isValid = false;
      } else {
         // Check if input looks like an email
         const isEmail = identifier.includes("@");
   
         if (isEmail && !identifier.match(/^\S+@\S+\.\S+$/)) {
            setIdentifierError("Invalid email format");
            isValid = false;
         }
      }

      if (password.length < 4) {
         setPasswordError("Password must be longer than 4 characters");
         isValid = false;
      }

      return isValid;
   };

   const navigate = useNavigate();

   const handleSubmit = async (e) => {
      e.preventDefault();
      setError("");

      if (!validateForm()) return;

      try {
         const data = await login(identifier, password);

         const userRole = data.role.includes("ROLE_ADMIN") ? "Admin" : "User";

         localStorage.setItem("accessToken", data.accessToken);
         localStorage.setItem("refreshToken", data.refreshToken);
         localStorage.setItem("userEmail", identifier);
         localStorage.setItem("userRole", userRole);
         navigate("/members"); // Redirect to Members page
      } catch (err) {
         setError(err.message || "Invalid credentials");
      }
   };

   return (
      <div
         className="flex min-h-screen items-center justify-end bg-cover bg-center px-4 sm:px-8"
         style={{ backgroundImage: "url('https://images2.alphacoders.com/132/1325726.png')" }}
      >
         <div className="w-full sm:w-3/4 md:w-1/2 lg:w-1/3 xl:w-1/4 max-w-md p-6 sm:p-8 bg-white/20 backdrop-blur-lg rounded-2xl shadow-lg border border-white/30 
        md:mr-16 lg:mr-24 xl:mr-32">
            <h2 className="text-3xl font-bold text-white text-center">Kitchensink Application</h2>
            <p className="text-gray-300 text-center mb-6">Sign in using email or username</p>

            {error && <p className="text-red-500 text-sm text-center">{error}</p>}

            <form onSubmit={handleSubmit} className="space-y-4">
               {/* Identifier (Email or Username) */}
               <div>
                  <label className="block text-gray-200 font-medium">Email or Username</label>
                  <input
                     type="text"
                     className="w-full p-3 border border-gray-300 rounded-lg bg-white/30 text-white placeholder-gray-200 focus:ring-2 focus:ring-indigo-400"
                     placeholder="Enter your email or username"
                     value={identifier}
                     onChange={(e) => setIdentifier(e.target.value)}
                  />
                  {identifierError && <p className="text-red-500 text-sm mt-1">{identifierError}</p>}
               </div>

               {/* Password */}
               <div>
                  <label className="block text-gray-200 font-medium">Password</label>
                  <input
                     type="password"
                     className="w-full p-3 border border-gray-300 rounded-lg bg-white/30 text-white placeholder-gray-200 focus:ring-2 focus:ring-indigo-400"
                     placeholder="Enter your password"
                     value={password}
                     onChange={(e) => setPassword(e.target.value)}
                  />
                  {passwordError && <p className="text-red-500 text-sm mt-1">{passwordError}</p>}
               </div>

               {/* Submit Button */}
               <button
                  type="submit"
                  className="w-full bg-indigo-600 text-white py-3 rounded-lg font-semibold hover:bg-indigo-700 transition-all"
               >
                  Sign In
               </button>
            </form>
         </div>
      </div>
   );
};

export default Login;
