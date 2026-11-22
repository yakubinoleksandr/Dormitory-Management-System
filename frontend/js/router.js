// Мінімальний hash-роутер: кожен маршрут — функція, що малює вміст у контейнер.
import { getRole } from './auth.js';

const routes = new Map();

export function register(path, { render, roles = null }) {
  routes.set(path, { render, roles });
}

export function navigate(path) {
  location.hash = `#${path}`;
}

export async function renderCurrent(container) {
  const path = location.hash.slice(1) || '/';
  const route = routes.get(path) ?? routes.get('/login');
  const role = getRole();

  if (route.roles && !route.roles.includes(role)) {
    return navigate('/login');
  }
  container.innerHTML = '';
  await route.render(container);
}
