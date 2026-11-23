// Спільні допоміжні функції інтерфейсу.
import { api } from './api.js';

export function esc(text) {
  const div = document.createElement('div');
  div.textContent = text ?? '';
  return div.innerHTML;
}

export const BTN = 'bg-blue-600 hover:bg-blue-700 text-white rounded-lg px-4 py-2 text-sm font-medium transition-colors shadow-sm inline-flex items-center justify-center gap-2 cursor-pointer';
export const INPUT = 'border border-slate-300 rounded-lg px-3 py-2 w-full text-sm focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 bg-white transition-all';
export const CARD = 'bg-white p-5 rounded-xl shadow-sm border border-slate-200';

export function showMessage(container, text, ok = false) {
  const box = container.querySelector('[data-msg]');
  if (!box) return;
  if (!text) {
    box.className = 'hidden';
    return;
  }
  box.innerHTML = `<i class="fa-solid ${ok ? 'fa-circle-check' : 'fa-circle-exclamation'} mr-1.5"></i>${esc(text)}`;
  box.className = `text-sm px-4 py-2.5 rounded-lg mb-4 font-medium border ${
    ok ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : 'bg-rose-50 text-rose-700 border-rose-200'
  }`;
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
  holder.innerHTML = `
    <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Гуртожиток</label>
    <select class="${INPUT}">
      ${dorms.map((d) => `<option value="${d.id}">${esc(d.nameOrNumber)} (${d.layoutType === 'BLOCK' ? 'блочний' : 'коридорний'})</option>`).join('')}
    </select>`;
  const select = holder.querySelector('select');
  select.addEventListener('change', () => onChange(Number(select.value)));
  if (dorms.length) onChange(Number(select.value));
  else holder.innerHTML = '<p class="text-sm text-slate-500 bg-slate-100 p-2.5 rounded-lg">Немає доступних гуртожитків</p>';
  return dorms;
}

export function formData(form) {
  return Object.fromEntries(new FormData(form));
}

export const fmtDate = (v) => (v ? new Date(v).toLocaleString('uk-UA') : '');