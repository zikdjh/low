// 应用常量定义

// 品牌色
export const BRAND_COLOR = '#E8A317';
export const BRAND_COLOR_LIGHT = '#F5A623';

// 存储键
export const STORAGE_KEYS = {
  TOKEN: 'token',
  ACCESS: 'access',
  USER: 'user_info',
  SIDEBAR_COLLAPSED: 'sidebar_collapsed',
} as const;

// API 响应码
export const API_CODE = {
  SUCCESS: 1,
  ERROR: 0,
  UNAUTHORIZED: 499,
} as const;

// 页面状态
export const PAGE_STATUS = {
  DRAFT: 'draft',
  PUBLISHED: 'published',
  ARCHIVED: 'archived',
} as const;

// 字段类型
export const FIELD_TYPES = [
  { value: 'STRING', label: '文本', icon: 'text' },
  { value: 'INTEGER', label: '整数', icon: 'number' },
  { value: 'LONG', label: '长整数', icon: 'number' },
  { value: 'DECIMAL', label: '小数', icon: 'number' },
  { value: 'BOOLEAN', label: '布尔值', icon: 'boolean' },
  { value: 'DATE', label: '日期', icon: 'date' },
  { value: 'DATETIME', label: '日期时间', icon: 'date' },
  { value: 'TEXT', label: '长文本', icon: 'text' },
] as const;
