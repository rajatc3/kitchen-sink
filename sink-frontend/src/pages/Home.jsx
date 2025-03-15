import { useState, useEffect } from "react";
import { fetchPosts, createPost, addComment, deletePost, deleteComment } from "../api/home";
import { FaTrash } from "react-icons/fa";
import Header from "./Header";
import Footer from "../pages/Footer";
import { format } from "date-fns";
import { QRCodeCanvas } from "qrcode.react";

const Home = () => {
   const userEmail = localStorage.getItem("userEmail");
   const userRole = localStorage.getItem("userRole")?.toUpperCase();
   const token = localStorage.getItem("accessToken");
   const firstName = localStorage.getItem("firstName");
   const lastName = localStorage.getItem("lastName");
   const phoneNumber = localStorage.getItem("phoneNumber");

   const [posts, setPosts] = useState([]);
   const [loading, setLoading] = useState(true);

   useEffect(() => {
      const loadPosts = async () => {
         try {
            const data = await fetchPosts(token);
            setPosts(data);
         } catch (error) {
            console.error("Error fetching posts:", error);
         } finally {
            setLoading(false);
         }
      };
      loadPosts();
   }, [token]);

   return (
      <div className="min-h-screen flex flex-col items-center text-white p-6 bg-black relative overflow-hidden pt-20">
         <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/noisy-net.png')] opacity-30"></div>
         <Header />
         <div className="flex flex-wrap w-full max-w-6xl mt-6 gap-6 relative z-10">
            <SidebarLeft userEmail={userEmail} userRole={userRole} firstName={firstName} lastName={lastName} phoneNumber={phoneNumber} />
            <div className="w-full max-w-2xl h-[80vh] overflow-y-auto mx-auto scrollbar-hide">
               <CreatePostForm token={token} setPosts={setPosts} />
               {loading ? <p>Loading posts...</p> : <PostList posts={posts} token={token} setPosts={setPosts} />}
            </div>
            <SidebarRight />
         </div>
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
            <h2 className="text-md font-semibold border-t border-gray-500 pt-2 ">📊 Stats for Nerds</h2>
            <ul className="mt-3 text-gray-300 space-y-2">
               <li>📝 Total Posts: 12</li>
               <li>💬 Comments Made: 45</li>
               <li>👥 Total Signed Up Users: 350</li>
            </ul>
         </div>
      </div>
   );
};

const SidebarRight = () => (
   <div className="w-1/5 bg-black bg-opacity-50 p-3 rounded-lg shadow-lg hidden md:block fixed right-6 top-24 h-[80vh] flex flex-col text-sm text-gray-200 backdrop-blur-md">
      <div>
         <h2 className="text-md font-semibold border-b border-gray-500 pb-2">🔥 Trending Topics</h2>
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

const CreatePostForm = ({ token, setPosts }) => {
   const [title, setTitle] = useState("");
   const [content, setContent] = useState("");

   const handleSubmit = async (e) => {
      e.preventDefault();
      if (title.trim() === "" || content.trim() === "") return;
      try {
         const newPost = await createPost(token, { title, content });
         setPosts((prevPosts) => [newPost, ...prevPosts]);
         setTitle("");
         setContent("");
      } catch (error) {
         console.error("Error creating post:", error);
      }
   };

   return (
      <div className="bg-gray-900 p-3 rounded-lg shadow-md border border-gray-700 max-w-2xl mx-auto">
         <h2 className="text-md font-semibold text-gray-200 mb-2">📝 New Post</h2>
         
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


const PostList = ({ posts, token, setPosts }) => (
   <div className="mt-6 space-y-4">
      {posts.map(post => <Post key={post.id} post={post} token={token} setPosts={setPosts} />)}
   </div>
);

const Post = ({ post, token, setPosts }) => {
   const [comment, setComment] = useState("");
   const loggedInMemberId = localStorage.getItem("memberId");

   const handleDeletePost = async () => {
      try {
         await deletePost(token, post.id);
         setPosts((prevPosts) => prevPosts.filter((p) => p.id !== post.id)); // Remove post
      } catch (error) {
         console.error("Error deleting post:", error);
      }
   };

   const handleDeleteComment = async (commentId) => {
      try {
         await deleteComment(token, commentId);
         setPosts((prevPosts) =>
            prevPosts.map((p) =>
               p.id === post.id ? { ...p, comments: p.comments.filter((c) => c.id !== commentId) } : p
            )
         ); // Remove comment
      } catch (error) {
         console.error("Error deleting comment:", error);
      }
   };

   const handleCommentSubmit = async (e) => {
      e.preventDefault();
      if (comment.trim() === "") return;
      try {
         const newComment = await addComment(token, post.id, { content: comment });
         setPosts((prevPosts) =>
            prevPosts.map((p) =>
               p.id === post.id ? { ...p, comments: [...p.comments, newComment] } : p
            )
         );
         setComment("");
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

   return (
      <div className="bg-gray-900 p-6 rounded-xl shadow-lg text-white w-full max-w-2xl mx-auto mb-6">
         {/* Post Header */}
         <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
               <div className="h-12 w-12 flex items-center justify-center bg-blue-500 text-white rounded-full text-lg font-bold uppercase">
                  {getInitials(post.member.firstName + " " + post.member.lastName)}
               </div>
               <div>
                  <h3 className="text-lg font-bold">
                     {post.member.firstName + " " + post.member.lastName}
                     {post.member.userRole === "admin" && " 👑"}
                  </h3>
                  <p className="text-gray-400 text-sm">{formatDate(post.createdAt)}</p>
               </div>
            </div>
            {/* Delete Post Icon (Only for the owner) */}
            {loggedInMemberId == post.member.memberId && (
               <button onClick={handleDeletePost} className="text-red-500 hover:text-red-700">
                  <FaTrash />
               </button>
            )}
         </div>

         {/* Post Content */}
         <h2 className="mt-4 text-xl font-semibold">{post.title}</h2>
         <p className="mt-2 text-gray-300">{post.content}</p>

         {/* Comments Section */}
         <div className="mt-4 space-y-2 border-t border-gray-700 pt-4">
            {post.comments.map((c) => (
               <div key={c.id} className="text-sm text-gray-400 flex justify-between items-center">
                  <div className="flex items-center space-x-2">
                     {/* Avatar with Initials */}
                     <div className="h-6 w-6 flex items-center justify-center bg-blue-500 text-white rounded-full text-xs font-bold uppercase">
                        {getInitials(c.member.firstName + " " + c.member.lastName)}
                     </div>
                     {/* Name and Role */}
                     <p className="flex items-center">
                        <span className="font-bold text-gray-200">
                           {c.member.firstName} {c.member.lastName}
                        </span>
                        {c.member.userRole === "admin" && " 👑"}: {c.content}
                     </p>
                  </div>
                  <div className="flex items-center space-x-2">
                     <p className="text-xs text-gray-500">{formatDate(c.createdAt)}</p>
                     {/* Delete Comment Icon (Only for the owner) */}
                     {loggedInMemberId == c.member.memberId && (
                        <button onClick={() => handleDeleteComment(c.id)} className="text-red-500 hover:text-red-700">
                           <FaTrash />
                        </button>
                     )}
                  </div>
               </div>
            ))}
         </div>

         {/* Comment Input */}
         <form onSubmit={handleCommentSubmit} className="mt-3 flex space-x-2">
            <input
               type="text"
               className="flex-1 p-2 bg-gray-700 rounded-full text-sm text-white border border-gray-600 focus:ring-2 focus:ring-blue-500 outline-none"
               placeholder="💬 Write a comment..."
               value={comment}
               onChange={(e) => setComment(e.target.value)}
            />
            <button
               type="submit"
               className="bg-green-500 px-4 py-2 rounded-full hover:bg-green-600 text-sm font-semibold transition-all"
            >
               💬 Comment
            </button>
         </form>
      </div>
   );
};

export default Home;