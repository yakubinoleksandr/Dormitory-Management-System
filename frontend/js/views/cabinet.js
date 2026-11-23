// FR-10 (особистий кабінет) і FR-06 (подання заяви на поселення).
import { api } from '../api.js';
import { esc, formData, withMessage, BTN, INPUT, CARD } from '../ui.js';

export async function renderCabinet(container) {
  container.innerHTML = '<p data-msg class="mb-2"></p><div id="cab" class="space-y-4"></div>';
  const box = container.querySelector('#cab');
  const cab = await withMessage(container, () => api('/me/cabinet'));
  if (!cab) return;

  const p = cab.placement;
  box.innerHTML = `
    <div class="${CARD}">
      <h2 class="font-semibold mb-1">${esc(cab.fullName)}</h2>
      <p class="text-sm text-slate-600">Курс: ${cab.course ?? '—'}, група: ${esc(cab.groupName ?? '—')}</p>
    </div>
    ${p ? `
    <div class="${CARD} text-sm space-y-1">
      <h3 class="font-semibold">Поселення</h3>
      <div>Гуртожиток: <b>${esc(p.dormitoryName)}</b>, поверх ${p.floor}${p.blockName ? `, блок ${esc(p.blockName)}` : ''}</div>
      <div>Кімната: <b>${esc(p.roomNumber)}</b>, ліжко-місце ${p.bedNumber}</div>
      <div>Договір № <b>${esc(p.contractNumber)}</b> (${p.startDate} — ${p.endDate})</div>
      <div>Підтверджені оплати: <b>${cab.verifiedPaymentsTotal} грн</b></div>
      <div>Сусіди: ${cab.neighbours.length ? cab.neighbours.map(esc).join(', ') : 'немає'}</div>
    </div>` : `
    <form id="apply" class="${CARD} space-y-2 max-w-md">
      <h3 class="font-semibold">Заява на поселення</h3>
      <select name="dormitoryId" id="applyDorm" class="${INPUT}"></select>
      <button class="${BTN}">Подати заяву</button>
    </form>
    <div id="myApps" class="text-sm"></div>`}`;

  if (!p) {
    const dorms = await api('/dormitories');
    box.querySelector('#applyDorm').innerHTML = dorms.map((d) => `<option value="${d.id}">${esc(d.nameOrNumber)}</option>`).join('');
    const label = { PENDING: 'очікує розгляду', APPROVED: 'схвалено', REJECTED: 'відхилено' };
    const refresh = async () => {
      const apps = await api('/settlement-applications/mine');
      box.querySelector('#myApps').innerHTML = apps.map((a) => `<div>Заява №${a.id}: ${label[a.status]}</div>`).join('');
    };
    box.querySelector('#apply').addEventListener('submit', async (e) => {
      e.preventDefault();
      const d = formData(e.target);
      await withMessage(container, () => api('/settlement-applications', { method: 'POST', body: { dormitoryId: Number(d.dormitoryId) } }), 'Заяву подано');
      await refresh();
    });
    await refresh();
  }
}
