import React, { useEffect, useState } from "react";
import { fetchUsers, fetchAnalytics } from "../api/admin";
import Header from "../pages/Header";

const Admin = () => {
   const [users, setUsers] = useState([]);
   const [analytics, setAnalytics] = useState(null);

   const userEmail = localStorage.getItem("userEmail");
   const userRole = localStorage.getItem("userRole")?.toUpperCase();

   useEffect(() => {
      const token = localStorage.getItem("accessToken");
      const loadData = async () => {
         const userData = await fetchUsers(token);
         const analyticsData = await fetchAnalytics(token);

         if (analyticsData && userData) {
            const mappedUsers = userData.map(user => {
               const matchingMember = analyticsData.members.find(m =>
                  m.username === user.username
               );
               return {
                  ...user,
                  totalPosts: matchingMember ? matchingMember.totalPosts : 0
               };
            });
            setUsers(mappedUsers);
         } else {
            setUsers(userData || []);
         }
         setAnalytics(analyticsData);
      };
      loadData();
   }, []);

   return (
      <div className="min-h-screen flex flex-col bg-gray-900 text-white"
         style={{
            backgroundImage: "url('https://images.alphacoders.com/114/thumb-1920-1146731.jpg')",
            backgroundSize: "cover",
            backgroundPosition: "center",
         }}>

         <Header />

         <div className="bg-gray-900 bg-opacity-80 min-h-screen flex flex-col items-center justify-center p-10">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 w-full max-w-6xl">
               {/* Analytics Overview */}
               {analytics && (
                  <div className="bg-gray-800 bg-opacity-90 p-6 rounded-2xl shadow-lg text-center backdrop-blur-md">
                     <h2 className="text-2xl font-semibold mb-4">Overview</h2>
                     <p className="text-lg">Total Users: <span className="font-bold">{analytics.totalUsers}</span></p>
                     <p className="text-lg">Total Posts: <span className="font-bold">{analytics.totalPosts}</span></p>
                     <p className="text-lg">Total Comments: <span className="font-bold">{analytics.totalComments}</span></p>
                  </div>
               )}

               {/* Trending Posts */}
               {analytics?.hotPosts && (
                  <div className="bg-gray-800 bg-opacity-90 p-6 rounded-2xl shadow-lg backdrop-blur-md">
                     <h2 className="text-2xl font-semibold mb-4 text-center">Trending Posts</h2>
                     <ul className="text-left space-y-2">
                        {analytics.hotPosts.length > 0 ? (
                           analytics.hotPosts.map((post, index) => (
                              <li key={index} className="p-3 bg-gray-700 rounded-lg hover:bg-gray-600 transition">
                                 <span className="font-bold">{post.title}</span> - {post.author}
                              </li>
                           ))
                        ) : (
                           <p className="text-center text-gray-400">No trending posts</p>
                        )}
                     </ul>
                  </div>
               )}

               {/* User List */}
               <div className="bg-gray-800 bg-opacity-90 p-6 rounded-2xl shadow-lg backdrop-blur-md col-span-2">
                  <h2 className="text-2xl font-semibold mb-4 text-center">Users</h2>
                  <div className="overflow-x-auto">
                     <table className="w-full border-collapse rounded-lg overflow-hidden">
                        <thead>
                           <tr className="bg-gray-700 text-white">
                              <th className="border px-4 py-2">Name</th>
                              <th className="border px-4 py-2">Email</th>
                              <th className="border px-4 py-2">Phone</th>
                              <th className="border px-4 py-2">Total Posts</th>
                           </tr>
                        </thead>
                        <tbody>
                           {users.length > 0 ? (
                              users.map((user, index) => (
                                 <tr key={index} className="border bg-gray-900 hover:bg-gray-700 transition">
                                    <td className="border px-4 py-2">{user.firstName} {user.lastName}</td>
                                    <td className="border px-4 py-2">{user.email}</td>
                                    <td className="border px-4 py-2">{user.phoneNumber}</td>
                                    <td className="border px-4 py-2">{user.totalPosts}</td>
                                 </tr>
                              ))
                           ) : (
                              <tr>
                                 <td colSpan="4" className="text-center py-4 text-gray-400">No users found</td>
                              </tr>
                           )}
                        </tbody>
                     </table>
                  </div>
               </div>
            </div>
         </div>
      </div>
   );
};

export default Admin;
