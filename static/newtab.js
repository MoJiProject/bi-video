const http=require('http');
const req=http.request('http://127.0.0.1:9222/json/new?http://localhost:8081/message?messageMenu=3%26preview=1',{method:'PUT'},res=>{
  let d='';res.on('data',c=>d+=c);res.on('end',()=>{
    const o=JSON.parse(d);
    console.log(o.webSocketDebuggerUrl);
    require('fs').writeFileSync('C:\\Users\\JDDT\\AppData\\Local\\Temp\\kilo\\wsurl.txt',o.webSocketDebuggerUrl);
  });
});
req.end();
