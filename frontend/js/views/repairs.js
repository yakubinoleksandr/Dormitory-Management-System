// FR-13/FR-15 (мешканець) і FR-14 (завідуюча): життєвий цикл ремонтної заявки.
import { api } from '../api.js';
import { getRole } from '../auth.js';
import { esc, formData, dormitorySelect, withMessage, BTN, INPUT, CARD } from '../ui.js';

const STATUS_LABEL = { NEW: 'Нова', IN_PROGRESS: 'В роботі', COMPLETED: 'Виконана', REJECTED: 'Відхилена' };
const CATEGORY_LABEL = { PLUMBING: 'Сантехніка', ELECTRICITY: 'Електрика', FURNITURE: 'Меблі' };
const NEXT = { NEW: ['IN_PROGRESS', 'REJECTED'], IN_PROGRESS: ['COMPLETED', 'REJECTED'], COMPLETED: [], REJECTED: [] };

const getStatusBadge = (status) => {
  if (status === 'NEW') return '<span class="px-2.5 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-semibold">Нова</span>';
  if (status === 'IN_PROGRESS') return '<span class="px-2.5 py-1 bg-amber-100 text-amber-700 rounded-full text-xs font-semibold">В роботі</span>';
  if (status === 'COMPLETED') return '<span class="px-2.5 py-1 bg-emerald-100 text-emerald-700 rounded-full text-xs font-semibold">Виконана</span>';
  return '<span class="px-2.5 py-1 bg-slate-100 text-slate-600 rounded-full text-xs font-semibold">Відхилена</span>';
};

export async function renderRepairs(container) {
  return getRole() === 'ROLE_MANAGER' ? renderManager(container) : renderResident(container);
}

async function renderResident(container) {
  const cab = await api('/me/cabinet');
  const roomId = cab.placement?.roomId;
  container.innerHTML = `
    <div class="max-w-5xl mx-auto">
      <p data-msg></p>
      ${roomId ? `
      <form id="repair" class="bg-white p-6 rounded-xl shadow-sm border border-slate-200 border-l-4 border-l-blue-600 space-y-4 mb-6">
        <h3 class="text-lg font-bold text-slate-800">Створити нову заявку (кімната ${esc(cab.placement.roomNumber)})</h3>
        <div>
          <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Категорія проблеми</label>
          <select name="category" class="${INPUT}">
            ${Object.entries(CATEGORY_LABEL).map(([v, l]) => `<option value="${v}">${l}</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Опис поломки</label>
          <textarea name="description" placeholder="Детально опишіть проблему..." required class="${INPUT} h-24 resize-none"></textarea>
        </div>
        <div class="flex justify-end">
          <button class="${BTN}"><i class="fa-solid fa-paper-plane"></i> Відправити заявку</button>
        </div>
      </form>` : '<p class="text-sm text-slate-600 bg-amber-50 border border-amber-200 p-4 rounded-xl mb-6">Подання заявок на ремонт стане доступним після поселення в кімнату.</p>'}

      <div class="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200">
                <th class="p-4 font-semibold text-slate-600 text-xs uppercase">ID</th>
                <th class="p-4 font-semibold text-slate-600 text-xs uppercase">Категорія</th>
                <th class="p-4 font-semibold text-slate-600 text-xs uppercase">Опис та коментар</th>
                <th class="p-4 font-semibold text-slate-600 text-xs uppercase">Статус</th>
              </tr>
            </thead>
            <tbody id="list"></tbody>
          </table>
        </div>
      </div>
    </div>`;

  const list = container.querySelector('#list');
  const refresh = async () => {
    const items = await api('/repair-requests/mine');
    list.innerHTML = items.length ? items.map((r) => `
      <tr class="hover:bg-slate-50 transition-colors border-b border-slate-100">
        <td class="p-4 text-sm text-slate-500 font-medium">#${r.id}</td>
        <td class="p-4 text-sm"><span class="bg-slate-100 px-2.5 py-1 rounded border border-slate-200 font-medium">${CATEGORY_LABEL[r.category]}</span></td>
        <td class="p-4 text-sm text-slate-700">
          <div>${esc(r.description)}</div>
          ${r.managerComment ? `<div class="text-xs text-slate-500 mt-1"><i class="fa-solid fa-comment-dots mr-1"></i>Коментар завідуючої: ${esc(r.managerComment)}</div>` : ''}
        </td>
        <td class="p-4">${getStatusBadge(r.status)}</td>
      </tr>`).join('') : '<tr><td colspan="4" class="p-8 text-center text-slate-500 text-sm">У вас поки немає заявок на ремонт</td></tr>';
  };

  container.querySelector('#repair')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const d = formData(e.target);
    const r = await withMessage(container, () => api('/repair-requests', { method: 'POST', body: { roomId, ...d } }), 'Заявку успішно створено');
    if (r) { e.target.reset(); await refresh(); }
  });
  await refresh();
}

async function renderManager(container) {
  container.innerHTML = `
    <div class="max-w-5xl mx-auto">
      <div class="bg-white p-4 rounded-xl shadow-sm border border-slate-200 mb-6 w-80" data-dorm></div>
      <p data-msg></p>
      <div id="list" class="space-y-4"></div>
    </div>`;

  let dormitoryId = null;
  const list = container.querySelector('#list');

  const load = async () => {
    const items = await withMessage(container, () => api(`/repair-requests?dormitoryId=${dormitoryId}`));
    list.innerHTML = (items ?? []).map((r) => `
      <div class="${CARD} space-y-3" data-id="${r.id}">
        <div class="flex flex-wrap justify-between items-center gap-2 border-b border-slate-100 pb-2">
          <div class="flex items-center gap-2">
            <span class="font-bold text-slate-800">Заявка #${r.id}</span>
            <span class="bg-slate-100 px-2.5 py-0.5 rounded border border-slate-200 text-xs font-semibold">${CATEGORY_LABEL[r.category]}</span>
            <span class="text-xs text-slate-500 font-medium">Кімната ID: ${r.roomId}</span>
          </div>
          ${getStatusBadge(r.status)}
        </div>
        <p class="text-sm text-slate-700">${esc(r.description)}</p>
        ${r.managerComment ? `<div class="text-xs bg-slate-50 p-2.5 rounded-lg text-slate-600 border border-slate-100"><b>Коментар:</b> ${esc(r.managerComment)}</div>` : ''}
        ${NEXT[r.status].length ? `
        <div class="flex flex-wrap gap-2 items-center pt-2">
          <input placeholder="Коментар (обов'язковий для відхилення)" class="${INPUT} flex-1 min-w-[200px]" data-comment>
          ${NEXT[r.status].map((s) => `<button data-to="${s}" class="${s === 'REJECTED' ? 'bg-rose-600 hover:bg-rose-700 text-white rounded-lg px-3 py-2 text-sm font-medium' : BTN}">${STATUS_LABEL[s]}</button>`).join('')}
        </div>` : ''}
      </div>`).join('') || '<p class="text-sm text-slate-500 text-center py-8 bg-white rounded-xl border border-slate-200">Заявок на ремонт немає</p>';

    list.querySelectorAll('[data-to]').forEach((btn) => btn.addEventListener('click', async () => {
      const card = btn.closest('[data-id]');
      const comment = card.querySelector('[data-comment]').value;
      await withMessage(container, () => api(`/repair-requests/${card.dataset.id}/status`, {
        method: 'PATCH', body: { status: btn.dataset.to, managerComment: comment || null },
      }), 'Статус заявки оновлено');
      await load();
    }));
  };
  await dormitorySelect(container, async (id) => { dormitoryId = id; await load(); });
}