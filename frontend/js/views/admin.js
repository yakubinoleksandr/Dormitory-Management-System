// Адміністратор: FR-01 (гуртожиток), FR-02 (генерація), FR-03 (завідуюча), FR-20 (акаунти).
import { api } from '../api.js';
import { esc, formData, withMessage, BTN, INPUT, CARD } from '../ui.js';

export async function renderAdmin(container) {
  container.innerHTML = `
    <p data-msg class="mb-3"></p>
    <div class="grid md:grid-cols-2 gap-4">
      <form id="dorm" class="${CARD} space-y-2">
        <h2 class="font-semibold">1. Новий гуртожиток</h2>
        <input name="nameOrNumber" placeholder="Назва або номер" required class="${INPUT}">
        <input name="address" placeholder="Адреса" class="${INPUT}">
        <input name="totalFloors" type="number" min="1" max="50" placeholder="Поверхів" required class="${INPUT}">
        <select name="layoutType" class="${INPUT}">
          <option value="CORRIDOR">Коридорний</option><option value="BLOCK">Блочний</option>
        </select>
        <button class="${BTN}">Створити</button>
      </form>

      <form id="gen" class="${CARD} space-y-2">
        <h2 class="font-semibold">2. Генерація кімнат і місць</h2>
        <select name="dormitoryId" id="genDorm" class="${INPUT}"></select>
        <input name="floors" type="number" min="1" placeholder="Поверхів (N)" required class="${INPUT}">
        <input name="blocksPerFloor" type="number" min="0" value="0" placeholder="Блоків на поверсі (K, лише блочний)" class="${INPUT}">
        <input name="roomsPerUnit" type="number" min="1" placeholder="Кімнат на поверх / на блок (M)" required class="${INPUT}">
        <input name="capacity" type="number" min="1" max="4" value="4" placeholder="Місць у кімнаті (1–4)" required class="${INPUT}">
        <button class="${BTN}">Згенерувати</button>
      </form>

      <form id="acc" class="${CARD} space-y-2">
        <h2 class="font-semibold">3. Обліковий запис</h2>
        <select name="role" id="accRole" class="${INPUT}">
          <option value="ROLE_MANAGER">Завідуюча</option><option value="ROLE_RESIDENT">Мешканець</option>
        </select>
        <input name="username" placeholder="Логін" required class="${INPUT}">
        <input name="email" type="email" placeholder="Email" required class="${INPUT}">
        <input name="password" type="password" minlength="8" placeholder="Первинний пароль (від 8 символів)" required class="${INPUT}">
        <div id="resFields" class="space-y-2 hidden">
          <input name="fullName" placeholder="ПІБ" class="${INPUT}">
          <input name="course" type="number" min="1" max="6" placeholder="Курс" class="${INPUT}">
          <input name="groupName" placeholder="Група" class="${INPUT}">
          <input name="phone" placeholder="Телефон" class="${INPUT}">
        </div>
        <button class="${BTN}">Створити акаунт</button>
        <p id="accResult" class="text-xs text-slate-500"></p>
      </form>

      <form id="assign" class="${CARD} space-y-2">
        <h2 class="font-semibold">4. Призначити завідуючу</h2>
        <select name="dormitoryId" id="asgDorm" class="${INPUT}"></select>
        <input name="managerAccountId" type="number" min="1" placeholder="ID акаунта завідуючої" required class="${INPUT}">
        <button class="${BTN}">Призначити</button>
      </form>
    </div>`;

  const refreshDorms = async () => {
    const dorms = await api('/dormitories');
    const options = dorms.map((d) => `<option value="${d.id}">${esc(d.nameOrNumber)} — ${d.layoutType}</option>`).join('');
    container.querySelector('#genDorm').innerHTML = options;
    container.querySelector('#asgDorm').innerHTML = options;
  };
  await refreshDorms();

  const num = (v) => (v === '' || v == null ? null : Number(v));

  container.querySelector('#dorm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const d = formData(e.target);
    d.totalFloors = Number(d.totalFloors);
    const r = await withMessage(container, () => api('/dormitories', { method: 'POST', body: d }), 'Гуртожиток створено');
    if (r) { e.target.reset(); await refreshDorms(); }
  });

  container.querySelector('#gen').addEventListener('submit', async (e) => {
    e.preventDefault();
    const d = formData(e.target);
    const id = d.dormitoryId;
    const body = { floors: +d.floors, blocksPerFloor: +d.blocksPerFloor, roomsPerUnit: +d.roomsPerUnit, capacity: +d.capacity };
    const r = await withMessage(container, () => api(`/dormitories/${id}/rooms/generate`, { method: 'POST', body }));
    if (r) container.querySelector('[data-msg]').textContent = `Створено кімнат: ${r.roomsCreated}`;
  });

  const role = container.querySelector('#accRole');
  role.addEventListener('change', () =>
    container.querySelector('#resFields').classList.toggle('hidden', role.value !== 'ROLE_RESIDENT'));

  container.querySelector('#acc').addEventListener('submit', async (e) => {
    e.preventDefault();
    const d = formData(e.target);
    d.course = num(d.course);
    const r = await withMessage(container, () => api('/accounts', { method: 'POST', body: d }));
    if (r) {
      container.querySelector('#accResult').textContent = `Створено акаунт ID ${r.id} (${r.username}). Передайте пароль особисто.`;
      e.target.reset();
    }
  });

  container.querySelector('#assign').addEventListener('submit', async (e) => {
    e.preventDefault();
    const d = formData(e.target);
    await withMessage(container,
      () => api(`/dormitories/${d.dormitoryId}/managers`, { method: 'POST', body: { managerAccountId: Number(d.managerAccountId) } }),
      'Завідуючу призначено');
  });
}
