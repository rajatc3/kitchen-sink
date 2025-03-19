import { useState } from "react";
import { login } from "../api/auth";
import { fetchProfile } from "../api/profile";
import { useNavigate, Link } from "react-router-dom";

const Login = () => {
   const [identifier, setIdentifier] = useState("");
   const [password, setPassword] = useState("");
   const [error, setError] = useState("");
   const [identifierError, setIdentifierError] = useState("");
   const [passwordError, setPasswordError] = useState("");

   const navigate = useNavigate();

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
         if (isEmail && !identifier.match(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/)) {
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

   const handleSubmit = async (e) => {
      e.preventDefault();
      setError("");

      if (!validateForm()) return;

      try {
         const data = await login(identifier, password);
         const userRole = data.role.includes("ROLE_ADMIN") ? "Admin" : "User";

         localStorage.setItem("accessToken", data.accessToken);
         localStorage.setItem("refreshToken", data.refreshToken);

         const profileData = await fetchProfile(data.accessToken);
         localStorage.setItem("userEmail", profileData.email);
         localStorage.setItem("firstName", profileData.firstName);
         localStorage.setItem("lastName", profileData.lastName);
         localStorage.setItem("phoneNumber", profileData.phoneNumber);
         localStorage.setItem("memberId", profileData.memberId)
         localStorage.setItem("userRole", userRole);

         navigate("/home");
      } catch (err) {
         setError(err.message || "Invalid credentials");
      }
   };

   return (
      <div className="flex min-h-screen items-center justify-center bg-cover bg-center px-4 sm:px-8"
         style={{ backgroundImage: "url('https://images2.alphacoders.com/132/1325726.png')" }}>
         {/* Left Side with Intro */}
         <div className="hidden sm:block w-1/2 p-6 sm:p-8 text-white bg-opacity-60 backdrop-blur-lg rounded-l-2xl flex items-center justify-center">
            <div className="text-center max-w-md">
               <h2 className="text-4xl font-bold mb-4">Kitchen Sink Application</h2>
               <p className="text-lg">
                  Just like a kitchen sink holds a variety of things, <strong>Kitchen Sink</strong> is a platform where every idea, every thought, and every perspective finds a place.
                  It’s a space for users to share their articles on anything and everything – from the latest trends to personal reflections.
                  Our community is here to read, engage, and comment on the diverse range of content, creating a vibrant exchange of ideas.
                  Whether you’re cooking up an article on technology, health, or just daily musings, <strong>Kitchen Sink</strong> is the place to let your thoughts simmer and serve them to the world.
               </p>
            </div>
         </div>

         {/* Right Side with Login Form */}
         <div className="w-full sm:w-1/2 md:w-1/3 lg:w-1/4 xl:w-1/4 max-w-md p-6 sm:p-8 bg-white/20 backdrop-blur-lg rounded-2xl shadow-lg border border-white/30">
            <p className="text-gray-300 text-center mb-6">Sign in using email or username</p>

            {error && <p className="text-red-500 text-sm text-center">{error}</p>}

            <form onSubmit={handleSubmit} className="space-y-4">
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

               <button type="submit" className="w-full bg-indigo-600 text-white py-3 rounded-lg font-semibold hover:bg-indigo-700 transition-all">
                  Sign In
               </button>
            </form>

            <p className="text-gray-300 text-center mt-4">
               New User? <Link to="/register" className="text-indigo-300">Register here</Link>
            </p>
         </div>
      </div>
   );
};

export default Login;
