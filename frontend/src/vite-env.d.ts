/// <reference types="vite/client" />

// Vite 环境类型声明文件
// 为 Vite 客户端 API 提供 TypeScript 类型支持

interface ImportMetaEnv {
  // 在这里添加自定义的环境变量类型
  readonly VITE_API_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
