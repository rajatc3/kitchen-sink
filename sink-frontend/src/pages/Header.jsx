import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";

const Header = ({ userEmail, userRole }) => {
   const navigate = useNavigate();
   const [isDropdownOpen, setIsDropdownOpen] = useState(false);
   const dropdownRef = useRef(null);

   // Close dropdown if user clicks outside
   useEffect(() => {
      const handleClickOutside = (event) => {
         if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
            setIsDropdownOpen(false);
         }
      };
      document.addEventListener("mousedown", handleClickOutside);
      return () => document.removeEventListener("mousedown", handleClickOutside);
   }, []);

   const handleLogout = () => {
      localStorage.clear();
      navigate("/login");
   };

   return (
      <header className="fixed top-0 left-0 w-full flex justify-between items-center bg-white/10 backdrop-blur-lg shadow-lg p-4 border-b border-white/20 px-10 z-50">
         {/* Left: App Name + Navigation */}
         <div className="flex items-center space-x-16 flex-1">
            <h1 className="text-3xl font-extrabold text-white tracking-wide drop-shadow-md">Kitchen Sink</h1>
            <nav className="flex space-x-8 text-lg font-semibold">
               <button onClick={() => navigate("/members")} className="text-white hover:text-gray-300 transition-all">
                  Home
               </button>
               {userRole === "ADMIN" && (
                  <button onClick={() => navigate("/admin")} className="text-white hover:text-gray-300 transition-all">
                     Admin Section
                  </button>
               )}
            </nav>
         </div>

         {/* Right: User Info with Hover & Click Dropdown */}
         <div className="relative" ref={dropdownRef}>
            <div
               className="flex items-center space-x-4 bg-white/10 px-4 py-2 rounded-full shadow-md backdrop-blur-lg cursor-pointer transition-all duration-200 hover:bg-white/20"
               onMouseEnter={() => setIsDropdownOpen(true)}
               onClick={() => setIsDropdownOpen(!isDropdownOpen)}
            >
               <span className="text-white font-medium">{userEmail}</span>
            </div>

            {/* Dropdown Menu */}
            {isDropdownOpen && (
               <div
                  className="absolute right-0 mt-2 w-48 bg-white text-black shadow-lg rounded-lg py-2 z-50 transition-opacity duration-300 ease-in-out"
                  onMouseEnter={() => setIsDropdownOpen(true)}
                  onMouseLeave={() => setIsDropdownOpen(false)}
               >
                  <button
                     onClick={() => navigate("/profile")}
                     className="block px-4 py-2 hover:bg-gray-100 w-full text-left"
                  >
                     View Profile
                  </button>
                  <button
                     onClick={handleLogout}
                     className="block px-4 py-2 hover:bg-red-100 w-full text-left text-red-600"
                  >
                     Logout
                  </button>
               </div>
            )}
         </div>
      </header>
   );
};

export default Header;