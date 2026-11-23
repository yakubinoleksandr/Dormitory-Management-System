// FR-16: мешканець подає квитанцію, завідуюча перевіряє.
import { api, upload, download } from '../api.js';
import { getRole } from '../auth.js';
import { esc, dormitorySelect, withMessage, BTN, INPUT, CARD } from '../ui.js';

const LABEL = { PENDING: 'очікує перевірки', VERIFIED: 'підтверджено', REJECTED: 'відхилено' };

export async function renderPayments(container) {
  return getRole() === 'ROLE_MANAGER' ? renderManager(container) : renderResident(container);
}

async function renderResident(container) {
  const cab = await api('/me/cabinet');
  const settlementId = cab.placement?.settlementId;
  if (!settlementId) {
    container.innerHTML = '<p class="text-sm text-slate-500">Оплати доступні після поселення.</p>';
    return;
  }
  container.innerHTML = `
    <p data-msg class="mb-2"></p>
    <form id="pay" class="${CARD} space-y-2 max-w-md mb-6">
      <h2 class="font-semibold">Завантажити квитанцію</h2>
      <input name="amount" type="number" step="0.01" min="0.01" placeholder="Сума, грн" required class="${INPUT}">
      <input name="method" placeholder="Спосіб оплати" class="${INPUT}">
      <input name="receipt" type="file" accept="application/pdf,image/jpeg,image/png" required class="${INPUT}">
      <button class="${BTN}">Надіслати</button>
    </form>
    <ul id="list" class="space-y-2"></ul>`;
  const list = container.querySelector('#list');
  const refresh = async () => {
    const items = await api(`/settlements/${settlementId}/payments`);
    list.innerHTML = items.map((p) => `
      <li class="${CARD} text-sm flex justify-between">
        <span>${p.amount} грн — ${LABEL[p.status]}</span>
        <button data-receipt="${p.id}" class="text-slate-700 underline">Квитанція</button>
      </li>`).join('');
    list.querySelectorAll('[data-receipt]').forEach((b) => b.addEventListener('click', () =>
      withMessage(container, () => download(`/payments/${b.dataset.receipt}/receipt`, 'receipt', { open: true }))));
  };
  container.querySelector('#pay').addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const r = await withMessage(container, () => upload(`/settlements/${settlementId}/payments`, fd), 'Квитанцію надіслано');
    if (r) { e.target.reset(); await refresh(); }
  });
  await refresh();
}

async function renderManager(container) {
  container.innerHTML = '<div data-dorm class="w-72 mb-3"></div><p data-msg class="mb-2"></p><ul id="list" class="space-y-2"></ul>';
  let dormitoryId = null;
  const list = container.querySelector('#list');
  const load = async () => {
    const items = await withMessage(container, () => api(`/payments?dormitoryId=${dormitoryId}&status=PENDING`));
    list.innerHTML = (items ?? []).map((p) => `
      <li class="${CARD} text-sm flex flex-wrap gap-2 items-center justify-between">
        <span>Платіж №${p.id}, договір ${p.settlementId}: <b>${esc(String(p.amount))} грн</b></span>
        <span class="flex gap-2">
          <button data-receipt="${p.id}" class="underline">Квитанція</button>
          <button data-set="VERIFIED" data-id="${p.id}" class="${BTN}">Підтвердити</button>
          <button data-set="REJECTED" data-id="${p.id}" class="${BTN}">Відхилити</button>
        </span>
      </li>`).join('') || '<li class="text-sm text-slate-500">Платежів на перевірку немає</li>';
    list.querySelectorAll('[data-receipt]').forEach((b) => b.addEventListener('click', () =>
      withMessage(container, () => download(`/payments/${b.dataset.receipt}/receipt`, 'receipt', { open: true }))));
    list.querySelectorAll('[data-set]').forEach((b) => b.addEventListener('click', async () => {
      await withMessage(container, () => api(`/payments/${b.dataset.id}/status`, { method: 'PATCH', body: { status: b.dataset.set } }), 'Статус оновлено');
      await load();
    }));
  };
  await dormitorySelect(container, async (id) => { dormitoryId = id; await load(); });
}
