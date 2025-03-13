import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { fetchMembers, addMember, updateMember, deleteMember } from "../api/members";
import Header from "../pages/Header"; 

const Members = () => {
   const [members, setMembers] = useState([]);
   const [newMember, setNewMember] = useState({ name: "", email: "", phoneNumber: "" });
   const [editingMemberId, setEditingMemberId] = useState(null);
   const [editedMember, setEditedMember] = useState({});
   const [errors, setErrors] = useState([]);
   const navigate = useNavigate();

   const userEmail = localStorage.getItem("userEmail");
   const userRole = localStorage.getItem("userRole")?.toUpperCase();

   useEffect(() => {
      const getMembers = async () => {
         const token = localStorage.getItem("accessToken");
         if (!token) {
            alert("Unauthorized! Redirecting to login.");
            navigate("/login");
            return;
         }
         try {
            const data = await fetchMembers(token);
            setMembers(data);
         } catch (error) {
            console.error("Error fetching members:", error);
         }
      };
      getMembers();
   }, [navigate]);

   return (
      <div className="min-h-screen flex flex-col items-center text-white p-6 bg-cover bg-center"
         style={{ backgroundImage: "url('https://images.alphacoders.com/114/thumb-1920-1146731.jpg')" }}>

         {/* Using the extracted Header component */}
         <Header userEmail={userEmail} userRole={userRole} />

         {/* MAIN CONTENT (Table Starts Below Header) */}
         <div className="w-full max-w-6xl bg-white/10 backdrop-blur-lg p-6 rounded-xl shadow-lg border border-white/20 mt-24">
            <h2 className="text-3xl font-bold text-center mb-6">Kitchen Sink Members List</h2>

            {userRole === "ADMIN" && (
               <div className="mb-4 grid grid-cols-4 gap-2">
                  <input type="text" placeholder="Name" className="p-2 rounded text-black" value={newMember.name} onChange={(e) => setNewMember({ ...newMember, name: e.target.value })} />
                  <input type="email" placeholder="Email" className="p-2 rounded text-black" value={newMember.email} onChange={(e) => setNewMember({ ...newMember, email: e.target.value })} />
                  <input type="text" placeholder="Phone" className="p-2 rounded text-black" value={newMember.phoneNumber} onChange={(e) => setNewMember({ ...newMember, phoneNumber: e.target.value })} />
                  <button onClick={() => { }} className="bg-green-500 px-4 py-2 rounded hover:bg-green-600">Add</button>
               </div>
            )}

            {/* Table Section - Now Correctly Positioned */}
            <div className="overflow-auto max-h-[500px] border border-white/20 rounded-lg">
               <table className="w-full text-white bg-white/10 backdrop-blur-lg rounded-lg shadow-lg">
                  <thead className="bg-gradient-to-r from-indigo-700 to-purple-700 text-white">
                     <tr className="text-left">
                        <th className="p-4">ID</th>
                        <th className="p-4">Name</th>
                        <th className="p-4">Email</th>
                        <th className="p-4">Phone</th>
                        {userRole === "ADMIN" && <th className="p-4 text-center">Actions</th>}
                     </tr>
                  </thead>
                  <tbody>
                     {members.map((member) => (
                        <tr key={member.memberId} className="odd:bg-white/20 even:bg-white/10 transition-all duration-300 hover:bg-white/30 border-b border-white/20">
                           <td className="p-4">{member.memberId}</td>
                           <td className="p-4">{member.name}</td>
                           <td className="p-4">{member.email}</td>
                           <td className="p-4">{member.phoneNumber}</td>
                           {userRole === "ADMIN" && (
                              <td className="p-4 flex justify-center space-x-2">
                                 <button className="bg-blue-500 px-4 py-2 rounded-lg shadow-md transition-all duration-300 hover:bg-blue-600 hover:scale-105">Edit</button>
                                 <button className="bg-red-500 px-4 py-2 rounded-lg shadow-md transition-all duration-300 hover:bg-red-600 hover:scale-105">Delete</button>
                              </td>
                           )}
                        </tr>
                     ))}
                  </tbody>
               </table>
            </div>
         </div>
      </div>
   );
};

export default Members;
