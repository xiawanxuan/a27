/** @type {import('tailwindcss').Config} */
// TailwindCSS 配置文件
// 主色调：自然绿 #2E7D32
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // 主色：自然绿
        primary: {
          50: '#E8F5E9',
          100: '#C8E6C9',
          200: '#A5D6A7',
          300: '#81C784',
          400: '#66BB6A',
          500: '#2E7D32',
          600: '#2E7D32',
          700: '#27632A',
          800: '#1B5E20',
          900: '#0D3D11',
        },
        // 辅助色1：棕色
        secondary: {
          50: '#EFEBE9',
          100: '#D7CCC8',
          200: '#BCAAA4',
          300: '#A1887F',
          400: '#8D6E63',
          500: '#795548',
          600: '#6D4C41',
          700: '#5D4037',
          800: '#4E342E',
          900: '#3E2723',
        },
        // 辅助色2：浅绿
        'light-green': {
          50: '#F1F8E9',
          100: '#DCEDC8',
          200: '#A5D6A7',
          300: '#AED581',
          400: '#9CCC65',
          500: '#8BC34A',
          600: '#7CB342',
          700: '#689F38',
          800: '#558B2F',
          900: '#33691E',
        },
        // 辅助色3：蓝色
        accent: {
          50: '#E3F2FD',
          100: '#BBDEFB',
          200: '#90CAF9',
          300: '#64B5F6',
          400: '#42A5F5',
          500: '#1565C0',
          600: '#1565C0',
          700: '#0D47A1',
          800: '#0A3D91',
          900: '#052F6D',
        },
        // 背景色
        background: '#FAFAF5',
      },
      fontFamily: {
        // 标题字体：Lora 衬线体
        serif: ['Lora', 'Georgia', 'serif'],
        // 正文字体：Inter 无衬线体
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
