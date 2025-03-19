import { useState, useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";

const Header = () => {
   const navigate = useNavigate();
   const location = useLocation();
   const [isDropdownOpen, setIsDropdownOpen] = useState(false);
   const dropdownRef = useRef(null);
   const userFullName = localStorage.getItem("firstName") +" "+ localStorage.getItem("lastName");
   const userRole = localStorage.getItem("userRole").toUpperCase();
   
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
      <header className="fixed top-0 left-0 w-full flex justify-between items-center bg-white/10 backdrop-blur-lg shadow-lg p-2 border-b border-white/20 px-10 z-50">
         {/* Left: App Name + Navigation */}
         <div className="flex items-center space-x-16 flex-1">
            <h1 className="text-3xl font-extrabold text-white tracking-wide drop-shadow-md">Kitchen Sink</h1>
            <nav className="flex space-x-8 text-lg font-semibold">
               <NavItem path="/home" currentPath={location.pathname}>Community Hub</NavItem>
               {userRole === "ADMIN" && <NavItem path="/admin" currentPath={location.pathname}>Admin's Corner</NavItem>}
            </nav>
         </div>

         {/* Right: User Info with Hover & Click Dropdown */}
         <div className="relative" ref={dropdownRef}>
            <div
               className="flex items-center space-x-4 bg-white/10 px-3 py-1.5 rounded-full shadow-md backdrop-blur-lg cursor-pointer transition-all duration-200 hover:bg-white/20"
               onMouseEnter={() => setIsDropdownOpen(true)}
               onClick={() => setIsDropdownOpen(!isDropdownOpen)}
            >
               <span className="text-white font-medium">{userFullName}</span>
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
                     className="block px-3 py-1.5 hover:bg-gray-100 w-full text-left"
                  >
                     View Profile
                  </button>
                  <button
                     onClick={handleLogout}
                     className="block px-3 py-1.5 hover:bg-red-100 w-full text-left text-red-600"
                  >
                     Logout
                  </button>
               </div>
            )}
         </div>
      </header>
   );
};

// Reusable Navigation Item with Active State Styling
const NavItem = ({ path, currentPath, children }) => {
   const navigate = useNavigate();
   const isActive = currentPath === path;

   return (
      <button
         onClick={() => navigate(path)}
         className={`relative transition-all duration-300 ${
            isActive ? "text-blue-400 font-semibold" : "text-white hover:text-gray-300"
         }`}
      >
         {children}
         {isActive && (
            <span className="absolute left-0 bottom-0 w-full h-0.5 bg-blue-400 animate-pulse"></span>
         )}
      </button>
   );
};

export default Header;
