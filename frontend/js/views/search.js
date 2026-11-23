// FR-17: глобальний пошук; FR-09: редагування контактних даних знайденого мешканця.
import { api } from '../api.js';
import { esc, withMessage, BTN, INPUT, CARD } from '../ui.js';

export async function renderSearch(container) {
  container.innerHTML = `
    <form id="q" class="flex gap-2 mb-3 max-w-xl">
      <input name="q" minlength="2" placeholder="ПІБ, кімната, договір, гуртожиток" required class="${INPUT}">
      <button class="${BTN}">Шукати</button>
    </form>
    <p data-msg class="mb-2"></p><div id="out" class="space-y-4"></div>`;
  const out = container.querySelector('#out');

  container.querySelector('#q').addEventListener('submit', async (e) => {
    e.preventDefault();
    const q = new FormData(e.target).get('q');
    const r = await withMessage(container, () => api(`/search?q=${encodeURIComponent(q)}`));
    if (!r) return;
    out.innerHTML = `
      <section><h3 class="font-semibold mb-1">Мешканці (${r.residents.length})</h3>
        <ul class="space-y-1 text-sm">${r.residents.map((h) => `
          <li class="${CARD}">${esc(h.fullName)} — договір ${esc(h.contractNumber)}, кімната ${esc(h.roomNumber)}, ${esc(h.dormitoryName)} (${h.status})
            <button data-edit="${h.accountId}" class="underline ml-2">Редагувати</button></li>`).join('')}</ul></section>
      <section><h3 class="font-semibold mb-1">Кімнати (${r.rooms.length})</h3>
        <ul class="space-y-1 text-sm">${r.rooms.map((h) => `<li class="${CARD}">${esc(h.roomNumber)} — ${esc(h.dormitoryName)}</li>`).join('')}</ul></section>
      <section><h3 class="font-semibold mb-1">Гуртожитки (${r.dormitories.length})</h3>
        <ul class="space-y-1 text-sm">${r.dormitories.map((h) => `<li class="${CARD}">${esc(h.nameOrNumber)} — ${esc(h.address ?? '')}</li>`).join('')}</ul></section>
      <div id="editor"></div>`;
    out.querySelectorAll('[data-edit]').forEach((b) => b.addEventListener('click', () => edit(Number(b.dataset.edit), q)));
  });

  // Профіль читається через пошук лише за ПІБ/договором; для редагування запитуємо поточні значення у користувача.
  function edit(accountId) {
    const editor = out.querySelector('#editor');
    editor.innerHTML = `
      <form class="${CARD} space-y-2 max-w-md">
        <h3 class="font-semibold">Редагування профілю (акаунт ${accountId})</h3>
        <input name="fullName" placeholder="ПІБ" required class="${INPUT}">
        <input name="course" type="number" min="1" max="6" placeholder="Курс" class="${INPUT}">
        <input name="groupName" placeholder="Група" class="${INPUT}">
        <input name="phone" placeholder="Телефон" class="${INPUT}">
        <button class="${BTN}">Зберегти</button>
      </form>`;
    editor.querySelector('form').addEventListener('submit', async (e) => {
      e.preventDefault();
      const d = Object.fromEntries(new FormData(e.target));
      d.course = d.course ? Number(d.course) : null;
      await withMessage(container, () => api(`/residents/${accountId}/profile`, { method: 'PUT', body: d }), 'Профіль збережено');
    });
  }
}
