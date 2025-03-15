import { useState, useEffect } from "react";
import { fetchPosts, createPost, addComment } from "../api/home";
import Header from "./Header";
import Footer from "../pages/Footer";

const Home = () => {
   const userEmail = localStorage.getItem("userEmail");
   const userRole = localStorage.getItem("userRole")?.toUpperCase();
   const token = localStorage.getItem("accessToken");
   const memberId = localStorage.getItem("memberId");
   const firstName = localStorage.getItem("firstName");
   const lastName = localStorage.getItem("lastName");
   const phoneNumber = localStorage.getItem("firstName");

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
         <Header userEmail={userEmail} userRole={userRole} firstName={firstName} lastName={lastName} phoneNumber={phoneNumber} />
         <div className="flex w-full max-w-6xl mt-6 gap-6 relative z-10">
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

const SidebarLeft = ({ userEmail, userRole, firstName, lastName, phoneNumber }) => (
   <div className="w-1/5 bg-black bg-opacity-50 p-3 rounded-lg shadow-lg hidden md:block fixed left-6 top-24 h-[80vh] flex flex-col text-sm text-gray-200 backdrop-blur-md">

      {/* User Info */}
      <div>
         <h2 className="text-md font-semibold border-b border-gray-500 pb-2">👤 Profile</h2>
         <p className="mt-2 text-gray-300 truncate">📧 Email: {userEmail}</p>
         <p className="text-gray-300">🧑‍💼 Name: {firstName} {lastName}</p>
         <p className="text-gray-300">📞 Phone: {phoneNumber}</p>
         {userRole.toLowerCase() === "admin" && (
            <p className="text-gray-300">🎩 Role: Admin 👑</p>
         )}
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
      <div className="bg-gray-900 p-6 rounded-xl shadow-lg space-y-4 border border-gray-700">
         <h2 className="text-lg font-semibold text-white">Create a New Post</h2>
         <input
            className="w-full p-3 bg-gray-800 rounded-lg text-white text-lg font-medium focus:outline-none focus:ring-2 focus:ring-blue-500"
            type="text"
            placeholder="Enter your post title..."
            value={title}
            onChange={(e) => setTitle(e.target.value)}
         />
         <textarea
            className="w-full p-3 bg-gray-800 rounded-lg text-white text-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows="4"
            placeholder="Share your thoughts..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
         ></textarea>
         <button
            onClick={handleSubmit}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 rounded-lg transition-all duration-200"
         >📢 Publish</button>
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

   const handleCommentSubmit = async (e) => {
      e.preventDefault();
      if (comment.trim() === "") return;
      try {
         const newComment = await addComment(token, post.id, { content: comment });
         setPosts(prevPosts => prevPosts.map(p => p.id === post.id ? { ...p, comments: [...p.comments, newComment] } : p));
         setComment("");
      } catch (error) {
         console.error("Error adding comment:", error);
      }
   };

   return (
      <div className="bg-gray-900 p-4 rounded-xl shadow-md">
         <h3 className="text-lg font-bold">{post.title}</h3>
         <p className="mt-2 text-gray-300">{post.content}</p>
         <div className="mt-4 space-y-2">
            {post.comments.map((c) => (
               <div key={c.id} className="text-sm text-gray-400">
                  <p><span className="font-bold text-gray-200">💬 {c.author || "Anonymous"}</span>: {c.content}</p>
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