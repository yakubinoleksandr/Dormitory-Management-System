// FR-18: експорт звітів у Excel і PDF.
import { download } from '../api.js';
import { dormitorySelect, withMessage, CARD } from '../ui.js';

export async function renderReports(container) {
  container.innerHTML = `
    <div class="max-w-3xl mx-auto space-y-6">
      <div class="bg-white p-4 rounded-xl shadow-sm border border-slate-200 w-80" data-dorm></div>
      <p data-msg></p>

      <div class="space-y-4">
        ${[
          ['residents', 'Список проживаючих мешканців', 'Повний перелік студентів із зазначенням кімнат, ліжко-місць та номерів договорів', 'fa-users'],
          ['free-beds', 'Звіт по вільних ліжко-місцях', 'Статистика завантаженості поверхів та перелік доступних місць для поселення', 'fa-bed'],
        ].map(([type, title, desc, icon]) => `
          <div class="${CARD} flex flex-wrap items-center justify-between gap-4 hover:shadow-md transition-shadow">
            <div class="flex items-start gap-4">
              <div class="w-11 h-11 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center text-lg shrink-0">
                <i class="fa-solid ${icon}"></i>
              </div>
              <div>
                <h3 class="font-bold text-slate-800">${title}</h3>
                <p class="text-xs text-slate-500 mt-0.5">${desc}</p>
              </div>
            </div>
            <div class="flex gap-2">
              <button data-type="${type}" data-format="xlsx" class="bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg px-4 py-2 text-sm font-medium transition-colors shadow-sm flex items-center gap-2">
                <i class="fa-solid fa-file-excel"></i> Excel
              </button>
              <button data-type="${type}" data-format="pdf" class="bg-rose-600 hover:bg-rose-700 text-white rounded-lg px-4 py-2 text-sm font-medium transition-colors shadow-sm flex items-center gap-2">
                <i class="fa-solid fa-file-pdf"></i> PDF
              </button>
            </div>
          </div>`).join('')}
      </div>

      <div class="bg-slate-100 border border-slate-200 rounded-xl p-4 text-xs text-slate-600 flex items-center gap-2.5">
        <i class="fa-solid fa-circle-info text-slate-400 text-base"></i>
        <span>Примітка: автоматичний список боржників буде додано у наступній версії системи після інтеграції модулів тарифів та нарахувань.</span>
      </div>
    </div>`;

  let dormitoryId = null;
  container.querySelectorAll('[data-type]').forEach((b) => b.addEventListener('click', () =>
    withMessage(container, () => download(
      `/reports/${b.dataset.type}/export?dormitoryId=${dormitoryId}&format=${b.dataset.format}`,
      `${b.dataset.type}.${b.dataset.format}`))));
  await dormitorySelect(container, (id) => { dormitoryId = id; });
}