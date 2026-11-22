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
  return response.status === 204 ? null : response.json();
}
