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

const ROLE_LABELS = {
  [ADMIN]: 'Системний адміністратор',
  [MANAGER]: 'Завідуюча (Комендант)',
  [RESIDENT]: 'Мешканець',
};

const ROUTES = [
  { path: '/admin',         label: 'Конфігурація',     icon: 'fa-solid fa-sliders',           render: renderAdmin,         roles: [ADMIN] },
  { path: '/rooms',         label: 'Номерний фонд',    icon: 'fa-solid fa-border-all',        render: renderRooms,         roles: [ADMIN, MANAGER] },
  { path: '/applications',  label: 'Заяви на поселення', icon: 'fa-solid fa-file-signature',  render: renderApplications,  roles: [MANAGER] },
  { path: '/cabinet',       label: 'Мій кабінет',      icon: 'fa-solid fa-user',              render: renderCabinet,       roles: [RESIDENT] },
  { path: '/repairs',       label: 'Заявки на ремонт', icon: 'fa-solid fa-wrench',            render: renderRepairs,       roles: [MANAGER, RESIDENT] },
  { path: '/payments',      label: 'Оплати',           icon: 'fa-solid fa-receipt',           render: renderPayments,      roles: [MANAGER, RESIDENT] },
  { path: '/announcements', label: 'Оголошення',       icon: 'fa-solid fa-bullhorn',          render: renderAnnouncements, roles: [ADMIN, MANAGER, RESIDENT] },
  { path: '/search',        label: 'Глобальний пошук', icon: 'fa-solid fa-magnifying-glass',  render: renderSearch,        roles: [ADMIN, MANAGER] },
  { path: '/reports',       label: 'Звіти',            icon: 'fa-solid fa-chart-pie',         render: renderReports,       roles: [MANAGER] },
];

register('/login', { render: renderLogin });
ROUTES.forEach((r) => register(r.path, { render: r.render, roles: r.roles }));

export const HOME = { [ADMIN]: '/admin', [MANAGER]: '/rooms', [RESIDENT]: '/cabinet' };

function updateLayout() {
  const role = getRole();
  const sidebar = document.getElementById('sidebar');
  const topbar = document.getElementById('topbar');
  const nav = document.getElementById('nav');
  const pageTitle = document.getElementById('pageTitle');
  const roleBadge = document.getElementById('roleBadge');

  if (!role || location.hash === '#/login') {
    sidebar.classList.add('hidden');
    sidebar.classList.remove('flex');
    topbar.classList.add('hidden');
    topbar.classList.remove('flex');
    return;
  }

  sidebar.classList.remove('hidden');
  sidebar.classList.add('flex');
  topbar.classList.remove('hidden');
  topbar.classList.add('flex');

  const currentPath = location.hash.replace('#', '') || HOME[role];
  const activeRoute = ROUTES.find((r) => r.path === currentPath);
  pageTitle.textContent = activeRoute ? activeRoute.label : 'Система управління гуртожитком';
  roleBadge.innerHTML = `<i class="fa-solid fa-user-shield text-blue-600"></i><span>Роль: ${ROLE_LABELS[role] ?? role}</span>`;

  nav.innerHTML = ROUTES.filter((r) => r.roles.includes(role))
      .map((r) => {
        const isActive = r.path === currentPath;
        const cls = isActive
            ? 'bg-blue-600 text-white shadow-md'
            : 'text-slate-300 hover:bg-slate-800 hover:text-white';
        return `<a href="#${r.path}" class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors text-sm font-medium ${cls}">
        <i class="${r.icon} w-5 text-center"></i>
        <span>${r.label}</span>
      </a>`;
      })
      .join('');
}

document.getElementById('logout').addEventListener('click', async () => {
  try { await api('/auth/logout', { method: 'POST' }); } catch { /* сесія вже недійсна */ }
  clearSession();
  navigate('/login');
  location.reload();
});

const app = document.getElementById('app');
window.addEventListener('hashchange', () => {
  updateLayout();
  renderCurrent(app);
});

updateLayout();
// Якщо адреса порожня, це екран входу або сторінка, недоступна цій ролі (наприклад, старе #/rooms у мешканця),
// відкриваємо домашню сторінку ролі
const currentRoute = ROUTES.find((r) => r.path === location.hash.slice(1));
const isAllowed = currentRoute ? currentRoute.roles.includes(getRole()) : false;
if (getRole() && (!location.hash || location.hash === '#/login' || !isAllowed)) {
  navigate(HOME[getRole()]);
} else {
  renderCurrent(app);
}