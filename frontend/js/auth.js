// Стан авторизації. sessionStorage: токен зникає із закриттям вкладки.
const KEY = 'dms.session';

export function saveSession(session) {
  sessionStorage.setItem(KEY, JSON.stringify(session));
}

export function getSession() {
  try {
    return JSON.parse(sessionStorage.getItem(KEY));
  } catch {
    return null;
  }
}

export const getToken = () => getSession()?.token ?? null;
export const getRole = () => getSession()?.role ?? null;

export function clearSession() {
  sessionStorage.removeItem(KEY);
}
