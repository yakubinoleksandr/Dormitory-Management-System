// FR-04/FR-05: сітка кімнат із колірною індикацією; колір дублюється текстом (NFR-05).
import { api } from '../api.js';

const STATUS = {
  FREE:    { cls: 'bg-green-100 border-green-500',   label: 'Вільна' },
  PARTIAL: { cls: 'bg-yellow-100 border-yellow-500', label: 'Частково зайнята' },
  FULL:    { cls: 'bg-red-100 border-red-500',       label: 'Зайнята' },
  REPAIR:  { cls: 'bg-gray-200 border-gray-500',     label: 'На ремонті' },
};

export async function renderRooms(container) {
  container.innerHTML = `
    <div class="flex gap-2 mb-4">
      <input id="dorm" type="number" min="1" placeholder="ID гуртожитку" class="border rounded px-3 py-2">
      <button id="load" class="bg-slate-800 text-white rounded px-4">Показати</button>
    </div>
    <p id="msg" class="text-red-600 text-sm mb-2"></p>
    <div id="grid" class="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 gap-2"></div>`;

  container.querySelector('#load').addEventListener('click', async () => {
    const id = container.querySelector('#dorm').value;
    const grid = container.querySelector('#grid');
    const msg = container.querySelector('#msg');
    msg.textContent = '';
    grid.innerHTML = '';
    try {
      const rooms = await api(`/dormitories/${id}/rooms`);
      grid.innerHTML = rooms.map(roomCard).join('');
    } catch (e) {
      msg.textContent = e.message;
    }
  });
}

function roomCard(room) {
  const s = STATUS[room.status];
  return `<div class="border-2 rounded p-2 text-sm ${s.cls}">
      <div class="font-semibold">${room.roomNumber}</div>
      <div>${s.label}</div>
      <div class="text-xs">Вільно: ${room.freeBeds}/${room.capacity}</div>
    </div>`;
}
