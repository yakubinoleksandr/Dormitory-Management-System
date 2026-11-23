// FR-04/FR-05: навігація по поверхах і сітка кімнат із колірною індикацією; колір дублюється текстом (NFR-05).
// Для завідуючої: вибір кімнати → вибір вільного місця → поселення за схваленою заявкою (FR-07).
import { api } from '../api.js';
import { getRole } from '../auth.js';
import { esc, dormitorySelect, withMessage, BTN, INPUT, CARD } from '../ui.js';

const STATUS = {
  FREE:    { cls: 'bg-green-100 border-green-500',   label: 'Вільна' },
  PARTIAL: { cls: 'bg-yellow-100 border-yellow-500', label: 'Частково зайнята' },
  FULL:    { cls: 'bg-red-100 border-red-500',       label: 'Зайнята' },
  REPAIR:  { cls: 'bg-gray-200 border-gray-500',     label: 'На ремонті' },
};

export async function renderRooms(container) {
  const isManager = getRole() === 'ROLE_MANAGER';
  container.innerHTML = `
    <div class="flex flex-wrap gap-2 mb-3 items-end">
      <div data-dorm class="w-72"></div>
      <select id="floor" class="border rounded px-3 py-2 text-sm"><option value="">Усі поверхи</option></select>
    </div>
    <p data-msg class="mb-2"></p>
    <div id="grid" class="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 gap-2"></div>
    <div id="panel" class="mt-4"></div>`;

  let dormitoryId = null;
  let rooms = [];
  const floorSelect = container.querySelector('#floor');
  const grid = container.querySelector('#grid');

  const draw = () => {
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
      <form id="settle" class="${CARD} space-y-2 max-w-xl">
        <h3 class="font-semibold">Кімната ${esc(room.roomNumber)} — поселення</h3>
        <select name="bedId" class="${INPUT}">
          ${beds.map((b) => `<option value="${b.id}" ${b.occupied ? 'disabled' : ''}>Ліжко ${b.bedNumber}${b.occupied ? ' (зайняте)' : ''}</option>`).join('')}
        </select>
        <select name="applicationId" class="${INPUT}" required>
          <option value="">Оберіть заяву мешканця</option>
          ${apps.map((a) => `<option value="${a.id}">${esc(a.fullName)} (заява №${a.id})</option>`).join('')}
        </select>
        <input name="contractNumber" placeholder="Номер договору" required class="${INPUT}">
        <div class="flex gap-2"><input name="startDate" type="date" required class="${INPUT}"><input name="endDate" type="date" required class="${INPUT}"></div>
        <button class="${BTN}">Поселити</button>
      </form>`;
    panel.querySelector('#settle').addEventListener('submit', async (e) => {
      e.preventDefault();
      const d = Object.fromEntries(new FormData(e.target));
      d.applicationId = Number(d.applicationId);
      d.bedId = Number(d.bedId);
      const r = await withMessage(container, () => api('/settlements', { method: 'POST', body: d }), 'Мешканця поселено');
      if (r) { panel.innerHTML = ''; await load(); }
    });
  }

  floorSelect.addEventListener('change', draw);
  await dormitorySelect(container, async (id) => { dormitoryId = id; container.querySelector('#panel').innerHTML = ''; await load(); });
}

function card(room) {
  const s = STATUS[room.status];
  return `<div data-room="${room.id}" class="border-2 rounded p-2 text-sm ${s.cls} cursor-pointer">
      <div class="font-semibold">${esc(room.roomNumber)}</div>
      <div>${s.label}</div>
      <div class="text-xs">Вільно: ${room.freeBeds}/${room.capacity}</div>
    </div>`;
}
