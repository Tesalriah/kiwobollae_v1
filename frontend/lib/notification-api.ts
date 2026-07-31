import { request, SpringPage } from '@/lib/api';

export type NotificationType = 'DELIVERY' | 'COMMUNITY' | 'POINT' | 'NOTICE' | 'INQUIRY' | 'JOURNAL_REMINDER';

export interface NotificationData {
  id: number;
  userId: number;
  type: NotificationType;
  title: string;
  content: string;
  linkUrl: string | null;
  refType: string | null;
  refId: number | null;
  isRead: boolean;
  readAt: string | null;
  createdAt: string;
}

export interface NotificationSettingData {
  id: number | null;
  userId: number;
  type: NotificationType;
  enabled: boolean;
  updatedAt: string | null;
}

export function getNotifications(
  accessToken: string,
  type?: NotificationType,
  page = 0,
  size = 20,
  signal?: AbortSignal,
): Promise<SpringPage<NotificationData>> {
  const query = new URLSearchParams({ page: String(page), size: String(size) });
  if (type) query.set('type', type);
  return request<SpringPage<NotificationData>>(`/api/v1/notifications?${query.toString()}`, {
    accessToken,
    signal,
  });
}

export function getUnreadNotificationCount(
  accessToken: string,
  signal?: AbortSignal,
): Promise<{ unreadCount: number }> {
  return request<{ unreadCount: number }>('/api/v1/notifications/unread-count', {
    accessToken,
    signal,
  });
}

export function markNotificationRead(
  notificationId: number,
  accessToken: string,
): Promise<NotificationData> {
  return request<NotificationData>(`/api/v1/notifications/${notificationId}/read`, {
    method: 'PATCH',
    accessToken,
  });
}

export function markAllNotificationsRead(accessToken: string): Promise<void> {
  return request<void>('/api/v1/notifications/read-all', {
    method: 'PATCH',
    accessToken,
  });
}

export function deleteNotification(notificationId: number, accessToken: string): Promise<void> {
  return request<void>(`/api/v1/notifications/${notificationId}`, {
    method: 'DELETE',
    accessToken,
  });
}

export function getNotificationSettings(
  accessToken: string,
  signal?: AbortSignal,
): Promise<NotificationSettingData[]> {
  return request<NotificationSettingData[]>('/api/v1/notifications/settings', {
    accessToken,
    signal,
  });
}

export function updateNotificationSetting(
  type: NotificationType,
  enabled: boolean,
  accessToken: string,
): Promise<NotificationSettingData> {
  return request<NotificationSettingData>('/api/v1/notifications/settings', {
    method: 'PATCH',
    accessToken,
    body: JSON.stringify({ type, enabled }),
  });
}
