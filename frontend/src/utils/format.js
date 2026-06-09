export function formatDateTime(value) {
  if (!value) return 'Not set';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

export function formatCurrency(value) {
  return `RM ${Number(value || 0).toFixed(2)}`;
}

export function statusClass(status) {
  return `status-badge status-${String(status || 'default').toLowerCase()}`;
}

export function toDateTimeLocal(value) {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';

  const offset = date.getTimezoneOffset();
  const local = new Date(date.getTime() - offset * 60000);
  return local.toISOString().slice(0, 16);
}
