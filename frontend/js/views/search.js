// FR-17: глобальний пошук; FR-09: редагування контактних даних знайденого мешканця.
import { api } from '../api.js';
import { esc, withMessage, BTN, INPUT, CARD } from '../ui.js';

export async function renderSearch(container) {
  container.innerHTML = `
    <div class="max-w-4xl mx-auto space-y-6">
      <form id="q" class="${CARD} flex flex-col sm:flex-row gap-3">
        <div class="relative flex-1">
          <i class="fa-solid fa-magnifying-glass absolute left-3.5 top-3 text-slate-400"></i>
          <input name="q" minlength="2" placeholder="Введіть ПІБ мешканця, номер кімнати, договору або назву гуртожитку..." required class="${INPUT} pl-10">
        </div>
        <button class="${BTN} px-6"><i class="fa-solid fa-search"></i> Знайти</button>
      </form>

      <p data-msg></p>
      <div id="out" class="space-y-6"></div>
    </div>`;

  const out = container.querySelector('#out');

  container.querySelector('#q').addEventListener('submit', async (e) => {
    e.preventDefault();
    const q = new FormData(e.target).get('q');
    const r = await withMessage(container, () => api(`/search?q=${encodeURIComponent(q)}`));
    if (!r) return;

    out.innerHTML = `
      <div id="editor"></div>

      <section class="space-y-2.5">
        <h3 class="text-base font-bold text-slate-800 flex items-center gap-2">
          <i class="fa-solid fa-user-graduate text-blue-600"></i> Мешканці (${r.residents.length})
        </h3>
        ${r.residents.length ? r.residents.map((h) => `
          <div class="${CARD} flex flex-wrap justify-between items-center gap-3">
            <div>
              <div class="font-bold text-slate-800">${esc(h.fullName)}</div>
              <div class="text-xs text-slate-500 mt-0.5">
                Договір: <b>${esc(h.contractNumber)}</b> • Кімната: <b>${esc(h.roomNumber)}</b> • ${esc(h.dormitoryName)} (${h.status})
              </div>
            </div>
            <button data-edit="${h.accountId}" class="text-sm font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 px-3 py-1.5 rounded-lg transition-colors">
              <i class="fa-solid fa-pen-to-square mr-1"></i> Редагувати дані
            </button>
          </div>`).join('') : '<p class="text-sm text-slate-500 bg-white p-4 rounded-xl border border-slate-200">Мешканців не знайдено</p>'}
      </section>

      <div class="grid md:grid-cols-2 gap-6">
        <section class="space-y-2.5">
          <h3 class="text-base font-bold text-slate-800 flex items-center gap-2">
            <i class="fa-solid fa-door-closed text-emerald-600"></i> Кімнати (${r.rooms.length})
          </h3>
          ${r.rooms.length ? r.rooms.map((h) => `
            <div class="${CARD} text-sm flex justify-between items-center">
              <span class="font-bold text-slate-800">Кімната ${esc(h.roomNumber)}</span>
              <span class="text-slate-500">${esc(h.dormitoryName)}</span>
            </div>`).join('') : '<p class="text-sm text-slate-500 bg-white p-4 rounded-xl border border-slate-200">Кімнат не знайдено</p>'}
        </section>

        <section class="space-y-2.5">
          <h3 class="text-base font-bold text-slate-800 flex items-center gap-2">
            <i class="fa-solid fa-building text-purple-600"></i> Гуртожитки (${r.dormitories.length})
          </h3>
          ${r.dormitories.length ? r.dormitories.map((h) => `
            <div class="${CARD} text-sm">
              <div class="font-bold text-slate-800">${esc(h.nameOrNumber)}</div>
              <div class="text-xs text-slate-500">${esc(h.address ?? 'Адресу не вказано')}</div>
            </div>`).join('') : '<p class="text-sm text-slate-500 bg-white p-4 rounded-xl border border-slate-200">Гуртожитків не знайдено</p>'}
        </section>
      </div>`;

    out.querySelectorAll('[data-edit]').forEach((b) => b.addEventListener('click', () => edit(Number(b.dataset.edit))));
  });

  function edit(accountId) {
    const editor = out.querySelector('#editor');
    editor.innerHTML = `
      <form class="${CARD} border-l-4 border-l-blue-600 space-y-4 fade-in">
        <div class="flex justify-between items-center">
          <h3 class="text-base font-bold text-slate-800">Редагування профілю (акаунт ID: ${accountId})</h3>
          <button type="button" onclick="this.closest('form').remove()" class="text-slate-400 hover:text-slate-600"><i class="fa-solid fa-xmark"></i></button>
        </div>
        <div class="grid sm:grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">ПІБ мешканця</label>
            <input name="fullName" placeholder="Прізвище Ім'я По батькові" required class="${INPUT}">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Телефон</label>
            <input name="phone" placeholder="+380..." class="${INPUT}">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Курс</label>
            <input name="course" type="number" min="1" max="6" placeholder="1-6" class="${INPUT}">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Навчальна група</label>
            <input name="groupName" placeholder="ІО-44" class="${INPUT}">
          </div>
        </div>
        <button class="${BTN}"><i class="fa-solid fa-floppy-disk"></i> Зберегти зміни</button>
      </form>`;
    editor.querySelector('form').addEventListener('submit', async (e) => {
      e.preventDefault();
      const d = Object.fromEntries(new FormData(e.target));
      d.course = d.course ? Number(d.course) : null;
      await withMessage(container, () => api(`/residents/${accountId}/profile`, { method: 'PUT', body: d }), 'Профіль мешканця збережено');
    });
  }
}