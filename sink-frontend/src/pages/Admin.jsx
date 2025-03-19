import React, { useEffect, useState } from 'react';
import { fetchUsers, fetchAnalytics, markAdmin } from '../api/admin';
import Header from '../pages/Header';
import Footer from '../pages/Footer';
import { toast, ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

const Admin = () => {
   const [users, setUsers] = useState([]);
   const [analytics, setAnalytics] = useState(null);
   const [currentPage, setCurrentPage] = useState(0);
   const [totalPages, setTotalPages] = useState(1);
   const [pageSize, setPageSize] = useState(5);
   const [selectedUser, setSelectedUser] = useState(null);

   const token = localStorage.getItem('accessToken');

   const loadData = async (page) => {
      const userData = await fetchUsers(token, page, pageSize);
      const analyticsData = await fetchAnalytics(token);

      if (userData) {
         setUsers(userData.content);
         setCurrentPage(userData.currentPage);
         setTotalPages(userData.totalPages);
      }

      if (analyticsData) {
         setAnalytics(analyticsData);
      }
   };

   useEffect(() => {
      loadData(currentPage);
   }, [currentPage, pageSize]);

   const handlePageChange = (newPage) => {
      if (newPage >= 0 && newPage < totalPages) {
         setCurrentPage(newPage);
      }
   };

   return (
      <div className="min-h-screen flex flex-col items-center text-white p-6 bg-black pt-20">
         <Header />
         <div className="flex w-full max-w-6xl mt-6 gap-4">
            {analytics && (
               <div className="w-1/4 bg-gray-800 p-4 rounded-xl shadow-md text-sm h-full">
                  <h2 className="text-lg font-semibold mb-2">Analytics</h2>
                  <p>Total Users: <span className="font-bold">{analytics.totalUsers}</span></p>
                  <p>Total Posts: <span className="font-bold">{analytics.totalPosts}</span></p>
                  <br></br>
                  <p><span className="font-bold">Trending Post:</span></p>
                  {analytics.topPost && (
                     <div className="mt-2 p-2 bg-gray-700 rounded-lg">
                        <p className="font-bold">{analytics.topPost.postTitle}</p>
                        <p className="text-gray-400 text-xs">By {analytics.topPost.member}</p>
                        <p className="text-gray-300 text-xs">{analytics.topPost.totalComments} Comments</p>
                     </div>
                  )}
               </div>
            )}

            <div className="w-3/4 flex flex-col gap-4">
               <div className="bg-gray-800 p-4 rounded-xl shadow-md">
                  <h2 className="text-lg font-semibold text-center mb-2">Users</h2>
                  <div className="overflow-x-auto text-sm">
                     <table className="w-full border-collapse rounded-lg overflow-hidden shadow-lg">
                        <thead>
                           <tr className="bg-gray-800 text-white text-left">
                              <th className="px-4 py-3">Name</th>
                              <th className="px-4 py-3">Email</th>
                              <th className="px-4 py-3">Phone</th>
                              <th className="px-4 py-3">Role</th>
                              <th className="px-4 py-3 text-center">Action</th>
                           </tr>
                        </thead>
                        <tbody>
                           {users.length > 0 ? (
                              users.map((user) => (
                                 <tr
                                    key={user.memberId}
                                    onClick={(event) => {
                                       if (event.target.tagName !== "BUTTON") {
                                          setSelectedUser(user);
                                       }
                                    }}
                                    className="border-b border-gray-700 hover:bg-gray-700 transition"
                                 >
                                    <td className="px-4 py-3">{user.firstName} {user.lastName}</td>
                                    <td className="px-4 py-3 text-gray-300">{user.email}</td>
                                    <td className="px-4 py-3">{user.phoneNumber}</td>
                                    <td className="px-4 py-3 font-semibold capitalize">{user.userRole}</td>
                                    {/* Wrap the button inside its own <td> and stop propagation here */}
                                    <td
                                       className="px-4 py-3 text-center"
                                       onClick={(event) => event.stopPropagation()} // ✅ Stops tr click here
                                    >
                                       {user.userRole !== "admin" && (
                                          <button
                                             onClick={async () => {
                                                const result = await markAdmin(token, user.username);
                                                if (result) {
                                                   toast.success(`${user.username} is now an admin`, { position: "top-right" });
                                                   loadData(currentPage);
                                                }
                                             }}
                                             className="px-3 py-1 bg-blue-500 text-white rounded-lg hover:bg-blue-400 transition shadow-md"
                                          >
                                             Make Admin
                                          </button>
                                       )}
                                    </td>
                                 </tr>
                              ))
                           ) : (
                              <tr>
                                 <td colSpan="5" className="text-center py-4">No users found</td>
                              </tr>
                           )}
                        </tbody>
                     </table>
                  </div>
                  <div className="flex justify-between items-center mt-3">
                     <button
                        onClick={() => handlePageChange(currentPage - 1)}
                        disabled={currentPage === 0}
                        className="px-3 py-1 bg-gray-700 rounded-lg hover:bg-gray-600 transition disabled:opacity-50"
                     >
                        Previous
                     </button>
                     <span className="text-gray-300">Page {currentPage + 1} of {totalPages}</span>
                     <button
                        onClick={() => handlePageChange(currentPage + 1)}
                        disabled={currentPage + 1 >= totalPages}
                        className="px-3 py-1 bg-gray-700 rounded-lg hover:bg-gray-600 transition disabled:opacity-50"
                     >
                        Next
                     </button>
                  </div>
               </div>

               {selectedUser && (
                  <div className="bg-gray-800 p-4 rounded-xl shadow-md">
                     <h2 className="text-lg font-semibold text-center mb-2">{selectedUser.firstName} {selectedUser.lastName}'s Latest Posts</h2>

                     {analytics.members.find(member => member.username === selectedUser?.username)?.posts.slice(0, 5).length > 0 ? (
                        <ul className="space-y-2">
                           {analytics.members.find(member => member.username === selectedUser?.username)?.posts.slice(0, 5).map((post, index) => (
                              <li key={index} className="p-3 bg-gray-700 rounded-md">
                                 <span className="font-bold">{post.postTitle}</span> - {post.totalComments} comments
                              </li>
                           ))}
                        </ul>
                     ) : (
                        <p className="text-gray-500 text-center">No posts available</p>
                     )}

                  </div>
               )}
            </div>
         </div>
         <ToastContainer />
         <Footer />
      </div>
   );
};

export default Admin;
