import { getToken } from './http';
import { offlinePreviewEnabled } from '../preview/config';

export function subscribeChat(onChange, onReady, onError) {
  if (offlinePreviewEnabled) return { close() {} };
  let socket;
  let closed = false;
  let retry;
  let heartbeat;
  let attempts = 0;
  let lastSeen = 0;
  let readySeen = false;
  function connect() {
    if (closed) return;
    const token = getToken();
    if (!token) { onError(new Error('请先登录后使用私信')); return; }
    const url = new URL('/api/chat', location.origin);
    url.protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
    url.searchParams.set('token', token);
    socket = new WebSocket(url);
    socket.onmessage = event => {
      let data;
      try { data = JSON.parse(event.data); }
      catch (error) { onError(error); socket.close(1007); return; }
      lastSeen = Date.now();
      if (data.type === 'ready') {
        attempts = 0;
        onReady(readySeen);
        readySeen = true;
      } else if (data.type === 'messages.changed') onChange(data);
    };
    socket.onopen = () => {
      lastSeen = Date.now();
      heartbeat = setInterval(() => {
        if (Date.now() - lastSeen > 60000) { socket.close(); return; }
        if (socket.readyState === WebSocket.OPEN) socket.send('{"type":"ping"}');
      }, 20000);
    };
    socket.onerror = () => onError(new Error('私信实时连接异常，正在尝试恢复'));
    socket.onclose = event => {
      clearInterval(heartbeat);
      if (closed) return;
      if (event.code === 1008) { onError(new Error('私信登录状态已失效，请重新登录')); return; }
      retry = setTimeout(connect, Math.min(30000, 1000 * 2 ** Math.min(attempts++, 5)));
    };
  }
  connect();
  return {
    close() {
      closed = true;
      clearTimeout(retry);
      clearInterval(heartbeat);
      if (socket) socket.close();
    },
  };
}
