const HEARTBEAT_INTERVAL = 8000;
const CONNECT_TIMEOUT = 10000;
// 心跳发出去后没等到回包就认为链路已经假死：弱网下连接常常是「开着但收不到东西」，
// 只等 close 事件的话会一直不同步。
const PONG_TIMEOUT = 20000;
const MAX_RECONNECT_DELAY = 15000;
// 瞬时消息（心跳、校准）过期就没意义了，断线时宁可丢掉，也不能重连后补发一堆过期指令。
const TRANSIENT_TYPES = new Set(['ping', 'sync']);

export function createWatchTogetherClient({ token, onMessage, onStatus, onLatency }) {
  let socket = null;
  let reconnectTimer = null;
  let heartbeatTimer = null;
  let connectTimer = null;
  let pongTimer = null;
  let reconnectAttempts = 0;
  let closedByUser = false;
  let latency = 0;
  const queue = [];
  const maxQueueSize = 100;

  function stopHeartbeat() {
    window.clearInterval(heartbeatTimer);
    heartbeatTimer = null;
    window.clearTimeout(pongTimer);
    pongTimer = null;
  }

  function sendPing() {
    if (socket?.readyState !== WebSocket.OPEN) return;
    socket.send(JSON.stringify({ type: 'ping', clientTime: Date.now() }));
    window.clearTimeout(pongTimer);
    pongTimer = window.setTimeout(() => {
      // 回包超时：链路多半已经不通，主动断开触发重连，而不是继续假装同步正常
      pongTimer = null;
      if (socket && socket.readyState === WebSocket.OPEN) socket.close();
    }, PONG_TIMEOUT);
  }

  function startHeartbeat() {
    stopHeartbeat();
    sendPing();
    heartbeatTimer = window.setInterval(sendPing, HEARTBEAT_INTERVAL);
  }

  function connect() {
    if (!token || socket?.readyState === WebSocket.OPEN || socket?.readyState === WebSocket.CONNECTING) return;

    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
    socket = new WebSocket(`${protocol}//${location.host}/api/watch-together/socket?Authorization=${encodeURIComponent(token)}`);

    // 握手迟迟不回来时（弱网、代理半开）主动断开，走重连而不是一直干等
    window.clearTimeout(connectTimer);
    connectTimer = window.setTimeout(() => {
      connectTimer = null;
      if (socket?.readyState === WebSocket.CONNECTING) socket.close();
    }, CONNECT_TIMEOUT);

    socket.onopen = () => {
      window.clearTimeout(connectTimer);
      connectTimer = null;
      reconnectAttempts = 0;
      startHeartbeat();
      onStatus?.('open');
      while (queue.length && socket.readyState === WebSocket.OPEN) {
        socket.send(queue.shift());
      }
    };

    socket.onmessage = (event) => {
      let message = null;
      try {
        message = JSON.parse(event.data);
      } catch {
        onStatus?.('invalid_message');
        return;
      }
      if (message?.type === 'pong') {
        window.clearTimeout(pongTimer);
        pongTimer = null;
        const sentAt = Number(message.clientTime);
        if (Number.isFinite(sentAt) && sentAt > 0) {
          const sample = Math.max(0, Date.now() - sentAt);
          latency = latency > 0 ? Math.round(latency * 0.7 + sample * 0.3) : sample;
          onLatency?.(latency);
        }
      }
      onMessage?.(message);
    };

    socket.onerror = () => onStatus?.('error');

    socket.onclose = (event) => {
      window.clearTimeout(connectTimer);
      connectTimer = null;
      stopHeartbeat();
      socket = null;
      queue.length = 0;
      const rejected = event.code === 1003;
      const kicked = event.code === 1008;
      onStatus?.(rejected ? 'auth_failed' : 'closed');
      if (!closedByUser && !rejected && !kicked) {
        const delay = Math.min(MAX_RECONNECT_DELAY, 1500 * (2 ** Math.min(reconnectAttempts, 4)));
        reconnectAttempts += 1;
        reconnectTimer = window.setTimeout(connect, delay);
      }
    };
  }

  function send(message) {
    const payload = JSON.stringify(message);
    if (socket?.readyState === WebSocket.OPEN) {
      socket.send(payload);
      return true;
    }
    if (TRANSIENT_TYPES.has(message?.type)) return false;
    if (queue.length >= maxQueueSize) queue.shift();
    queue.push(payload);
    connect();
    return false;
  }

  function close() {
    closedByUser = true;
    window.clearTimeout(reconnectTimer);
    window.clearTimeout(connectTimer);
    connectTimer = null;
    stopHeartbeat();
    socket?.close();
    socket = null;
  }

  return { connect, send, close, isOpen: () => socket?.readyState === WebSocket.OPEN, latency: () => latency };
}
