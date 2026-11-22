import { getRole, clearSession } from './auth.js';
import { register, renderCurrent, navigate } from './router.js';
import { renderLogin } from './views/login.js';
import { renderRooms } from './views/rooms.js';
import { renderRepairs } from './views/repairs.js';
import { api } from './api.js';

const ADMIN_MANAGER = ['ROLE_ADMIN', 'ROLE_MANAGER'];

register('/login', { render: renderLogin });
register('/rooms', { render: renderRooms, roles: [...ADMIN_MANAGER, 'ROLE_RESIDENT'] });
register('/repairs', { render: renderRepairs, roles: ['ROLE_RESIDENT'] });

function renderNav() {
  const nav = document.getElementById('nav');
  const role = getRole();
  if (!role) { nav.innerHTML = ''; return; }
  nav.innerHTML = `
    <a href="#/rooms" class="hover:underline">Кімнати</a>
    ${role === 'ROLE_RESIDENT' ? '<a href="#/repairs" class="hover:underline">Ремонт</a>' : ''}
    <button id="logout" class="hover:underline">Вийти</button>`;
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
renderCurrent(app);
