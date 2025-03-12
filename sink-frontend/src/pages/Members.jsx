import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { fetchMembers, addMember, updateMember, deleteMember } from "../api/members";

const Members = () => {
   const [members, setMembers] = useState([]);
   const [newMember, setNewMember] = useState({ name: "", email: "", phoneNumber: "" });
   const [editingMemberId, setEditingMemberId] = useState(null);
   const [editedMember, setEditedMember] = useState({});
   const [errors, setErrors] = useState([]);
   const navigate = useNavigate();

   const userEmail = localStorage.getItem("userEmail");
   const userRole = localStorage.getItem("userRole");

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

   const handleAddMember = async () => {
      setErrors([]);
      if (!newMember.name || !newMember.email || !newMember.phoneNumber) {
         setErrors(["Please fill all fields"]);
         return;
      }
      try {
         const token = localStorage.getItem("accessToken");
         const addedMember = await addMember(token, newMember);
         setMembers([...members, addedMember]);
         setNewMember({ name: "", email: "", phoneNumber: "" });
      } catch (error) {
         setErrors(error.errors || ["Failed to add member"]);
      }
   };

   const handleEditClick = (member) => {
      setEditingMemberId(member.memberId);
      setEditedMember({ ...member });
   };

   const handleInputChange = (e, field) => {
      setEditedMember({ ...editedMember, [field]: e.target.value });
   };

   const handleUpdateMember = async () => {
      if (!editingMemberId) return;
      setErrors([]);

      try {
         const token = localStorage.getItem("accessToken");
         const updatedData = await updateMember(token, editingMemberId, editedMember);

         setMembers(members.map(m => (m.memberId === editingMemberId ? updatedData : m)));
         setEditingMemberId(null);
      } catch (error) {
         setErrors(error.errors || ["Failed to update member"]);
      }
   };

   const handleCancelEdit = () => {
      setEditingMemberId(null);
      setErrors([]);
   };

   const handleDeleteMember = async (memberId) => {
      if (!window.confirm("Are you sure you want to delete this member?")) return;
      try {
         const token = localStorage.getItem("accessToken");
         await deleteMember(token, memberId);
         setMembers(members.filter(m => m.memberId !== memberId));
      } catch (error) {
         setErrors(["Error deleting member"]);
      }
   };

   return (
      <div className="min-h-screen flex flex-col items-center justify-center text-white p-6 bg-cover bg-center"
         style={{ backgroundImage: "url('https://wallpapercave.com/wp/6SLzBEY.jpg')" }}>

         <div className="fixed top-5 right-10 z-50 pointer-events-none">
            <div className="flex items-center space-x-4 bg-white/20 px-4 py-2 rounded-full shadow-md backdrop-blur-lg pointer-events-auto">
               <span>{userEmail} - <span className="font-bold">{userRole}</span></span>
               <button onClick={() => { localStorage.clear(); navigate("/login"); }}
                  className="px-4 py-2 bg-red-600 rounded-lg hover:bg-red-700">Logout</button>
            </div>
         </div>

         {errors.length > 0 && (
            <div className="fixed top-10 left-1/2 transform -translate-x-1/2 bg-red-500 text-white px-5 py-2 rounded-lg shadow-lg flex items-center space-x-3 
      opacity-100 transition-all duration-500 ease-in-out animate-slideIn">
               <div className="text-sm font-medium tracking-wide font-sans">
                  {errors.map((err, index) => <p key={index}>{err}</p>)}
               </div>
               <button
                  onClick={() => setErrors([])}
                  className="text-white text-lg font-bold transition-all transform duration-300 hover:scale-110 hover:text-gray-300">
                  ✖
               </button>
            </div>
         )}


         <div className="w-full max-w-6xl bg-white/10 backdrop-blur-lg p-6 rounded-xl shadow-lg border border-white/20">
            <h2 className="text-3xl font-bold text-center mb-6">Kitchen Sink Members List</h2>
            {userRole.toUpperCase() === "ADMIN" && (
               <div className="mb-4 grid grid-cols-4 gap-2">
                  <input type="text" placeholder="Name" className="p-2 rounded text-black" value={newMember.name} onChange={(e) => setNewMember({ ...newMember, name: e.target.value })} />
                  <input type="email" placeholder="Email" className="p-2 rounded text-black" value={newMember.email} onChange={(e) => setNewMember({ ...newMember, email: e.target.value })} />
                  <input type="text" placeholder="Phone" className="p-2 rounded text-black" value={newMember.phoneNumber} onChange={(e) => setNewMember({ ...newMember, phoneNumber: e.target.value })} />
                  <button onClick={handleAddMember} className="bg-green-500 px-4 py-2 rounded hover:bg-green-600">Add</button>
               </div>
            )}
            <div className="overflow-x-auto">
               <table className="w-full text-white bg-white/10 backdrop-blur-lg border border-white/20 rounded-lg shadow-lg overflow-hidden">
                  <thead className="bg-gradient-to-r from-indigo-700 to-purple-700 text-white">
                     <tr className="text-left">
                        <th className="p-4">ID</th>
                        <th className="p-4">Name</th>
                        <th className="p-4">Email</th>
                        <th className="p-4">Phone</th>
                        {userRole.toUpperCase() === "ADMIN" && <th className="p-4 text-center">Actions</th>}
                     </tr>
                  </thead>
                  <tbody>
                     {members.map((member) => (
                        <tr
                           key={member.memberId}
                           className="odd:bg-white/20 even:bg-white/10 transition-all duration-300 hover:bg-white/30 border-b border-white/20">

                           <td className="p-4">{member.memberId}</td>

                           <td className="p-4">
                              {editingMemberId === member.memberId ? (
                                 <input
                                    type="text"
                                    value={editedMember.name}
                                    onChange={(e) => handleInputChange(e, "name")}
                                    className="border border-white/30 bg-black/40 rounded p-2 text-white focus:outline-none focus:ring focus:ring-indigo-500"
                                 />
                              ) : (
                                 member.name
                              )}
                           </td>

                           <td className="p-4">
                              {editingMemberId === member.memberId ? (
                                 <input
                                    type="text"
                                    value={editedMember.email}
                                    onChange={(e) => handleInputChange(e, "email")}
                                    className="border border-white/30 bg-black/40 rounded p-2 text-white focus:outline-none focus:ring focus:ring-indigo-500"
                                 />
                              ) : (
                                 member.email
                              )}
                           </td>

                           <td className="p-4">
                              {editingMemberId === member.memberId ? (
                                 <input
                                    type="text"
                                    value={editedMember.phoneNumber}
                                    onChange={(e) => handleInputChange(e, "phoneNumber")}
                                    className="border border-white/30 bg-black/40 rounded p-2 text-white focus:outline-none focus:ring focus:ring-indigo-500"
                                 />
                              ) : (
                                 member.phoneNumber
                              )}
                           </td>

                           {userRole.toUpperCase() === "ADMIN" && (
                              <td className="p-4 flex justify-center space-x-2">
                                 {editingMemberId === member.memberId ? (
                                    <>
                                       <button onClick={handleUpdateMember}
                                          className="bg-green-500 px-4 py-2 rounded-lg shadow-md transition-all duration-300 hover:bg-green-600 hover:scale-105">
                                          Save
                                       </button>
                                       <button onClick={handleCancelEdit}
                                          className="bg-gray-500 px-4 py-2 rounded-lg shadow-md transition-all duration-300 hover:bg-gray-600 hover:scale-105">
                                          Cancel
                                       </button>
                                    </>
                                 ) : (
                                    <button onClick={() => handleEditClick(member)}
                                       className="bg-blue-500 px-4 py-2 rounded-lg shadow-md transition-all duration-300 hover:bg-blue-600 hover:scale-105">
                                       Edit
                                    </button>
                                 )}
                                 <button onClick={() => handleDeleteMember(member.memberId)}
                                    className="bg-red-500 px-4 py-2 rounded-lg shadow-md transition-all duration-300 hover:bg-red-600 hover:scale-105">
                                    Delete
                                 </button>
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
