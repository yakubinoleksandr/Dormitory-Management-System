// FR-13/FR-15: мешканець подає заявку на ремонт і бачить статуси своїх звернень.
import { api } from '../api.js';

const STATUS_LABEL = { NEW: 'Нова', IN_PROGRESS: 'В роботі', COMPLETED: 'Виконана', REJECTED: 'Відхилена' };
const CATEGORY_LABEL = { PLUMBING: 'Сантехніка', ELECTRICITY: 'Електрика', FURNITURE: 'Меблі' };

export async function renderRepairs(container) {
  container.innerHTML = `
    <form id="repair" class="bg-white p-4 rounded shadow space-y-2 mb-6">
      <h2 class="font-semibold">Нова заявка на ремонт</h2>
      <input name="roomId" type="number" min="1" placeholder="ID вашої кімнати" required class="border rounded px-3 py-2 w-full">
      <select name="category" class="border rounded px-3 py-2 w-full">
        ${Object.entries(CATEGORY_LABEL).map(([v, l]) => `<option value="${v}">${l}</option>`).join('')}
      </select>
      <textarea name="description" placeholder="Опис поломки" required class="border rounded px-3 py-2 w-full"></textarea>
      <button class="bg-slate-800 text-white rounded px-4 py-2">Надіслати</button>
      <p id="msg" class="text-red-600 text-sm"></p>
    </form>
    <ul id="list" class="space-y-2"></ul>`;

  const list = container.querySelector('#list');
  const refresh = async () => {
    const items = await api('/repair-requests/mine');
    list.innerHTML = items.map((r) => `
      <li class="bg-white p-3 rounded shadow text-sm">
        <b>${CATEGORY_LABEL[r.category]}</b> — ${STATUS_LABEL[r.status]}
        <div>${escapeHtml(r.description)}</div>
        ${r.managerComment ? `<div class="text-slate-500">Коментар: ${escapeHtml(r.managerComment)}</div>` : ''}
      </li>`).join('');
  };

  container.querySelector('#repair').addEventListener('submit', async (event) => {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(event.target));
    data.roomId = Number(data.roomId);
    try {
      await api('/repair-requests', { method: 'POST', body: data });
      event.target.reset();
      await refresh();
    } catch (e) {
      container.querySelector('#msg').textContent = e.message;
    }
  });
  await refresh();
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}
