// 低代码平台相关类型定义

// ---- 数据字典 ----

export interface DictType {
  id?: number;
  code: string;
  name: string;
  description?: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface DictItem {
  id?: number;
  dictCode?: string;
  value: string;
  label: string;
  sortOrder?: number;
  status?: string;
}

// ---- 枚举 ----

export type FieldType =
  | 'VARCHAR'
  | 'INTEGER'
  | 'LONG'
  | 'DOUBLE'
  | 'BOOLEAN'
  | 'DATE'
  | 'DATETIME'
  | 'TEXT'
  | 'JSON'
  | 'DECIMAL'
  | 'REFERENCE';

export type EntityStatus = 'draft' | 'published' | 'archived';
export type PageType = 'list' | 'form' | 'detail' | 'custom' | 'dashboard';
export type ComponentCategory = 'layout' | 'data' | 'form' | 'display' | 'chart' | 'advanced';

// ---- 实体元数据 ----

export interface EntityMeta {
  id?: number;
  code: string;
  name: string;
  tableName?: string;
  description?: string;
  status?: EntityStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface FieldMeta {
  id?: number;
  entityId?: number;
  code: string;
  name: string;
  columnName?: string;
  fieldType: FieldType;
  length?: number;
  precision?: number;
  scale?: number;
  decimalPlaces?: number;
  nullable: boolean;
  defaultValue?: string;
  isPrimaryKey: boolean;
  isAutoIncrement: boolean;
  sortOrder: number;
  showInList: boolean;
  showInForm: boolean;
  showInSearch: boolean;
  dictCode?: string;
  referenceEntityCode?: string;
  referenceDisplayFieldCode?: string;
  validationRule?: string;
  sortable?: boolean;
}

// ---- 实体关系 ----

export type RelationType = 'ONE_TO_ONE' | 'ONE_TO_MANY' | 'MANY_TO_ONE' | 'MANY_TO_MANY';

export interface EntityRelation {
  id?: number;
  sourceEntityCode: string;
  sourceFieldCode: string;
  targetEntityCode: string;
  targetDisplayFieldCode?: string;
  relationType: RelationType;
  description?: string;
  cascadeDelete?: boolean;
  enabled?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// ---- 组件定义 ----

export interface ComponentDef {
  id?: number;
  compKey: string;
  name: string;
  category: ComponentCategory;
  icon: string;
  defaultPropsJson: string;
  propsSchemaJson: string;
  isSystem: boolean;
}

// ---- 页面 Schema ----

export interface PageSchema {
  id?: number;
  name: string;
  pageCode: string;
  entityCode?: string;
  pageType: PageType;
  layoutJson: string;
  version: number;
  status: string;
}

export interface ComponentInstance {
  id: string;
  compKey: string;
  label: string;
  props: Record<string, any>;
  style: Record<string, string>;
  events?: Record<string, { action: string; [key: string]: any }>;
  children: ComponentInstance[];
  slot?: string;
}

// ---- 通用 CRUD ----

export interface GenericQueryRequest {
  page: number;
  pageSize: number;
  filters: Record<string, string>;
  sort: Record<string, string>;
}

export interface GenericPageResult {
  records: Record<string, any>[];
  total: number;
  page: number;
  pageSize: number;
}

// ---- 请假管理 & 工作流 ----

export interface LeaveApplication {
  studentId: number;
  studentName: string;
  leaveType: string;
  reason: string;
  startDate: string;
  endDate: string;
}

export interface LeaveQueryParams {
  page?: number;
  pageSize?: number;
  studentId?: number;
  status?: string;
  leaveType?: string;
}

export interface WorkflowTask {
  id: number;
  instanceId: number;
  nodeName: string;
  nodeOrder: number;
  assigneeRole: string;
  assigneeId?: number;
  assigneeName?: string;
  status: 'pending' | 'approved' | 'rejected' | 'skipped';
  comment?: string;
  processedAt?: string;
  createdAt: string;
}

export interface WorkflowInstance {
  id: number;
  businessId: number;
  businessCode: string;
  workflowCode: string;
  workflowName: string;
  status: 'pending' | 'approved' | 'rejected' | 'cancelled' | 'archived';
  initiatorId: number;
  initiatorName: string;
  currentNodeIndex: number;
  totalNodes: number;
  finishedAt?: string;
  createdAt: string;
}

export interface WorkflowProgress {
  instance: WorkflowInstance;
  tasks: WorkflowTask[];
  currentNode: number;
}

export interface ApprovalRequest {
  instanceId: number;
  userId: number;
  username: string;
  comment: string;
}

export interface PageResult {
  content?: any[];
  records?: any[];
  totalElements?: number;
  total?: number;
  number?: number;
  page?: number;
  size?: number;
  pageSize?: number;
}

