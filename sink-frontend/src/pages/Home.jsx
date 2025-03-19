import { useState, useEffect } from "react";
import { fetchPosts, createPost, addComment, deletePost, deleteComment } from "../api/home";
import { FaTrash } from "react-icons/fa";
import Header from "./Header";
import Footer from "./Footer";
import { format } from "date-fns";
import { QRCodeCanvas } from "qrcode.react";
import { Search, ArrowUp, ArrowDown } from "lucide-react";

const size = 5;

const Home = () => {
   const userEmail = localStorage.getItem("userEmail");
   const token = localStorage.getItem("accessToken");
   const firstName = localStorage.getItem("firstName");
   const lastName = localStorage.getItem("lastName");
   const phoneNumber = localStorage.getItem("phoneNumber");
   const userRole = localStorage.getItem("userRole")?.toUpperCase();

   const [loading, setLoading] = useState(true);
   const [currentPage, setCurrentPage] = useState(0);
   const [sortBy, setSortBy] = useState("createdAt");
   const [sortOrder, setSortOrder] = useState("desc");
   const [posts, setPosts] = useState([]);
   const [totalPages, setTotalPages] = useState(1);

   useEffect(() => {
      const loadPosts = async () => {
         try {
            setLoading(true);
            const data = await fetchPosts(token, currentPage, size, sortBy, sortOrder);
            setPosts(data.content);
            setTotalPages(data.totalPages);
         } catch (error) {
            console.error("Error fetching posts:", error);
         } finally {
            setLoading(false);
         }
      };
      loadPosts();
   }, [token, currentPage, sortBy, sortOrder]);

   return (
      <div className="min-h-screen flex flex-col items-center text-white p-6 bg-black pt-20">
         <Header />
         <div className="flex flex-wrap w-full max-w-6xl mt-6 gap-6">
            <SidebarLeft userEmail={userEmail} userRole={userRole} firstName={firstName} lastName={lastName} phoneNumber={phoneNumber} />
            <div id="posts-container" className="w-full max-w-4xl h-[80vh] overflow-y-auto mx-auto scrollbar-hide">
               <div className="space-y-6">
                  <CreatePostForm token={token} setPosts={setPosts} currentPage={currentPage} sortBy={sortBy} sortOrder={sortOrder} />
                  {loading ? <p>Loading posts...</p> : <PostList posts={posts} setPosts={setPosts} currentPage={currentPage} setCurrentPage={setCurrentPage} totalPages={totalPages} sortBy={sortBy}
                     setSortBy={setSortBy} sortOrder={sortOrder} setSortOrder={setSortOrder} token={token} userRole={userRole} />}
               </div>
            </div>
            <SidebarRight />
         </div>
         <ScrollButton />
         <Footer />
      </div>
   );
};

const SidebarLeft = ({ userEmail, userRole, firstName, lastName, phoneNumber }) => {
   const qrData = JSON.stringify({ email: userEmail, name: `${firstName} ${lastName}`, phone: phoneNumber });

   return (
      <div className="w-1/5 bg-black bg-opacity-50 p-3 rounded-lg shadow-lg hidden md:block fixed left-6 top-24 h-[80vh] flex flex-col text-sm text-gray-200 backdrop-blur-md">

         {/* User Info + QR Code */}
         <h2 className="text-md font-semibold border-b border-gray-500 pb-2">👤 Profile</h2>
         <div className="flex justify-between items-center">
            <div>
               <p className="mt-2 text-gray-300 truncate">📧 Email: {userEmail}</p>
               <p className="text-gray-300">🧑‍💼 Name: {firstName} {lastName}</p>
               <p className="text-gray-300">📞 Contact: {phoneNumber}</p>
               {userRole.toLowerCase() === "admin" && (
                  <p className="text-gray-300">🎩 Role: Admin 👑</p>
               )}
            </div>

            {/* QR Code */}
            <div className="p-1 bg-white rounded-md shadow-md">
               <QRCodeCanvas value={qrData} size={60} fgColor="#ffffff" bgColor="#1a1a1a" />
            </div>
         </div>

         {/* Stats for Nerds */}
         <div className="mt-6">
            <h2 className="text-md font-semibold border-t border-gray-500 pt-2 ">📊 Stats for Nerds (Placeholder)</h2>
            <ul className="mt-3 text-gray-300 space-y-2">
               <li>📝 Your Post Count: <b>12</b></li>
               <li>💬 Comments on your posts: <b>45</b></li>
               <li>💬 Most engaged with: <b>John Doe</b></li>
            </ul>
         </div>
      </div>
   );
};

const SidebarRight = () => (
   <div className="w-1/5 bg-black bg-opacity-50 p-3 rounded-lg shadow-lg hidden md:block fixed right-6 top-24 h-[80vh] flex flex-col text-sm text-gray-200 backdrop-blur-md">
      <div>
         <h2 className="text-md font-semibold border-b border-gray-500 pb-2">🔥 Trending Topics (Placeholder)</h2>
         <ul className="mt-3 text-gray-300 space-y-2">
            <li className="hover:text-white transition">🚀 #TechNews</li>
            <li className="hover:text-white transition">💻 #ReactJS</li>
            <li className="hover:text-white transition">🌍 #WebDevelopment</li>
         </ul>
      </div>

      {/* Pushes bottom section to the actual bottom with spacing */}
      <div className="flex-grow mb-4"></div>

      {/* Bottom: Resources */}
      <div>
         <h2 className="text-md font-semibold border-t border-gray-500 pt-3 pt-2">📚 Resources</h2>
         <ul className="mt-3 text-blue-400 space-y-2">
            <li className="hover:text-blue-300 transition"><a href="#">📖 Docs</a></li>
            <li className="hover:text-blue-300 transition"><a href="#">🌐 Community</a></li>
            <li className="hover:text-blue-300 transition"><a href="#">🔧 Support</a></li>
         </ul>
      </div>
   </div>
);

const CreatePostForm = ({ token, setPosts, currentPage, sortBy, sortOrder }) => {
   const [title, setTitle] = useState("");
   const [content, setContent] = useState("");
   const [message, setMessage] = useState(null);

   const handleSubmit = async (e) => {
      e.preventDefault();
      if (title.trim() === "") {
         setMessage({ text: "Title cannot be empty!", type: "error" });
         return;
      }
      try {
         await createPost(token, { title, content });
         const updatedPosts = await fetchPosts(token, currentPage, size, sortBy, sortOrder);
         setPosts(updatedPosts.content);
         setTitle("");
         setContent("");
         setMessage({ text: "Post created successfully!", type: "success" });
         setTimeout(() => setMessage(null), 3000);
      } catch (error) {
         setMessage({ text: "Failed to create post. Try again!", type: "error" });
         console.error("Error creating post:", error);
      }
   };

   return (
      <div className="bg-gray-900 p-3 rounded-lg shadow-md border border-gray-700 max-w-2xl mx-auto">
         <h2 className="text-md font-semibold text-gray-200 mb-2">📝 New Post</h2>

         {/* 🔹 Notification Banner */}
         {message && (
            <div
               className={`flex items-center justify-between p-3 mb-2 text-sm rounded-lg shadow-md 
         ${message.type === "success" ? "bg-green-500 text-white" : "bg-red-500 text-white"} 
         animate-fadeIn transition-all duration-300`}
            >
               {/* Icon */}
               <span className="flex items-center space-x-2">
                  {message.type === "success" ? (
                     <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                     </svg>
                  ) : (
                     <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                     </svg>
                  )}
                  <span>{message.text}</span>
               </span>

               {/* Close Button */}
               <button
                  onClick={() => setMessage(null)}
                  className="text-white hover:text-gray-200 transition"
               >
                  ✖
               </button>
            </div>
         )}

         <div className="flex gap-2">
            <input
               className="flex-1 p-2 bg-gray-800 rounded-md text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-400"
               type="text"
               placeholder="Post title..."
               value={title}
               onChange={(e) => setTitle(e.target.value)}
            />
            <button
               onClick={handleSubmit}
               className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold px-4 py-2 rounded-md transition-all duration-200"
            >
               📢 Post
            </button>
         </div>

         <textarea
            className="w-full mt-2 p-2 bg-gray-800 rounded-md text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-400"
            rows="2"
            placeholder="Share your thoughts..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
         ></textarea>
      </div>
   );
};


const PostList = ({ posts, setPosts, currentPage, setCurrentPage, totalPages, sortBy, setSortBy, sortOrder, setSortOrder, token, userRole }) => {
   const [searchQuery, setSearchQuery] = useState("");
   const [filteredPosts, setFilteredPosts] = useState([]);

   useEffect(() => {
      setFilteredPosts(posts);
   }, [posts]);

   const handleSearch = (e) => {
      const query = e.target.value.toLowerCase();
      setSearchQuery(query);
      setFilteredPosts(query ? posts.filter(post =>
         post.title.toLowerCase().includes(query) ||
         post.content.toLowerCase().includes(query) ||
         (post.member?.firstName?.toLowerCase().includes(query) || post.member?.lastName?.toLowerCase().includes(query))
      ) : posts);
   };

   const handleSortChange = (newSortBy) => {
      if (sortBy === newSortBy) {
         setSortOrder(sortOrder === "asc" ? "desc" : "asc");
      } else {
         setSortBy(newSortBy);
         setSortOrder("asc");
      }
   };

   return (
      <div className="w-full max-w-2xl mx-auto">
         <div className="flex items-center justify-between space-x-3 mb-4">
            <div className="flex items-center bg-gray-800 px-4 py-2 rounded-lg flex-grow">
               <Search className="w-5 h-5 text-gray-400 mr-2" />
               <input type="text" placeholder="Search posts..." value={searchQuery} onChange={handleSearch} className="w-full bg-transparent focus:outline-none text-sm text-white" />
            </div>
            {[{ label: "Date", field: "createdAt" }, { label: "Title", field: "title" }].map(({ label, field }) => (
               <button key={field} onClick={() => handleSortChange(field)} className={`flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-lg shadow-md ${sortBy === field ? "bg-blue-600 text-white" : "bg-gray-800 text-gray-300 hover:bg-gray-700"}`}>
                  {label}
                  {sortBy === field && (sortOrder === "asc" ? <ArrowUp size={16} /> : <ArrowDown size={16} />)}
               </button>
            ))}
         </div>
         {filteredPosts.length > 0 ? (
            filteredPosts.map((post) => <Post key={post.id} post={post} token={token} setPosts={setPosts} currentPage={currentPage} userRole={userRole} />)
         ) : (
            <p className="text-white text-center">No posts available.</p>
         )}
         <div className="flex justify-between mt-4">
            <button
               onClick={() => setCurrentPage(prev => Math.max(prev - 1, 0))}
               disabled={currentPage === 0}
               className="px-4 py-2 bg-gray-700 text-white rounded disabled:opacity-50"
            >
               ⬅️ Previous
            </button>

            <span className="text-white">Page {currentPage + 1} of {totalPages === 0 ? 1 : totalPages}</span>

            <button
               onClick={() => {
                  if (posts.length > 0) {
                     setCurrentPage(prev => (prev < totalPages - 1 ? prev + 1 : prev));
                  }
               }}
               disabled={currentPage >= totalPages - 1 || posts.length === 0}
               className="px-4 py-2 bg-gray-700 text-white rounded disabled:opacity-50"
            >
               Next ➡️
            </button>
         </div>
      </div>


   );
};

const Post = ({ post, token, setPosts, currentPage, userRole }) => {
   const [comment, setComment] = useState("");
   const [commentToDelete, setCommentToDelete] = useState(null);
   const loggedInMemberId = localStorage.getItem("memberId");
   const [postToDelete, setPostToDelete] = useState(null);

   const handleDeletePost = async () => {
      if (!postToDelete) return;

      try {
         await deletePost(token, postToDelete);

         setPosts(prev => {
            const updatedPosts = prev.filter(post => post.id !== postToDelete);

            if (updatedPosts.length === 0 && currentPage > 0) {
               setCurrentPage(prevPage => prevPage - 1); // Move to previous page if empty
            }

            return updatedPosts;
         });

         const newPosts = await fetchPosts(token, currentPage, size, sortBy, sortOrder);
         setPosts(newPosts.content);
         setTotalPages(newPosts.totalPages);
      } catch (error) {
         console.error("Error deleting post:", error);
      } finally {
         setPostToDelete(null);
      }
   };

   const handleDeleteComment = async () => {
      if (!commentToDelete) return;
      try {
         await deleteComment(token, commentToDelete);
         setCommentToDelete(null);
         const updatedPosts = await fetchPosts(token, 0, size, "createdAt", "desc");
         setPosts(updatedPosts.content);
      } catch (error) {
         console.error("Error deleting comment:", error);
      }
   };

   const handleCommentSubmit = async (e) => {
      e.preventDefault();
      if (comment.trim() === "") return;

      try {
         const savedComment = await addComment(token, post.id, { content: comment });
         console.log(savedComment);
         setPosts(prevPosts =>
            prevPosts.map(p =>
               p.id === post.id
                  ? { ...p, comments: [...p.comments, savedComment] }
                  : p
            )
         );

         setComment(""); // Clear input field
      } catch (error) {
         console.error("Error adding comment:", error);
      }
   };

   const formatDate = (dateString) => {
      const date = new Date(dateString);
      const today = new Date();
      return date.toDateString() === today.toDateString()
         ? `Today at ${format(date, "HH:mm")}`
         : format(date, "dd MMM yyyy, HH:mm");
   };

   const getInitials = (name) => {
      return name
         .split(" ")
         .map((n) => n[0])
         .join("")
         .toUpperCase();
   };

   const getAvatarColor = (id) => {
      const colors = ["bg-blue-500", "bg-green-500", "bg-red-500", "bg-yellow-500", "bg-purple-500", "bg-pink-500", "bg-indigo-500"];
      const index = id % colors.length;
      return colors[index];
   };

   return (
      <div className="bg-gray-900 p-6 rounded-xl shadow-lg text-white w-full max-w-2xl mx-auto mb-6">
         {/* Post Header */}
         <div className="flex justify-between items-center mb-2">
            <h2 className="text-lg font-bold text-white">{post.title}</h2>
            <div className="flex items-center space-x-3">
               <p className="text-xs text-gray-400">{formatDate(post.createdAt)}</p>
               {/* Delete Post Icon (Top-Right) */}
               {(loggedInMemberId == post.member.memberId || userRole === "ADMIN") && (
                  <button onClick={(e) => {
                     setPostToDelete(post.id);
                  }} className="text-red-500 hover:text-red-700">
                     <FaTrash />
                  </button>
               )}
            </div>
         </div>

         {/* Author Info */}
         <div className="flex items-center space-x-3 mb-4">
            <div className={`h-6 w-6 flex items-center justify-center ${getAvatarColor(post.member.memberId)} text-white rounded-full text-sm font-bold uppercase`}>
               {getInitials(post.member.firstName + " " + post.member.lastName)}
            </div>
            <p className="text-sm text-gray-300">
               By <span className="font-semibold">{post.member.firstName} {post.member.lastName}</span>
               {post.member.userRole === "admin" && " 👑"}
            </p>
         </div>

         {/* Post Content */}
         <p className="mt-2 text-gray-300 text-sm leading-snug">{post.content}</p>

         {/* Separator */}
         <hr className="border-t border-gray-600 my-5" />

         {/* Comments Section */}
         <h3 className="text-lg font-semibold text-gray-300 mb-3">💬 Comments</h3>
         {post.comments && post.comments.length > 0 ? (
            post.comments.map((c) => (
               <div key={c.id} className="bg-gray-800 px-2 py-1 rounded-md flex justify-between items-center mb-1">
                  <div className="flex items-center space-x-3">
                     {/* Avatar with Initials */}
                     <div className={`h-5 w-5 flex items-center justify-center ${getAvatarColor(c.member.memberId)} text-white rounded-full text-xs font-bold uppercase`}>
                        {getInitials(`${c.member.firstName} ${c.member.lastName}`)}
                     </div>

                     {/* Name, Role, and Comment in Single Line */}
                     <p className="text-gray-300 text-xs flex items-center space-x-2">
                        <span className="font-semibold">{c.member.firstName} {c.member.lastName}</span>
                        {c.member.userRole === "admin" && " 👑"}
                        <span className="text-gray-400"> {c.content}</span>
                     </p>
                  </div>

                  {/* Time and Delete Icon */}
                  <div className="flex items-center space-x-2">
                     <p className="text-[10px] text-gray-500">{formatDate(c.createdAt)}</p>
                     {(loggedInMemberId == c.member.memberId || userRole === "ADMIN") && (
                        <button onClick={() => setCommentToDelete(c.id)} className="text-red-500 hover:text-red-700">
                           <FaTrash />
                        </button>
                     )}
                  </div>
               </div>
            ))
         ) : (
            <p className="text-gray-400 italic">No comments yet. Be the first to comment! 🎉</p>
         )}

         {/* Comment Input */}
         <form onSubmit={handleCommentSubmit} className="mt-4 flex space-x-2">
            <input
               type="text"
               className="flex-1 p-2 bg-gray-700 rounded-full text-sm text-white border border-gray-600 focus:ring-2 focus:ring-blue-500 outline-none"
               placeholder="💬 Write a comment..."
               value={comment}
               onChange={(e) => setComment(e.target.value)}
            />
            <button
               type="submit"
               className="bg-green-500 hover:bg-green-600 text-white px-3 py-1.5 rounded-full shadow-md transition-all duration-200 ease-in-out text-xs flex items-center space-x-1">
               <span className="hidden sm:inline">Comment</span>
            </button>
         </form>

         {/* Confirmation Dialog for Deleting Post */}
         {postToDelete && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center">
               <div className="bg-gray-800 p-5 rounded-lg shadow-lg">
                  <p className="text-white text-lg">Are you sure you want to delete this post?</p>
                  <div className="flex justify-end space-x-4 mt-4">
                     <button onClick={() => setPostToDelete(false)} className="px-4 py-2 bg-gray-600 text-white rounded-lg">
                        Cancel
                     </button>
                     <button onClick={handleDeletePost} className="px-4 py-2 bg-red-500 text-white rounded-lg">
                        Delete
                     </button>
                  </div>
               </div>
            </div>
         )}

         {/* Confirmation Dialog for Deleting Comment */}
         {commentToDelete && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center">
               <div className="bg-gray-800 p-5 rounded-lg shadow-lg">
                  <p className="text-white text-lg">Are you sure you want to delete this comment?</p>
                  <div className="flex justify-end space-x-4 mt-4">
                     <button onClick={() => setCommentToDelete(null)} className="px-4 py-2 bg-gray-600 text-white rounded-lg">
                        Cancel
                     </button>
                     <button onClick={handleDeleteComment} className="px-4 py-2 bg-red-500 text-white rounded-lg">
                        Delete
                     </button>
                  </div>
               </div>
            </div>
         )}
      </div>
   );
};

const ScrollButton = () => {
   const [visible, setVisible] = useState(false);
   const postsContainerRef = document.getElementById("posts-container");

   useEffect(() => {
      if (!postsContainerRef) return;

      const toggleVisibility = () => {
         setVisible(postsContainerRef.scrollTop > 100); // Show after scrolling 100px
      };

      postsContainerRef.addEventListener("scroll", toggleVisibility);
      return () => postsContainerRef.removeEventListener("scroll", toggleVisibility);
   }, [postsContainerRef]);

   const scrollToTop = () => {
      postsContainerRef.scrollTo({ top: 0, behavior: "smooth" });
   };

   const scrollToBottom = () => {
      postsContainerRef.scrollTo({ top: postsContainerRef.scrollHeight, behavior: "smooth" });
   };

   return (
      <div className="fixed bottom-16 right-24 flex flex-col space-y-2 z-50">
         {visible && (
            <>
               <button
                  onClick={scrollToTop}
                  className="bg-gray-800 hover:bg-gray-700 text-white p-3 rounded-full shadow-lg transition-all"
                  title="Scroll to Top"
               >
                  🔼
               </button>
               <button
                  onClick={scrollToBottom}
                  className="bg-gray-800 hover:bg-gray-700 text-white p-3 rounded-full shadow-lg transition-all"
                  title="Scroll to Bottom"
               >
                  🔽
               </button>
            </>
         )}
      </div>
   );
};

export default Home;