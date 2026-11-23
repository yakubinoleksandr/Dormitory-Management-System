// FR-16: мешканець подає квитанцію, завідуюча перевіряє.
import { api, upload, download } from '../api.js';
import { getRole } from '../auth.js';
import { esc, dormitorySelect, withMessage, BTN, INPUT, CARD } from '../ui.js';

const getStatusBadge = (status) => {
  if (status === 'PENDING') return '<span class="px-2.5 py-1 bg-amber-100 text-amber-700 rounded-full text-xs font-semibold">Очікує перевірки</span>';
  if (status === 'VERIFIED') return '<span class="px-2.5 py-1 bg-emerald-100 text-emerald-700 rounded-full text-xs font-semibold">Підтверджено</span>';
  return '<span class="px-2.5 py-1 bg-rose-100 text-rose-700 rounded-full text-xs font-semibold">Відхилено</span>';
};

export async function renderPayments(container) {
  return getRole() === 'ROLE_MANAGER' ? renderManager(container) : renderResident(container);
}

async function renderResident(container) {
  const cab = await api('/me/cabinet');
  const settlementId = cab.placement?.settlementId;
  if (!settlementId) {
    container.innerHTML = '<p class="text-sm text-slate-600 bg-amber-50 border border-amber-200 p-4 rounded-xl max-w-2xl mx-auto">Завантаження квитанцій стане доступним після поселення в гуртожиток.</p>';
    return;
  }

  container.innerHTML = `
    <div class="max-w-4xl mx-auto space-y-6">
      <p data-msg></p>
      <form id="pay" class="${CARD} border-l-4 border-l-blue-600 space-y-4">
        <h3 class="text-lg font-bold text-slate-800 flex items-center gap-2">
          <i class="fa-solid fa-file-arrow-up text-blue-600"></i> Завантажити нову квитанцію про оплату
        </h3>
        <div class="grid sm:grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Сума оплати (грн)</label>
            <input name="amount" type="number" step="0.01" min="0.01" placeholder="1500.00" required class="${INPUT}">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Спосіб оплати</label>
            <input name="method" placeholder="Приват24 / Монобанк / Каса" class="${INPUT}">
          </div>
        </div>
        <div>
          <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Файл квитанції (PDF, JPG, PNG)</label>
          <input name="receipt" type="file" accept="application/pdf,image/jpeg,image/png" required class="${INPUT} file:mr-4 file:py-1 file:px-3 file:rounded-md file:border-0 file:text-xs file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100">
        </div>
        <div class="flex justify-end">
          <button class="${BTN}"><i class="fa-solid fa-paper-plane"></i> Надіслати на перевірку</button>
        </div>
      </form>

      <div>
        <h3 class="text-lg font-bold text-slate-800 mb-3">Історія моїх оплат</h3>
        <div id="list" class="space-y-2.5"></div>
      </div>
    </div>`;

  const list = container.querySelector('#list');
  const refresh = async () => {
    const items = await api(`/settlements/${settlementId}/payments`);
    list.innerHTML = items.length ? items.map((p) => `
      <div class="${CARD} flex flex-wrap justify-between items-center gap-4">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-slate-100 text-slate-600 flex items-center justify-center">
            <i class="fa-solid fa-receipt"></i>
          </div>
          <div>
            <div class="font-bold text-slate-800 text-base">${esc(String(p.amount))} ₴</div>
            <div class="text-xs text-slate-500">${p.method ? esc(p.method) : 'Оплата за проживання'}</div>
          </div>
        </div>
        <div class="flex items-center gap-4">
          ${getStatusBadge(p.status)}
          <button data-receipt="${p.id}" class="text-sm font-medium text-blue-600 hover:text-blue-800 flex items-center gap-1.5">
            <i class="fa-solid fa-eye"></i> Квитанція
          </button>
        </div>
      </div>`).join('') : '<p class="text-sm text-slate-500 text-center py-6 bg-white rounded-xl border border-slate-200">Ви ще не завантажували квитанцій</p>';

    list.querySelectorAll('[data-receipt]').forEach((b) => b.addEventListener('click', () =>
      withMessage(container, () => download(`/payments/${b.dataset.receipt}/receipt`, 'receipt', { open: true }))));
  };

  container.querySelector('#pay').addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const r = await withMessage(container, () => upload(`/settlements/${settlementId}/payments`, fd), 'Квитанцію надіслано на перевірку');
    if (r) { e.target.reset(); await refresh(); }
  });
  await refresh();
}

async function renderManager(container) {
  container.innerHTML = `
    <div class="max-w-4xl mx-auto space-y-6">
      <div class="bg-white p-4 rounded-xl shadow-sm border border-slate-200 w-80" data-dorm></div>
      <p data-msg></p>
      <h3 class="text-lg font-bold text-slate-800">Квитанції, що очікують верифікації</h3>
      <div id="list" class="space-y-3"></div>
    </div>`;

  let dormitoryId = null;
  const list = container.querySelector('#list');
  const load = async () => {
    const items = await withMessage(container, () => api(`/payments?dormitoryId=${dormitoryId}&status=PENDING`));
    list.innerHTML = (items ?? []).map((p) => `
      <div class="${CARD} flex flex-wrap gap-4 items-center justify-between">
        <div>
          <div class="flex items-center gap-2">
            <span class="font-bold text-slate-800">Платіж №${p.id}</span>
            <span class="text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded font-medium">Договір ID: ${p.settlementId}</span>
          </div>
          <div class="text-lg font-bold text-emerald-600 mt-1">${esc(String(p.amount))} ₴</div>
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <button data-receipt="${p.id}" class="text-sm font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 px-3 py-2 rounded-lg transition-colors">
            <i class="fa-solid fa-file-pdf mr-1"></i> Переглянути чек
          </button>
          <button data-set="VERIFIED" data-id="${p.id}" class="bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg px-3.5 py-2 text-sm font-medium transition-colors">
            <i class="fa-solid fa-check mr-1"></i> Підтвердити
          </button>
          <button data-set="REJECTED" data-id="${p.id}" class="bg-rose-600 hover:bg-rose-700 text-white rounded-lg px-3.5 py-2 text-sm font-medium transition-colors">
            <i class="fa-solid fa-xmark mr-1"></i> Відхилити
          </button>
        </div>
      </div>`).join('') || '<p class="text-sm text-slate-500 text-center py-8 bg-white rounded-xl border border-slate-200">Нових платежів на перевірку немає</p>';

    list.querySelectorAll('[data-receipt]').forEach((b) => b.addEventListener('click', () =>
      withMessage(container, () => download(`/payments/${b.dataset.receipt}/receipt`, 'receipt', { open: true }))));
    list.querySelectorAll('[data-set]').forEach((b) => b.addEventListener('click', async () => {
      await withMessage(container, () => api(`/payments/${b.dataset.id}/status`, { method: 'PATCH', body: { status: b.dataset.set } }), 'Статус платежу оновлено');
      await load();
    }));
  };
  await dormitorySelect(container, async (id) => { dormitoryId = id; await load(); });
}