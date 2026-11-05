const WebSocket = require('ws');
const wsUrl = require('fs').readFileSync('C:\\Users\\JDDT\\AppData\\Local\\Temp\\kilo\\wsurl.txt','utf8').trim();
const ws = new WebSocket(wsUrl);
ws.on('open', () => {
  ws.send(JSON.stringify({ id: 1, method: 'Runtime.enable' }));
  ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
  setTimeout(() => {
    ws.send(JSON.stringify({
      id: 3,
      method: 'Runtime.evaluate',
      params: { expression: "document.querySelector('a[data-eit-userid]')?.outerHTML || 'none'", returnByValue: true }
    }));
  }, 8000);
});
ws.on('message', (data) => {
  const msg = JSON.parse(data);
  if (msg.method === 'Runtime.consoleAPICalled') {
    console.log('CONSOLE:', msg.params.args.map(a=>a.value||a.description).join(' '));
  }
  if (msg.id === 3) {
    console.log('RESULT:', JSON.stringify(msg.result));
    ws.close();
  }
});
ws.on('error', (e) => console.error('WS ERR', e));
