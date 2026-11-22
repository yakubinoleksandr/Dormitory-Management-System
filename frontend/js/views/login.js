import { api } from '../api.js';
import { saveSession } from '../auth.js';
import { navigate } from '../router.js';

export async function renderLogin(container) {
  container.innerHTML = `
    <form id="login" class="max-w-sm mx-auto bg-white p-6 rounded shadow space-y-3">
      <h2 class="text-xl font-semibold">Вхід</h2>
      <input name="username" placeholder="Логін" required class="w-full border rounded px-3 py-2">
      <input name="password" type="password" placeholder="Пароль" required class="w-full border rounded px-3 py-2">
      <button class="w-full bg-slate-800 text-white rounded py-2">Увійти</button>
      <p id="error" class="text-red-600 text-sm"></p>
    </form>`;

  container.querySelector('#login').addEventListener('submit', async (event) => {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(event.target));
    try {
      const session = await api('/auth/login', { method: 'POST', body: data });
      saveSession(session);
      navigate(session.role === 'ROLE_RESIDENT' ? '/repairs' : '/rooms');
      location.reload();
    } catch (e) {
      container.querySelector('#error').textContent = e.message;
    }
  });
}
