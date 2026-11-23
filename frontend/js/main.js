import { getRole, clearSession } from './auth.js';
import { register, renderCurrent, navigate } from './router.js';
import { api } from './api.js';
import { renderLogin } from './views/login.js';
import { renderAdmin } from './views/admin.js';
import { renderRooms } from './views/rooms.js';
import { renderApplications } from './views/applications.js';
import { renderCabinet } from './views/cabinet.js';
import { renderRepairs } from './views/repairs.js';
import { renderPayments } from './views/payments.js';
import { renderAnnouncements } from './views/announcements.js';
import { renderSearch } from './views/search.js';
import { renderReports } from './views/reports.js';

const ADMIN = 'ROLE_ADMIN';
const MANAGER = 'ROLE_MANAGER';
const RESIDENT = 'ROLE_RESIDENT';

const ROUTES = [
  { path: '/admin',         label: 'Конфігурація',   render: renderAdmin,         roles: [ADMIN] },
  { path: '/rooms',         label: 'Кімнати',        render: renderRooms,         roles: [ADMIN, MANAGER, RESIDENT] },
  { path: '/applications',  label: 'Заяви',          render: renderApplications,  roles: [MANAGER] },
  { path: '/cabinet',       label: 'Кабінет',        render: renderCabinet,       roles: [RESIDENT] },
  { path: '/repairs',       label: 'Ремонт',         render: renderRepairs,       roles: [MANAGER, RESIDENT] },
  { path: '/payments',      label: 'Оплати',         render: renderPayments,      roles: [MANAGER, RESIDENT] },
  { path: '/announcements', label: 'Оголошення',     render: renderAnnouncements, roles: [ADMIN, MANAGER, RESIDENT] },
  { path: '/search',        label: 'Пошук',          render: renderSearch,        roles: [ADMIN, MANAGER] },
  { path: '/reports',       label: 'Звіти',          render: renderReports,       roles: [MANAGER] },
];

register('/login', { render: renderLogin });
ROUTES.forEach((r) => register(r.path, { render: r.render, roles: r.roles }));

export const HOME = { [ADMIN]: '/admin', [MANAGER]: '/rooms', [RESIDENT]: '/cabinet' };

function renderNav() {
  const nav = document.getElementById('nav');
  const role = getRole();
  if (!role) { nav.innerHTML = ''; return; }
  nav.innerHTML = ROUTES.filter((r) => r.roles.includes(role))
    .map((r) => `<a href="#${r.path}" class="hover:underline">${r.label}</a>`).join('') +
    '<button id="logout" class="hover:underline">Вийти</button>';
  document.getElementById('logout').addEventListener('click', async () => {
    try { await api('/auth/logout', { method: 'POST' }); } catch { /* сесія вже недійсна */ }
    clearSession();
    navigate('/login');
    location.reload();
  });
}

const app = document.getElementById('app');
window.addEventListener('hashchange', () => renderCurrent(app));
renderNav();
if (getRole() && (!location.hash || location.hash === '#/login')) navigate(HOME[getRole()]);
else renderCurrent(app);
