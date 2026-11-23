// Завідуюча: розгляд заяв на поселення (відхилення), виселення, зміна термінів (FR-07–FR-09).
import { api } from '../api.js';
import { getRole } from '../auth.js';
import { esc, formData, fmtDate, dormitorySelect, withMessage, BTN, INPUT, CARD } from '../ui.js';

export async function renderAnnouncements(container) {
  const canPublish = getRole() !== 'ROLE_RESIDENT';
  container.innerHTML = `
    <div class="max-w-3xl mx-auto">
      <div class="bg-white p-4 rounded-xl shadow-sm border border-slate-200 mb-6 w-80" data-dorm></div>
      <p data-msg></p>

      ${canPublish ? `
      <form id="ann" class="${CARD} space-y-4 mb-8">
        <h3 class="text-lg font-bold text-slate-800">Опублікувати нове оголошення</h3>
        <div>
          <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Заголовок</label>
          <input name="title" placeholder="Введіть заголовок оголошення" required maxlength="200" class="${INPUT}">
        </div>
        <div>
          <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Текст повідомлення</label>
          <textarea name="content" placeholder="Деталі оголошення..." required class="${INPUT} h-24 resize-none"></textarea>
        </div>
        <div>
          <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Актуальне до (необов'язково)</label>
          <input name="expiresAt" type="datetime-local" class="${INPUT}">
        </div>
        <div class="flex justify-end">
          <button class="${BTN}"><i class="fa-solid fa-bullhorn"></i> Опублікувати</button>
        </div>
      </form>` : ''}

      <h3 class="text-xl font-bold text-slate-800 mb-4 flex items-center gap-2">
        <i class="fa-solid fa-bell text-blue-600"></i> Стрічка новин гуртожитку
      </h3>
      <div id="list" class="space-y-4"></div>
    </div>`;

  let dormitoryId = null;
  const list = container.querySelector('#list');
  const load = async () => {
    const items = await withMessage(container, () => api(`/announcements?dormitoryId=${dormitoryId}`));
    list.innerHTML = (items ?? []).map((a) => `
      <div class="bg-white p-5 rounded-xl shadow-sm border border-slate-200 hover:shadow-md transition-shadow relative overflow-hidden">
        <div class="absolute top-0 left-0 w-1.5 h-full bg-blue-600"></div>
        <div class="flex justify-between items-start gap-4 mb-2 pl-2">
          <h4 class="text-lg font-bold text-slate-800">${esc(a.title)}</h4>
          <span class="text-xs bg-slate-100 text-slate-600 px-2.5 py-1 rounded-md font-medium shrink-0">${fmtDate(a.createdAt)}</span>
        </div>
        <p class="text-slate-600 text-sm pl-2 leading-relaxed">${esc(a.content)}</p>
      </div>`).join('') || '<p class="text-sm text-slate-500 text-center py-8 bg-white rounded-xl border border-slate-200">Актуальних оголошень поки немає</p>';
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