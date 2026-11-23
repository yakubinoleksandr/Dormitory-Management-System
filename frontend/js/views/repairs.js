// FR-13/FR-15 (мешканець) і FR-14 (завідуюча): життєвий цикл ремонтної заявки.
import { api } from '../api.js';
import { getRole } from '../auth.js';
import { esc, formData, dormitorySelect, withMessage, BTN, INPUT, CARD } from '../ui.js';

const STATUS_LABEL = { NEW: 'Нова', IN_PROGRESS: 'В роботі', COMPLETED: 'Виконана', REJECTED: 'Відхилена' };
const CATEGORY_LABEL = { PLUMBING: 'Сантехніка', ELECTRICITY: 'Електрика', FURNITURE: 'Меблі' };
const NEXT = { NEW: ['IN_PROGRESS', 'REJECTED'], IN_PROGRESS: ['COMPLETED', 'REJECTED'], COMPLETED: [], REJECTED: [] };

export async function renderRepairs(container) {
  return getRole() === 'ROLE_MANAGER' ? renderManager(container) : renderResident(container);
}

async function renderResident(container) {
  const cab = await api('/me/cabinet');
  const roomId = cab.placement?.roomId;
  container.innerHTML = `
    <p data-msg class="mb-2"></p>
    ${roomId ? `
    <form id="repair" class="${CARD} space-y-2 mb-6 max-w-xl">
      <h2 class="font-semibold">Нова заявка на ремонт (кімната ${esc(cab.placement.roomNumber)})</h2>
      <select name="category" class="${INPUT}">
        ${Object.entries(CATEGORY_LABEL).map(([v, l]) => `<option value="${v}">${l}</option>`).join('')}
      </select>
      <textarea name="description" placeholder="Опис поломки" required class="${INPUT}"></textarea>
      <button class="${BTN}">Надіслати</button>
    </form>` : '<p class="text-sm text-slate-500 mb-4">Заявки на ремонт доступні після поселення.</p>'}
    <ul id="list" class="space-y-2"></ul>`;

  const list = container.querySelector('#list');
  const refresh = async () => {
    const items = await api('/repair-requests/mine');
    list.innerHTML = items.map((r) => `
      <li class="${CARD} text-sm">
        <b>${CATEGORY_LABEL[r.category]}</b> — ${STATUS_LABEL[r.status]}
        <div>${esc(r.description)}</div>
        ${r.managerComment ? `<div class="text-slate-500">Коментар: ${esc(r.managerComment)}</div>` : ''}
      </li>`).join('');
  };
  container.querySelector('#repair')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const d = formData(e.target);
    const r = await withMessage(container, () => api('/repair-requests', { method: 'POST', body: { roomId, ...d } }));
    if (r) { e.target.reset(); await refresh(); }
  });
  await refresh();
}

async function renderManager(container) {
  container.innerHTML = '<div data-dorm class="w-72 mb-3"></div><p data-msg class="mb-2"></p><ul id="list" class="space-y-2"></ul>';
  let dormitoryId = null;
  const list = container.querySelector('#list');

  const load = async () => {
    const items = await withMessage(container, () => api(`/repair-requests?dormitoryId=${dormitoryId}`));
    list.innerHTML = (items ?? []).map((r) => `
      <li class="${CARD} text-sm space-y-1" data-id="${r.id}">
        <div><b>№${r.id} ${CATEGORY_LABEL[r.category]}</b> — ${STATUS_LABEL[r.status]} <span class="text-slate-500">(кімната ${r.roomId})</span></div>
        <div>${esc(r.description)}</div>
        ${r.managerComment ? `<div class="text-slate-500">Коментар: ${esc(r.managerComment)}</div>` : ''}
        ${NEXT[r.status].length ? `
        <div class="flex gap-2 items-center pt-1">
          <input placeholder="Коментар (обов'язковий для відхилення)" class="border rounded px-2 py-1 flex-1" data-comment>
          ${NEXT[r.status].map((s) => `<button data-to="${s}" class="${BTN}">${STATUS_LABEL[s]}</button>`).join('')}
        </div>` : ''}
      </li>`).join('') || '<li class="text-sm text-slate-500">Заявок немає</li>';

    list.querySelectorAll('[data-to]').forEach((btn) => btn.addEventListener('click', async () => {
      const li = btn.closest('li');
      const comment = li.querySelector('[data-comment]').value;
      await withMessage(container, () => api(`/repair-requests/${li.dataset.id}/status`, {
        method: 'PATCH', body: { status: btn.dataset.to, managerComment: comment || null },
      }), 'Статус змінено');
      await load();
    }));
  };
  await dormitorySelect(container, async (id) => { dormitoryId = id; await load(); });
}
