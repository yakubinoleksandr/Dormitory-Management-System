// FR-18: експорт звітів у Excel і PDF.
import { download } from '../api.js';
import { dormitorySelect, withMessage, BTN, CARD } from '../ui.js';

export async function renderReports(container) {
  container.innerHTML = `
    <div data-dorm class="w-72 mb-3"></div><p data-msg class="mb-2"></p>
    <div class="${CARD} space-y-3 max-w-xl text-sm">
      ${[['residents', 'Список проживаючих'], ['free-beds', 'Вільні місця']].map(([type, title]) => `
        <div class="flex items-center justify-between"><span>${title}</span>
          <span class="flex gap-2">
            <button data-type="${type}" data-format="xlsx" class="${BTN}">Excel</button>
            <button data-type="${type}" data-format="pdf" class="${BTN}">PDF</button>
          </span></div>`).join('')}
      <p class="text-xs text-slate-500">Список боржників недоступний: у системі немає тарифів і нарахувань.</p>
    </div>`;
  let dormitoryId = null;
  container.querySelectorAll('[data-type]').forEach((b) => b.addEventListener('click', () =>
    withMessage(container, () => download(
      `/reports/${b.dataset.type}/export?dormitoryId=${dormitoryId}&format=${b.dataset.format}`,
      `${b.dataset.type}.${b.dataset.format}`))));
  await dormitorySelect(container, (id) => { dormitoryId = id; });
}
