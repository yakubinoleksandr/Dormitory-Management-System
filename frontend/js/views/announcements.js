// FR-11 (публікація: адміністратор, завідуюча) і FR-12 (перегляд стрічки).
import { api } from '../api.js';
import { getRole } from '../auth.js';
import { esc, formData, fmtDate, dormitorySelect, withMessage, BTN, INPUT, CARD } from '../ui.js';

export async function renderAnnouncements(container) {
  const canPublish = getRole() !== 'ROLE_RESIDENT';
  container.innerHTML = `
    <div data-dorm class="w-72 mb-3"></div><p data-msg class="mb-2"></p>
    ${canPublish ? `
    <form id="ann" class="${CARD} space-y-2 max-w-xl mb-6">
      <h2 class="font-semibold">Нове оголошення</h2>
      <input name="title" placeholder="Заголовок" required maxlength="200" class="${INPUT}">
      <textarea name="content" placeholder="Текст" required class="${INPUT}"></textarea>
      <label class="text-xs text-slate-500">Актуальне до (необов'язково)</label>
      <input name="expiresAt" type="datetime-local" class="${INPUT}">
      <button class="${BTN}">Опублікувати</button>
    </form>` : ''}
    <ul id="list" class="space-y-2"></ul>`;

  let dormitoryId = null;
  const list = container.querySelector('#list');
  const load = async () => {
    const items = await withMessage(container, () => api(`/announcements?dormitoryId=${dormitoryId}`));
    list.innerHTML = (items ?? []).map((a) => `
      <li class="${CARD} text-sm">
        <div class="font-semibold">${esc(a.title)} <span class="text-xs text-slate-500">${fmtDate(a.createdAt)}</span></div>
        <div>${esc(a.content)}</div>
      </li>`).join('') || '<li class="text-sm text-slate-500">Актуальних оголошень немає</li>';
  };

  container.querySelector('#ann')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const d = formData(e.target);
    const body = { dormitoryId, title: d.title, content: d.content, expiresAt: d.expiresAt || null };
    const r = await withMessage(container, () => api('/announcements', { method: 'POST', body }), 'Оголошення опубліковано');
    if (r) { e.target.reset(); await load(); }
  });
  await dormitorySelect(container, async (id) => { dormitoryId = id; await load(); });
}
