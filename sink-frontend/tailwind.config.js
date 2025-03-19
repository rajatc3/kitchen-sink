/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    extend: {
      animation: {
        'grid-move': 'gridBG 10s linear infinite',
      },
      keyframes: {
        gridBG: {
          '0%': { backgroundPosition: "0% 0%" },
          '100%': { backgroundPosition: "100% 100%" },
        },
      },
    },
  },
  plugins: [
    function ({ addUtilities }) {
      addUtilities({
        ".scrollbar-hide": {
          "-ms-overflow-style": "none", // IE & Edge
          "scrollbar-width": "none", // Firefox
        },
        ".scrollbar-hide::-webkit-scrollbar": {
          display: "none", // Chrome, Safari
        },
      });
    },
  ],
};
