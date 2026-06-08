/**
 * 统一响应类型
 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

/**
 * 分页结果类型
 */
export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

/**
 * 用户角色类型
 */
export interface Role {
  id: number;
  name: string;
  code: string;
  description?: string;
  createdAt?: string;
}

/**
 * 用户类型
 */
export interface User {
  id: number;
  username: string;
  email?: string;
  phone?: string;
  realName?: string;
  avatar?: string;
  roleId: number;
  roleCode?: string;
  role?: Role;
  status: number;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * 登录请求参数
 */
export interface LoginRequest {
  username: string;
  password: string;
  remember?: boolean;
}

/**
 * 登录响应结果
 */
export interface LoginResponse {
  token: string;
  userInfo: User;
}

/**
 * 标本图片类型
 */
export interface SpecimenImage {
  id: number;
  specimenId: number;
  imageUrl: string;
  sortOrder?: number;
  imageType?: string;
  createdAt?: string;
}

/**
 * 标本查询参数
 */
export interface SpecimenQueryParams {
  page?: number;
  pageSize?: number;
  keyword?: string;
  taxonomyId?: number;
  collector?: string;
  startDate?: string;
  endDate?: string;
  status?: number;
}

/**
 * 标本类型
 */
export interface Specimen {
  id: number;
  specimenNo: string;
  name: string;
  latinName?: string;
  taxonomyId?: number;
  taxonomyName?: string;
  collector?: string;
  collectionDate?: string;
  collectionLocation?: string;
  latitude?: number;
  longitude?: number;
  habitat?: string;
  description?: string;
  creatorId: number;
  creatorName?: string;
  status: number;
  images?: SpecimenImage[];
  createdAt?: string;
  updatedAt?: string;
}

/**
 * 分类等级枚举
 */
export type TaxonomyRank = 'kingdom' | 'phylum' | 'class' | 'order' | 'family' | 'genus' | 'species';

/**
 * 分类类型
 */
export interface Taxonomy {
  id: number;
  parentId: number;
  name: string;
  latinName?: string;
  rank: TaxonomyRank;
  level: number;
  sortOrder?: number;
  description?: string;
  children?: Taxonomy[];
  createdAt?: string;
}

/**
 * 特征数据类型
 */
export interface FeatureData {
  id: number;
  specimenId: number;
  leafLength?: number;
  leafWidth?: number;
  leafArea?: number;
  leafPerimeter?: number;
  aspectRatio?: number;
  leafShape?: string;
  leafMargin?: string;
  leafApex?: string;
  leafBase?: string;
  texture?: string;
  colorFeatures?: any;
  featureVector?: any;
  extractedAt?: string;
}

/**
 * 特征对比参数
 */
export interface FeatureCompareParams {
  specimenIds: number[];
  features?: string[];
}

/**
 * 识别结果类型
 */
export interface RecognitionResult {
  predictedName: string;
  confidence: number;
  topPredictions?: Array<{
    name: string;
    confidence: number;
    latinName?: string;
  }>;
}

/**
 * 识别记录类型
 */
export interface RecognitionRecord {
  id: number;
  specimenId?: number;
  imageUrl: string;
  predictedName?: string;
  confidence?: number;
  topPredictions?: any;
  isConfirmed: number;
  confirmedBy?: number;
  createdAt?: string;
}

/**
 * 导出任务状态枚举
 */
export type ExportTaskStatus = 'PENDING' | 'PROCESSING' | 'SUCCESS' | 'FAILED';

/**
 * 导出任务类型
 */
export interface ExportTask {
  id: number;
  userId: number;
  taskName?: string;
  type?: string;
  exportType: string;
  format?: string;
  fileName?: string;
  fileUrl?: string;
  status: ExportTaskStatus;
  totalCount: number;
  createdAt?: string;
  completedAt?: string;
}
