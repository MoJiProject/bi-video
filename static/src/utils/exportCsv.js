export function exportCsv(filename, rows) {
  const data = Array.isArray(rows) ? rows : [];
  if (!data.length) return;
  const keys = Object.keys(data[0]);
  const escape = (v) => {
    if (v == null) return '';
    const s = String(v).replace(/"/g, '""');
    return /[",\n]/.test(s) ? `"${s}"` : s;
  };
  const lines = [keys.join(',')];
  data.forEach((row) => {
    lines.push(keys.map((k) => escape(row[k])).join(','));
  });
  const csv = '\uFEFF' + lines.join('\r\n');
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${filename}.csv`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
