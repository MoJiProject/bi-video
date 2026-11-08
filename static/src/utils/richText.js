const tags = new Set(['P', 'DIV', 'BR', 'SPAN', 'B', 'STRONG', 'I', 'EM', 'U']);

export function extractMentionIds(value) {
  const source = new DOMParser().parseFromString(sanitizeRichText(value), 'text/html');
  const ids = new Set();
  for (const link of source.querySelectorAll('a')) {
    const url = new URL(link.href, location.origin);
    const raw = link.getAttribute('data-eit-userid')
      || (url.origin === location.origin && url.pathname === '/home' && link.textContent.trim().startsWith('@')
        ? url.searchParams.get('userId') : null);
    const id = Number(raw);
    if (Number.isSafeInteger(id) && id > 0) ids.add(id);
  }
  return [...ids];
}

const sanitizeCache = new Map();
const SANITIZE_CACHE_LIMIT = 500;

export function sanitizeRichText(value) {
  const key = value || '';
  if (sanitizeCache.has(key)) return sanitizeCache.get(key);
  const source = new DOMParser().parseFromString(key, 'text/html').body;
  const result = document.createElement('div');
  function append(node, parent) {
    if (node.nodeType === Node.TEXT_NODE) { parent.append(document.createTextNode(node.textContent)); return; }
    if (node.nodeType !== Node.ELEMENT_NODE || ['SCRIPT', 'STYLE', 'IFRAME', 'OBJECT'].includes(node.tagName)) return;
    let element;
    if (node.tagName === 'IMG') {
      const src = node.getAttribute('src') || '';
      if (!/^\/img\/emoji\/\d{1,3}\.png$/.test(src) && src !== '/img/视频标题.png') return;
      element = document.createElement('img');
      element.src = src;
      element.alt = node.getAttribute('alt') || '[表情]';
      element.className = 'inline-emoji';
      element.decoding = 'async';
    } else if (node.tagName === 'A') {
      const href = node.getAttribute('href');
      if (href) {
        try {
          const url = new URL(href, location.origin);
          if (['http:', 'https:'].includes(url.protocol) && !url.username && !url.password) {
            element = document.createElement('a');
            element.href = url.href;
            element.target = '_blank';
            element.rel = 'noopener noreferrer';
            const userId = node.getAttribute('data-eit-userid');
            if (userId && /^\d+$/.test(userId)) element.setAttribute('data-eit-userid', userId);
            const inferredUserId = (!userId && url.origin === location.origin && url.pathname === '/home' && (node.textContent || '').trim().startsWith('@'))
              ? url.searchParams.get('userId') : null;
            if (inferredUserId && /^\d+$/.test(inferredUserId)) element.setAttribute('data-eit-userid', inferredUserId);
            if (element.hasAttribute('data-eit-userid')) element.className = 'at-msg';
          }
        } catch (error) {
          if (!(error instanceof TypeError)) throw error;
        }
      }
    } else if (tags.has(node.tagName)) {
      element = document.createElement(node.tagName.toLowerCase());
      const time = node.getAttribute('data-time');
      if (time && /^\d{1,6}[�?]\d{2}([�?]\d{2})?$/.test(time)) {
        element.setAttribute('data-time', time);
        element.className = 'time-span';
      }
    }
    const destination = element || parent;
    for (const child of node.childNodes) append(child, destination);
    if (element) parent.append(element);
  }
  for (const child of source.childNodes) append(child, result);
  const output = result.innerHTML;
  if (sanitizeCache.size >= SANITIZE_CACHE_LIMIT) sanitizeCache.clear();
  sanitizeCache.set(key, output);
  return output;
}
