// Адміністратор: FR-01 (гуртожиток), FR-02 (генерація), FR-03 (завідуюча), FR-20 (акаунти).
import { api } from '../api.js';
import { esc, formData, withMessage, BTN, INPUT, CARD } from '../ui.js';

export async function renderAdmin(container) {
  container.innerHTML = `
    <div class="max-w-5xl mx-auto">
      <p data-msg></p>
      <div class="grid md:grid-cols-2 gap-6">
        
        <form id="dorm" class="${CARD} space-y-4 border-t-4 border-t-blue-600">
          <div class="flex items-center gap-3 border-b border-slate-100 pb-3">
            <div class="w-9 h-9 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center font-bold">
              <i class="fa-solid fa-building"></i>
            </div>
            <h2 class="text-lg font-bold text-slate-800">1. Новий гуртожиток</h2>
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Назва або номер</label>
            <input name="nameOrNumber" placeholder="Наприклад: Гуртожиток №7" required class="${INPUT}">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Адреса</label>
            <input name="address" placeholder="Вулиця, номер будинку" class="${INPUT}">
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Поверхів</label>
              <input name="totalFloors" type="number" min="1" max="50" placeholder="9" required class="${INPUT}">
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Тип планування</label>
              <select name="layoutType" class="${INPUT}">
                <option value="CORRIDOR">Коридорний</option>
                <option value="BLOCK">Блочний</option>
              </select>
            </div>
          </div>
          <button class="${BTN} w-full"><i class="fa-solid fa-plus"></i> Створити гуртожиток</button>
        </form>

        <form id="gen" class="${CARD} space-y-4 border-t-4 border-t-emerald-600">
          <div class="flex items-center gap-3 border-b border-slate-100 pb-3">
            <div class="w-9 h-9 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center font-bold">
              <i class="fa-solid fa-layer-group"></i>
            </div>
            <h2 class="text-lg font-bold text-slate-800">2. Генерація кімнат і місць</h2>
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Оберіть гуртожиток</label>
            <select name="dormitoryId" id="genDorm" class="${INPUT}"></select>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Поверхів (N)</label>
              <input name="floors" type="number" min="1" placeholder="5" required class="${INPUT}">
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Блоків на поверх (K)</label>
              <input name="blocksPerFloor" type="number" min="0" value="0" placeholder="0 для коридорного" class="${INPUT}">
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Кімнат на блок/поверх (M)</label>
              <input name="roomsPerUnit" type="number" min="1" placeholder="4" required class="${INPUT}">
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Ліжок у кімнаті (1–4)</label>
              <input name="capacity" type="number" min="1" max="4" value="4" required class="${INPUT}">
            </div>
          </div>
          <button class="${BTN} w-full"><i class="fa-solid fa-wand-magic-sparkles"></i> Згенерувати фонд</button>
        </form>

        <form id="acc" class="${CARD} space-y-4 border-t-4 border-t-amber-500">
          <div class="flex items-center gap-3 border-b border-slate-100 pb-3">
            <div class="w-9 h-9 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center font-bold">
              <i class="fa-solid fa-user-plus"></i>
            </div>
            <h2 class="text-lg font-bold text-slate-800">3. Обліковий запис</h2>
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Роль користувача</label>
            <select name="role" id="accRole" class="${INPUT}">
              <option value="ROLE_MANAGER">Завідуюча (Комендант)</option>
              <option value="ROLE_RESIDENT">Мешканець (Студент)</option>
            </select>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Логін</label>
              <input name="username" placeholder="ivanov_i" required class="${INPUT}">
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Email</label>
              <input name="email" type="email" placeholder="user@kpi.ua" required class="${INPUT}">
            </div>
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Первинний пароль</label>
            <input name="password" type="password" minlength="8" placeholder="Мінімум 8 символів" required class="${INPUT}">
          </div>
          <div id="resFields" class="space-y-3 hidden bg-slate-50 p-3.5 rounded-xl border border-slate-200">
            <input name="fullName" placeholder="ПІБ студента" class="${INPUT}">
            <div class="grid grid-cols-2 gap-2">
              <input name="course" type="number" min="1" max="6" placeholder="Курс" class="${INPUT}">
              <input name="groupName" placeholder="Група (ІО-44)" class="${INPUT}">
            </div>
            <input name="phone" placeholder="Номер телефону" class="${INPUT}">
          </div>
          <button class="${BTN} w-full"><i class="fa-solid fa-check"></i> Створити акаунт</button>
          <p id="accResult" class="text-xs font-medium text-emerald-700 bg-emerald-50 p-2 rounded-lg hidden"></p>
        </form>

        <form id="assign" class="${CARD} space-y-4 border-t-4 border-t-purple-600 h-fit">
          <div class="flex items-center gap-3 border-b border-slate-100 pb-3">
            <div class="w-9 h-9 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center font-bold">
              <i class="fa-solid fa-user-tie"></i>
            </div>
            <h2 class="text-lg font-bold text-slate-800">4. Призначити завідуючу</h2>
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Гуртожиток</label>
            <select name="dormitoryId" id="asgDorm" class="${INPUT}"></select>
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">ID акаунта завідуючої</label>
            <input name="managerAccountId" type="number" min="1" placeholder="Введіть числовий ID" required class="${INPUT}">
          </div>
          <button class="${BTN} w-full"><i class="fa-solid fa-link"></i> Закріпити за гуртожитком</button>
        </form>

      </div>
    </div>`;

  const refreshDorms = async () => {
    const dorms = await api('/dormitories');
    const options = dorms.map((d) => `<option value="${d.id}">${esc(d.nameOrNumber)} (${d.layoutType === 'BLOCK' ? 'блочний' : 'коридорний'})</option>`).join('');
    container.querySelector('#genDorm').innerHTML = options;
    container.querySelector('#asgDorm').innerHTML = options;
  };
  await refreshDorms();

  const num = (v) => (v === '' || v == null ? null : Number(v));

  container.querySelector('#dorm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const d = formData(e.target);
    d.totalFloors = Number(d.totalFloors);
    const r = await withMessage(container, () => api('/dormitories', { method: 'POST', body: d }), 'Гуртожиток успішно створено');
    if (r) { e.target.reset(); await refreshDorms(); }
  });

  container.querySelector('#gen').addEventListener('submit', async (e) => {
    e.preventDefault();
    const d = formData(e.target);
    const id = d.dormitoryId;
    const body = { floors: +d.floors, blocksPerFloor: +d.blocksPerFloor, roomsPerUnit: +d.roomsPerUnit, capacity: +d.capacity };
    const r = await withMessage(container, () => api(`/dormitories/${id}/rooms/generate`, { method: 'POST', body }));
    if (r) {
      const msg = container.querySelector('[data-msg]');
      msg.innerHTML = `<i class="fa-solid fa-circle-check mr-1.5"></i>Успішно згенеровано кімнат: ${r.roomsCreated}`;
      msg.className = 'text-sm px-4 py-2.5 rounded-lg mb-4 font-medium border bg-emerald-50 text-emerald-700 border-emerald-200';
    }
  });

  const role = container.querySelector('#accRole');
  role.addEventListener('change', () =>
    container.querySelector('#resFields').classList.toggle('hidden', role.value !== 'ROLE_RESIDENT'));

  container.querySelector('#acc').addEventListener('submit', async (e) => {
    e.preventDefault();
    const d = formData(e.target);
    d.course = num(d.course);
    const r = await withMessage(container, () => api('/accounts', { method: 'POST', body: d }), 'Обліковий запис створено');
    if (r) {
      const resBox = container.querySelector('#accResult');
      resBox.textContent = `Створено акаунт ID ${r.id} (${r.username}). Передайте первинний пароль користувачу особисто.`;
      resBox.classList.remove('hidden');
      e.target.reset();
      container.querySelector('#resFields').classList.add('hidden');
    }
  });

  container.querySelector('#assign').addEventListener('submit', async (e) => {
    e.preventDefault();
    const d = formData(e.target);
    await withMessage(container,
      () => api(`/dormitories/${d.dormitoryId}/managers`, { method: 'POST', body: { managerAccountId: Number(d.managerAccountId) } }),
      'Завідуючу успішно призначено');
  });
}