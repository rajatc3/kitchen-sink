const Footer = () => {
   return (
       <footer className="fixed bottom-0 w-full bg-black bg-opacity-50 text-gray-300 text-center py-3 text-sm">
           <div className="flex flex-col md:flex-row justify-center md:justify-between items-center px-6">
               <p>© {new Date().getFullYear()} Kitchen Sink Application</p>
               <div className="flex space-x-4 mt-2 md:mt-0">
                   <a href="#" className="hover:text-blue-400">🐦 Twitter</a>
                   <a href="#" className="hover:text-blue-400">💼 LinkedIn</a>
                   <a href="#" className="hover:text-blue-400">🐙 GitHub</a>
               </div>
           </div>
       </footer>
   );
};

export default Footer;
