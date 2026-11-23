import { api } from '../api.js';
import { saveSession } from '../auth.js';
import { navigate } from '../router.js';
import { BTN, INPUT } from '../ui.js';

export async function renderLogin(container) {
  container.innerHTML = `
    <div class="min-h-[80vh] flex items-center justify-center">
      <form id="login" class="w-full max-w-md bg-white p-8 rounded-2xl shadow-lg border border-slate-100 space-y-5">
        <div class="text-center space-y-2">
          <div class="w-14 h-14 bg-blue-600 text-white rounded-2xl flex items-center justify-center text-2xl mx-auto shadow-md">
            <i class="fa-solid fa-building"></i>
          </div>
          <h2 class="text-2xl font-bold text-slate-800">Гуртожиток КС</h2>
          <p class="text-sm text-slate-500">Увійдіть в систему управління кампусом</p>
        </div>

        <div class="space-y-3">
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Логін</label>
            <input name="username" placeholder="Введіть логін" required class="${INPUT}">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Пароль</label>
            <input name="password" type="password" placeholder="••••••••" required class="${INPUT}">
          </div>
        </div>

        <button class="${BTN} w-full py-2.5 text-base">
          <i class="fa-solid fa-right-to-bracket"></i> Увійти в систему
        </button>
        <p id="error" class="text-rose-600 text-sm text-center font-medium"></p>
      </form>
    </div>`;

  container.querySelector('#login').addEventListener('submit', async (event) => {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(event.target));
    try {
      const session = await api('/auth/login', { method: 'POST', body: data });
      saveSession(session);
      navigate({ ROLE_ADMIN: '/admin', ROLE_MANAGER: '/rooms', ROLE_RESIDENT: '/cabinet' }[session.role]);
      location.reload();
    } catch (e) {
      container.querySelector('#error').textContent = e.message;
    }
  });
}