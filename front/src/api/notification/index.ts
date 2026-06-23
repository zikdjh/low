import request from '../index'

export interface NotificationRequest {
  title: string
  content: string
  type?: string
  targetUsers?: string
}

export interface NotificationResponse {
  id: number
  title: string
  content: string
  type: string
  status: string
  targetUsers: string
  isPublished: boolean
  createdAt: string
  updatedAt: string
  publishedAt: string | null
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export const notificationApi = {
  createNotification: (data: NotificationRequest) => {
    return request.post<NotificationResponse>('/notifications', data)
  },

  updateNotification: (id: number, data: NotificationRequest) => {
    return request.put<NotificationResponse>(`/notifications/${id}`, data)
  },

  getNotificationById: (id: number) => {
    return request.get<NotificationResponse>(`/notifications/${id}`)
  },

  getAllNotifications: (page: number = 0, size: number = 10) => {
    return request.get<PageResponse<NotificationResponse>>(`/notifications?page=${page}&size=${size}`)
  },

  deleteNotification: (id: number) => {
    return request.delete(`/notifications/${id}`)
  },

  publishNotification: (id: number) => {
    return request.post<NotificationResponse>(`/notifications/${id}/publish`)
  },

  unpublishNotification: (id: number) => {
    return request.post(`/notifications/${id}/unpublish`)
  },

  getPublishedNotifications: () => {
    return request.get<NotificationResponse[]>('/notifications/published')
  }
}
