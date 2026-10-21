import http from './http';
import { offlinePreviewEnabled } from '../preview/config';

export function connectVideoLive(videoId, handlers) {
  if (offlinePreviewEnabled) {
    let stopped = false;
    Promise.resolve().then(() => {
      if (stopped) return;
      handlers.status('preview');
      return handlers.open();
    }).catch(handlers.error);
    return () => { stopped = true; };
  }
  const url = new URL(`${http.defaults.baseURL.replace(/\/$/, '')}/video/live`, location.origin);
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:';
  url.searchParams.set('videoId', videoId);
  let socket;
  let retry;
  let heartbeat;
  let attempts = 0;
  let stopped = false;
  let lastPong = 0;

  function connect() {
    if (stopped) return;
    handlers.status('connecting');
    socket = new WebSocket(url.href);
    const current = socket;
    current.onopen = () => {
      if (stopped || current !== socket) { current.close(); return; }
      attempts = 0;
      lastPong = Date.now();
      handlers.status('connected');
      Promise.resolve(handlers.open()).catch(handlers.error);
      heartbeat = window.setInterval(() => {
        if (Date.now() - lastPong > 75000) { current.close(); return; }
        if (current.readyState === WebSocket.OPEN) current.send('{"type":"ping"}');
      }, 25000);
    };
    current.onmessage = event => {
      if (stopped || current !== socket) return;
      let message;
      try { message = JSON.parse(event.data); }
      catch (error) { handlers.error(new Error(`实时消息格式错误：${error.message}`)); current.close(); return; }
      if (message.type === 'pong') lastPong = Date.now();
      else handlers.message(message);
    };
    current.onerror = () => { if (!stopped && current === socket) handlers.status('reconnecting'); };
    current.onclose = event => {
      if (current !== socket) return;
      window.clearInterval(heartbeat);
      if (stopped) return;
      if ([1007, 1008].includes(event.code)) {
        handlers.status('unavailable');
        handlers.error(new Error(event.reason || '当前视频无法建立实时连接'));
        return;
      }
      handlers.status('reconnecting');
      retry = window.setTimeout(connect, Math.min(30000, 1000 * 2 ** attempts++));
    };
  }
  function visibility() {
    if (!document.hidden && !stopped && socket?.readyState === WebSocket.CLOSED) {
      window.clearTimeout(retry);
      connect();
    }
  }
  document.addEventListener('visibilitychange', visibility);
  connect();
  return () => {
    stopped = true;
    window.clearTimeout(retry);
    window.clearInterval(heartbeat);
    document.removeEventListener('visibilitychange', visibility);
    socket?.close();
  };
}
