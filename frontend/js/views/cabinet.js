// FR-10 (особистий кабінет) і FR-06 (подання заяви на поселення).
import { api } from '../api.js';
import { esc, formData, withMessage, BTN, INPUT, CARD } from '../ui.js';

export async function renderCabinet(container) {
  container.innerHTML = '<p data-msg></p><div id="cab" class="max-w-4xl mx-auto space-y-6"></div>';
  const box = container.querySelector('#cab');
  const cab = await withMessage(container, () => api('/me/cabinet'));
  if (!cab) return;

  const p = cab.placement;
  const initial = (cab.fullName || 'С').trim().charAt(0).toUpperCase();

  box.innerHTML = `
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
      <div class="bg-blue-600 p-6 text-white flex items-center gap-5">
        <div class="w-20 h-20 bg-white rounded-full flex items-center justify-center text-blue-600 text-3xl font-bold shadow-inner shrink-0">
          ${esc(initial)}
        </div>
        <div>
          <h2 class="text-2xl font-bold">${esc(cab.fullName)}</h2>
          <p class="opacity-90 text-sm mt-1">
            Студент • Курс: ${cab.course ?? '—'}, група: ${esc(cab.groupName ?? '—')}
            ${p ? ` • Договір № ${esc(p.contractNumber)}` : ''}
          </p>
        </div>
      </div>

      ${p ? `
      <div class="p-6 grid grid-cols-1 md:grid-cols-2 gap-8">
        <div class="space-y-4">
          <h3 class="text-lg font-bold text-slate-800 flex items-center gap-2">
            <i class="fa-solid fa-house text-blue-500"></i> Дані про поселення
          </h3>
          <ul class="space-y-3 bg-slate-50 p-4 rounded-xl border border-slate-100 text-sm">
            <li class="flex justify-between"><span class="text-slate-500">Гуртожиток:</span><span class="font-bold text-slate-800">${esc(p.dormitoryName)}</span></li>
            <li class="flex justify-between"><span class="text-slate-500">Поверх / Блок:</span><span class="font-semibold text-slate-800">${p.floor}${p.blockName ? ` (блок ${esc(p.blockName)})` : ''}</span></li>
            <li class="flex justify-between"><span class="text-slate-500">Кімната / Ліжко:</span><span class="font-bold text-slate-800">Кім. ${esc(p.roomNumber)}, місце ${p.bedNumber}</span></li>
            <li class="flex justify-between"><span class="text-slate-500">Термін дії:</span><span class="font-medium text-slate-700">${p.startDate} — ${p.endDate}</span></li>
            <li class="flex justify-between items-center"><span class="text-slate-500">Статус:</span><span class="font-semibold text-emerald-700 bg-emerald-100 px-2.5 py-0.5 rounded-full text-xs">Проживає</span></li>
          </ul>

          <h3 class="text-lg font-bold text-slate-800 flex items-center gap-2 pt-2">
            <i class="fa-solid fa-user-group text-blue-500"></i> Сусіди по кімнаті
          </h3>
          ${cab.neighbours.length ? `
          <ul class="space-y-2">
            ${cab.neighbours.map(mate => `
              <li class="bg-white border border-slate-200 p-3 rounded-xl flex items-center gap-3 shadow-sm">
                <div class="w-8 h-8 bg-blue-50 text-blue-600 rounded-full flex items-center justify-center font-bold text-sm">
                  ${esc(mate.charAt(0))}
                </div>
                <span class="text-slate-700 font-medium text-sm">${esc(mate)}</span>
              </li>`).join('')}
          </ul>` : '<p class="text-sm text-slate-500 bg-slate-50 p-3 rounded-xl">Сусідів поки немає</p>'}
        </div>

        <div class="space-y-4">
          <h3 class="text-lg font-bold text-slate-800 flex items-center gap-2">
            <i class="fa-solid fa-wallet text-blue-500"></i> Фінансовий стан
          </h3>
          <div class="p-6 rounded-2xl text-center shadow-sm bg-emerald-50 border border-emerald-200">
            <p class="text-sm font-medium mb-1 text-slate-600">Підтверджено оплат за проживання</p>
            <h4 class="text-4xl font-bold text-emerald-600 my-2">${cab.verifiedPaymentsTotal} ₴</h4>
            <a href="#/payments" class="${BTN} w-full mt-4">
              <i class="fa-solid fa-file-invoice-dollar"></i> Керувати квитанціями
            </a>
          </div>
        </div>
      </div>` : `
      <div class="p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
        <form id="apply" class="bg-slate-50 p-5 rounded-xl border border-slate-200 space-y-4">
          <h3 class="text-lg font-bold text-slate-800">Подати заяву на поселення</h3>
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Оберіть гуртожиток</label>
            <select name="dormitoryId" id="applyDorm" class="${INPUT}"></select>
          </div>
          <button class="${BTN} w-full"><i class="fa-solid fa-paper-plane"></i> Подати заяву</button>
        </form>
        <div class="bg-white p-5 rounded-xl border border-slate-200 space-y-3">
          <h3 class="text-lg font-bold text-slate-800">Мої подані заяви</h3>
          <div id="myApps" class="space-y-2 text-sm"></div>
        </div>
      </div>`}
    </div>`;

  if (!p) {
    const dorms = await api('/dormitories');
    box.querySelector('#applyDorm').innerHTML = dorms.map((d) => `<option value="${d.id}">${esc(d.nameOrNumber)}</option>`).join('');
    const label = {
      PENDING: '<span class="px-2.5 py-0.5 bg-amber-100 text-amber-700 rounded-full text-xs font-semibold">Очікує розгляду</span>',
      APPROVED: '<span class="px-2.5 py-0.5 bg-emerald-100 text-emerald-700 rounded-full text-xs font-semibold">Схвалено</span>',
      REJECTED: '<span class="px-2.5 py-0.5 bg-rose-100 text-rose-700 rounded-full text-xs font-semibold">Відхилено</span>'
    };
    const refresh = async () => {
      const apps = await api('/settlement-applications/mine');
      box.querySelector('#myApps').innerHTML = apps.length
        ? apps.map((a) => `<div class="p-3 bg-slate-50 rounded-lg flex justify-between items-center border border-slate-100">
            <span class="font-medium">Заява №${a.id}</span> ${label[a.status]}
          </div>`).join('')
        : '<p class="text-slate-500">Ви ще не подавали заяв на поселення.</p>';
    };
    box.querySelector('#apply').addEventListener('submit', async (e) => {
      e.preventDefault();
      const d = formData(e.target);
      await withMessage(container, () => api('/settlement-applications', { method: 'POST', body: { dormitoryId: Number(d.dormitoryId) } }), 'Заяву успішно подано');
      await refresh();
    });
    await refresh();
  }
}