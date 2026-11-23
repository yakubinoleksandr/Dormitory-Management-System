// FR-04/FR-05: навігація по поверхах і сітка кімнат із колірною індикацією; колір дублюється текстом (NFR-05).
// Для завідуючої: вибір кімнати → вибір вільного місця → поселення за схваленою заявкою (FR-07).
import { api } from '../api.js';
import { getRole } from '../auth.js';
import { esc, dormitorySelect, withMessage, BTN, INPUT, CARD } from '../ui.js';

const STATUS = {
  FREE:    { cls: 'bg-emerald-50 border-emerald-500 text-emerald-900', label: 'Вільна' },
  PARTIAL: { cls: 'bg-amber-50 border-amber-500 text-amber-900',       label: 'Частково' },
  FULL:    { cls: 'bg-rose-50 border-rose-500 text-rose-900',          label: 'Зайнята' },
  REPAIR:  { cls: 'bg-slate-100 border-slate-400 text-slate-700',      label: 'На ремонті' },
};

export async function renderRooms(container) {
  const isManager = getRole() === 'ROLE_MANAGER';
  container.innerHTML = `
    <div id="stats" class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6"></div>

    <div class="bg-white p-4 rounded-xl shadow-sm border border-slate-200 flex flex-wrap gap-4 mb-6 items-end">
      <div data-dorm class="w-72"></div>
      <div class="w-48">
        <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Поверх</label>
        <select id="floor" class="${INPUT}"><option value="">Усі поверхи</option></select>
      </div>
    </div>

    <p data-msg></p>
    <h3 class="text-lg font-bold text-slate-800 border-b border-slate-200 pb-2 mb-4">План кімнат</h3>
    <div id="grid" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4"></div>
    <div id="panel" class="mt-6"></div>`;

  let dormitoryId = null;
  let rooms = [];
  const floorSelect = container.querySelector('#floor');
  const grid = container.querySelector('#grid');
  const stats = container.querySelector('#stats');

  const drawStats = () => {
    stats.innerHTML = `
      <div class="bg-white p-4 rounded-xl shadow-sm border border-slate-200 flex flex-col items-center">
        <span class="text-3xl font-bold text-slate-800">${rooms.length}</span>
        <span class="text-xs font-medium text-slate-500 mt-1">Всього кімнат</span>
      </div>
      <div class="bg-emerald-50 p-4 rounded-xl border border-emerald-200 flex flex-col items-center">
        <span class="text-3xl font-bold text-emerald-600">${rooms.filter(r => r.status === 'FREE').length}</span>
        <span class="text-xs font-medium text-emerald-700 mt-1">Вільних</span>
      </div>
      <div class="bg-rose-50 p-4 rounded-xl border border-rose-200 flex flex-col items-center">
        <span class="text-3xl font-bold text-rose-600">${rooms.filter(r => r.status === 'FULL').length}</span>
        <span class="text-xs font-medium text-rose-700 mt-1">Повністю зайнятих</span>
      </div>
      <div class="bg-amber-50 p-4 rounded-xl border border-amber-200 flex flex-col items-center">
        <span class="text-3xl font-bold text-amber-600">${rooms.filter(r => r.status === 'PARTIAL').length}</span>
        <span class="text-xs font-medium text-amber-700 mt-1">Частково зайнятих</span>
      </div>`;
  };

  const draw = () => {
    drawStats();
    const floor = floorSelect.value;
    grid.innerHTML = rooms.filter((r) => !floor || r.floor === Number(floor)).map(card).join('');
    if (isManager) {
      grid.querySelectorAll('[data-room]').forEach((el) =>
        el.addEventListener('click', () => openRoom(Number(el.dataset.room))));
    }
  };

  const load = async () => {
    await withMessage(container, async () => {
      rooms = await api(`/dormitories/${dormitoryId}/rooms`);
      const floors = [...new Set(rooms.map((r) => r.floor))];
      floorSelect.innerHTML = '<option value="">Усі поверхи</option>' +
        floors.map((f) => `<option value="${f}">Поверх ${f}</option>`).join('');
      draw();
    });
  };

  async function openRoom(roomId) {
    const room = rooms.find((r) => r.id === roomId);
    const panel = container.querySelector('#panel');
    const [beds, apps] = await Promise.all([
      api(`/dormitories/${dormitoryId}/rooms/${roomId}/beds`),
      api(`/settlement-applications?dormitoryId=${dormitoryId}&status=PENDING`),
    ]);
    panel.innerHTML = `
      <form id="settle" class="${CARD} border-l-4 border-l-blue-600 space-y-4 max-w-xl fade-in">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-bold text-slate-800">Кімната ${esc(room.roomNumber)} — поселення мешканця</h3>
          <button type="button" onclick="this.closest('form').remove()" class="text-slate-400 hover:text-slate-600"><i class="fa-solid fa-xmark"></i></button>
        </div>
        <div>
          <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Ліжко-місце</label>
          <select name="bedId" class="${INPUT}">
            ${beds.map((b) => `<option value="${b.id}" ${b.occupied ? 'disabled' : ''}>Ліжко ${b.bedNumber}${b.occupied ? ' (зайняте)' : ''}</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Заява студента</label>
          <select name="applicationId" class="${INPUT}" required>
            <option value="">Оберіть заяву мешканця</option>
            ${apps.map((a) => `<option value="${a.id}">${esc(a.fullName)} (заява №${a.id})</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Номер договору</label>
          <input name="contractNumber" placeholder="Наприклад: № 44-2026/ІО" required class="${INPUT}">
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Початок проживання</label>
            <input name="startDate" type="date" required class="${INPUT}">
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Кінець проживання</label>
            <input name="endDate" type="date" required class="${INPUT}">
          </div>
        </div>
        <button class="${BTN}"><i class="fa-solid fa-user-check"></i> Поселити мешканця</button>
      </form>`;
    panel.querySelector('#settle').addEventListener('submit', async (e) => {
      e.preventDefault();
      const d = Object.fromEntries(new FormData(e.target));
      d.applicationId = Number(d.applicationId);
      d.bedId = Number(d.bedId);
      const r = await withMessage(container, () => api('/settlements', { method: 'POST', body: d }), 'Мешканця успішно поселено');
      if (r) { panel.innerHTML = ''; await load(); }
    });
  }

  floorSelect.addEventListener('change', draw);
  await dormitorySelect(container, async (id) => { dormitoryId = id; container.querySelector('#panel').innerHTML = ''; await load(); });
}

function card(room) {
  const s = STATUS[room.status] || STATUS.FREE;
  const occupied = room.capacity - room.freeBeds;
  let bedsHtml = '';
  for (let i = 0; i < room.capacity; i++) {
    bedsHtml += `<div class="h-2 flex-1 rounded-full ${i < occupied ? 'bg-current opacity-85' : 'bg-white/70 border border-current/20'}"></div>`;
  }

  return `
    <div data-room="${room.id}" class="p-4 rounded-xl border-2 shadow-sm flex flex-col ${s.cls} transition-transform hover:-translate-y-1 cursor-pointer">
      <div class="flex justify-between items-center mb-2">
        <span class="text-lg font-bold">Кім. ${esc(room.roomNumber)}</span>
        <i class="fa-solid fa-users opacity-70"></i>
      </div>
      <div class="flex-1">
        <p class="text-sm font-semibold">${s.label}</p>
        <p class="text-xs mt-1 opacity-80">Зайнято: ${occupied} / ${room.capacity}</p>
      </div>
      <div class="flex gap-1.5 mt-3">${bedsHtml}</div>
    </div>`;
}