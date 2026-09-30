export function createWatchTogetherClient({ token, onMessage, onStatus }) {
  let socket = null;
  let reconnectTimer = null;
  let heartbeatTimer = null;
  let reconnectAttempts = 0;
  let closedByUser = false;
  const queue = [];
  const maxQueueSize = 100;

  function stopHeartbeat() {
    window.clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  }

  function startHeartbeat() {
    stopHeartbeat();
    heartbeatTimer = window.setInterval(() => {
      if (socket?.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({ type: 'ping' }));
      }
    }, 25000);
  }

  function connect() {
    if (!token || socket?.readyState === WebSocket.OPEN || socket?.readyState === WebSocket.CONNECTING) return;

    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
    socket = new WebSocket(`${protocol}//${location.host}/api/watch-together/socket?Authorization=${encodeURIComponent(token)}`);

    socket.onopen = () => {
      reconnectAttempts = 0;
      startHeartbeat();
      onStatus?.('open');
      while (queue.length && socket.readyState === WebSocket.OPEN) {
        socket.send(queue.shift());
      }
    };

    socket.onmessage = (event) => {
      try {
        onMessage?.(JSON.parse(event.data));
      } catch {
        onStatus?.('invalid_message');
      }
    };

    socket.onerror = () => onStatus?.('error');

    socket.onclose = (event) => {
      stopHeartbeat();
      socket = null;
      queue.length = 0;
      const rejected = event.code === 1003;
      const kicked = event.code === 1008;
      onStatus?.(rejected ? 'auth_failed' : 'closed');
      if (!closedByUser && !rejected && !kicked) {
        const delay = Math.min(30000, 1500 * (2 ** Math.min(reconnectAttempts, 4)));
        reconnectAttempts += 1;
        reconnectTimer = window.setTimeout(connect, delay);
      }
    };
  }

  function send(message) {
    const payload = JSON.stringify(message);
    if (socket?.readyState === WebSocket.OPEN) {
      socket.send(payload);
    } else {
      if (queue.length >= maxQueueSize) queue.shift();
      queue.push(payload);
      connect();
    }
  }

  function close() {
    closedByUser = true;
    window.clearTimeout(reconnectTimer);
    stopHeartbeat();
    socket?.close();
    socket = null;
  }

  return { connect, send, close };
}
