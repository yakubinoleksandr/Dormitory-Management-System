// Завідуюча: розгляд заяв на поселення (відхилення), виселення, зміна термінів (FR-07–FR-09).
import { api } from '../api.js';
import { esc, dormitorySelect, withMessage, BTN, CARD } from '../ui.js';

export async function renderApplications(container) {
  container.innerHTML = `
    <div data-dorm class="w-72 mb-3"></div><p data-msg class="mb-2"></p>
    <h2 class="font-semibold mb-2">Заяви на поселення (очікують)</h2>
    <ul id="apps" class="space-y-2 mb-6"></ul>
    <h2 class="font-semibold mb-2">Виселення за номером договору</h2>
    <form id="moveout" class="${CARD} flex gap-2 max-w-xl">
      <input name="id" type="number" min="1" placeholder="ID договору" required class="border rounded px-3 py-2 text-sm flex-1">
      <button class="${BTN}">Виселити</button>
    </form>
    <h2 class="font-semibold mt-6 mb-2">Переселення в межах гуртожитку</h2>
    <form id="move" class="${CARD} flex gap-2 max-w-xl">
      <input name="id" type="number" min="1" placeholder="ID договору" required class="border rounded px-3 py-2 text-sm flex-1">
      <input name="bedId" type="number" min="1" placeholder="ID нового ліжка" required class="border rounded px-3 py-2 text-sm flex-1">
      <button class="${BTN}">Переселити</button>
    </form>`;

  let dormitoryId = null;
  const list = container.querySelector('#apps');

  const load = async () => {
    const apps = await withMessage(container, () => api(`/settlement-applications?dormitoryId=${dormitoryId}&status=PENDING`));
    list.innerHTML = (apps ?? []).map((a) => `
      <li class="${CARD} text-sm flex justify-between items-center">
        <span>№${a.id} — ${esc(a.fullName)} <span class="text-slate-500">(${new Date(a.createdAt).toLocaleDateString('uk-UA')})</span></span>
        <button data-reject="${a.id}" class="text-red-600 hover:underline">Відхилити</button>
      </li>`).join('') || '<li class="text-sm text-slate-500">Заяв немає</li>';
    list.querySelectorAll('[data-reject]').forEach((b) => b.addEventListener('click', async () => {
      await withMessage(container, () => api(`/settlement-applications/${b.dataset.reject}/reject`, { method: 'PATCH' }), 'Заяву відхилено');
      await load();
    }));
  };

  container.querySelector('#moveout').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = new FormData(e.target).get('id');
    await withMessage(container, () => api(`/settlements/${id}/move-out`, { method: 'PATCH' }), 'Мешканця виселено');
  });
  container.querySelector('#move').addEventListener('submit', async (e) => {
    e.preventDefault();
    const d = Object.fromEntries(new FormData(e.target));
    await withMessage(container, () => api(`/settlements/${d.id}/bed`, { method: 'PATCH', body: { bedId: Number(d.bedId) } }), 'Мешканця переселено');
  });

  await dormitorySelect(container, async (id) => { dormitoryId = id; await load(); });
}
