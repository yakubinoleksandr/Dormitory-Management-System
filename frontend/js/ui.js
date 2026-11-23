// Спільні допоміжні функції інтерфейсу.
import { api } from './api.js';

export function esc(text) {
  const div = document.createElement('div');
  div.textContent = text ?? '';
  return div.innerHTML;
}

export const BTN = 'bg-slate-800 text-white rounded px-4 py-2 text-sm hover:bg-slate-700';
export const INPUT = 'border rounded px-3 py-2 w-full text-sm';
export const CARD = 'bg-white p-4 rounded shadow';

export function showMessage(container, text, ok = false) {
  const box = container.querySelector('[data-msg]');
  if (!box) return;
  box.textContent = text;
  box.className = `text-sm ${ok ? 'text-green-700' : 'text-red-600'}`;
}

export async function withMessage(container, action, okText) {
  try {
    const result = await action();
    if (okText) showMessage(container, okText, true);
    return result;
  } catch (e) {
    showMessage(container, e.message);
    return null;
  }
}

/** Випадаючий список гуртожитків, доступних поточному користувачу. */
export async function dormitorySelect(container, onChange) {
  const holder = container.querySelector('[data-dorm]');
  const dorms = await api('/dormitories');
  holder.innerHTML = `<select class="${INPUT}">
    ${dorms.map((d) => `<option value="${d.id}">${esc(d.nameOrNumber)} (${d.layoutType === 'BLOCK' ? 'блочний' : 'коридорний'})</option>`).join('')}
  </select>`;
  const select = holder.querySelector('select');
  select.addEventListener('change', () => onChange(Number(select.value)));
  if (dorms.length) onChange(Number(select.value));
  else holder.innerHTML = '<p class="text-sm text-slate-500">Немає доступних гуртожитків</p>';
  return dorms;
}

export function formData(form) {
  return Object.fromEntries(new FormData(form));
}

export const fmtDate = (v) => (v ? new Date(v).toLocaleString('uk-UA') : '');
