export function getNotificationId(notice) {
  return notice.notifId ?? notice.notificationId ?? notice.notification_id ?? null;
}

export function getNotificationMessage(notice) {
  return notice.message ?? notice.eventMessage ?? notice.event_message ?? 'No notification message provided.';
}

export function getNotificationTimestamp(notice) {
  return notice.timestamp ?? notice.createdAt ?? notice.created_at ?? null;
}

export function isNotificationRead(notice) {
  return notice.read === true || notice.isRead === true || notice.is_read === true || notice.is_read === 1;
}
