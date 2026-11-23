// Єдина точка звернення до REST API (/api). Токен береться з auth.js.
import { getToken, clearSession } from './auth.js';

export class ApiError extends Error {
  constructor(status, code, message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

export async function api(path, { method = 'GET', body, headers = {} } = {}) {
  const token = getToken();
  const response = await fetch(`/api${path}`, {
    method,
    headers: {
      ...(body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  if (response.status === 401) clearSession();
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new ApiError(response.status, err.code ?? 'ERROR', err.message ?? 'Помилка запиту');
  }
  // Деякі успішні відповіді (201/204) приходять без тіла: парсимо JSON лише якщо він є
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

/** Завантаження файлу (звіт, квитанція) із авторизацією: відкриваємо Blob у новій вкладці або зберігаємо. */
export async function download(path, filename, { open = false } = {}) {
  const token = getToken();
  const response = await fetch(`/api${path}`, { headers: token ? { Authorization: `Bearer ${token}` } : {} });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new ApiError(response.status, err.code ?? 'ERROR', err.message ?? 'Помилка завантаження');
  }
  const url = URL.createObjectURL(await response.blob());
  if (open) {
    window.open(url, '_blank');
  } else {
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
  }
  setTimeout(() => URL.revokeObjectURL(url), 60_000);
}

/** multipart/form-data (квитанції). */
export async function upload(path, formData) {
  const token = getToken();
  const response = await fetch(`/api${path}`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData,
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new ApiError(response.status, err.code ?? 'ERROR', err.message ?? 'Помилка завантаження');
  }
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}