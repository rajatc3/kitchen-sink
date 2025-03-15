import { useState } from "react";
import Header from "./Header";
import Footer from "../pages/Footer"

const Home = () => {
   const userEmail = localStorage.getItem("userEmail");
   const userRole = localStorage.getItem("userRole")?.toUpperCase();

   return (
      <div className="min-h-screen flex flex-col items-center text-white p-6 bg-black relative overflow-hidden pt-20">
         {/* Noise overlay effect */}
         <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/noisy-net.png')] opacity-30"></div>
         <Header userEmail={userEmail} userRole={userRole} />
         <div className="flex w-full max-w-6xl mt-6 gap-6 relative z-10">
            <SidebarLeft userEmail={userEmail} userRole={userRole} />
            <div className="w-full max-w-2xl h-[80vh] overflow-y-auto mx-auto">
               <CreatePostForm />
               <PostList />
            </div>
            <SidebarRight />
         </div>
         <Footer />
      </div>
   );
};

const SidebarLeft = ({ userEmail, userRole }) => (
   <div className="w-1/5 bg-black bg-opacity-50 p-3 rounded-lg shadow-lg hidden md:block fixed left-6 top-24 h-[80vh] flex flex-col text-sm text-gray-200 backdrop-blur-md">
      
      {/* User Info */}
      <div>
         <h2 className="text-md font-semibold border-b border-gray-500 pb-2">👤 User Info</h2>
         <p className="mt-2 text-gray-300 truncate">📧 {userEmail}</p>
         <p className="text-gray-300">🔹 Role: {userRole.toLowerCase() === "admin" ? "Admin 👑" : "User 🙋"}</p>
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

const SidebarRight = () => (
   <div className="w-1/5 bg-black bg-opacity-50 p-3 rounded-lg shadow-lg hidden md:block fixed right-6 top-24 h-[80vh] flex flex-col text-sm text-gray-200 backdrop-blur-md">
   {/* Top: Trending Topics */}
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

const CreatePostForm = () => {
   const [content, setContent] = useState("");

   const handleSubmit = (e) => {
      e.preventDefault();
      if (content.trim() === "") return;
      console.log("New Post:", content);
      setContent("");
   };

   return (
      <div className="bg-gray-900 p-4 rounded-xl shadow-md">
         <textarea
            className="w-full p-2 bg-gray-700 rounded-md text-white"
            rows="3"
            placeholder="🤔 What's on your mind?"
            value={content}
            onChange={(e) => setContent(e.target.value)}
         ></textarea>
         <button
            onClick={handleSubmit}
            className="mt-2 bg-blue-500 px-4 py-2 rounded-full hover:bg-blue-600 text-sm font-semibold"
         >📢 Post</button>
      </div>
   );
};

const PostList = () => {
   const dummyPosts = [
      { id: 1, author: "John Doe", content: "This is my first post!", comments: [{ user: "Alice", text: "Nice! 👍" }, { user: "Bob", text: "Cool post 😎" }] },
      { id: 3, author: "John Doe", content: "This is my first post!", comments: [{ user: "Alice", text: "Nice! 👍" }, { user: "Bob", text: "Cool post 😎" }] },
      { id: 2, author: "Jane Doe", content: "Loving this platform!", comments: [{ user: "Charlie", text: "Agreed! 🔥" }] }
   ];

   return (
      <div className="mt-6 space-y-4">
         {dummyPosts.map(post => <Post key={post.id} post={post} />)}
      </div>
   );
};

const Post = ({ post }) => {
   const [comment, setComment] = useState("");

   const handleCommentSubmit = (e) => {
      e.preventDefault();
      if (comment.trim() === "") return;
      console.log(`New Comment on Post ${post.id}:`, comment);
      setComment("");
   };

   return (
      <div className="bg-gray-900 p-4 rounded-xl shadow-md">
         <div className="flex items-center space-x-2">
            <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center font-bold text-white">
               {post.author[0]}
            </div>
            <h3 className="text-lg font-bold">{post.author}</h3>
         </div>
         <p className="mt-2 text-gray-300">{post.content}</p>
         <div className="mt-4 space-y-2">
            {post.comments.map((c, index) => (
               <div key={index} className="flex items-center space-x-2 text-sm text-gray-400">
                  <div className="w-6 h-6 bg-gray-600 rounded-full flex items-center justify-center text-xs font-bold text-white">
                     {c.user[0]}
                  </div>
                  <p><span className="font-bold text-gray-200">{c.user}</span>: {c.text}</p>
               </div>
            ))}
         </div>
         <form onSubmit={handleCommentSubmit} className="mt-3 flex space-x-2">
            <input
               type="text"
               className="flex-1 p-2 bg-gray-700 rounded-full text-sm text-white"
               placeholder="💬 Write a comment..."
               value={comment}
               onChange={(e) => setComment(e.target.value)}
            />
            <button
               type="submit"
               className="bg-green-500 px-4 py-2 rounded-full hover:bg-green-600 text-sm font-semibold"
            >💬 Comment</button>
         </form>
      </div>
   );
};

export default Home;