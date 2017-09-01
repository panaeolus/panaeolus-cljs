const {app, BrowserWindow, ipcMain, ipcRenderer} = require('electron');
const path = require('path');
const url = require('url');
const spawn = require('child_process').spawn;
const net = require('net');

const lumoConnection = new net.Socket();
// Hacky temp solution
var logBuffer = "";

let win;

var proc = spawn('./node_modules/lumo-cljs/bin/lumo',
		 ['-c', 'src:lib/andare-0.7.0.jar:lib/fs-0.1.0.jar:lib/instaparse-cljs-1.4.1.2.jar', '-n', '5555', '-k', '.lumo_cache']);

// proc.stdout.setEncoding('utf8');


function getWindow(windowName) {
    for (var i = 0; i < windowArray.length; i++) {
	if (windowArray[i].name == windowName) {
	    return windowArray[i].window;
	}
    }
    return null;
}

function createWindow () {
  // Create the browser window.
  
    win = new BrowserWindow({width: 800, height: 600});

    win.loadURL(url.format({
	pathname: path.join(__dirname, 'index.html'),
	protocol: 'file:',
	slashes: true
    }));
    
    // Open the DevTools.
    // win.webContents.openDevTools()
    

    // proc.stdout.on('data', function (data) {
    //   var str = data.toString()
    //   var lines = str.split(/(\r?\n)/g);
    //   console.log(lines.join("").toString('utf8'));
    // });

    var triggerPanaeolus = function () {
	lumoConnection.write(`(in-ns 'panaeolus.gui)(require 'panaeolus.all)(load-file "src/panaeolus/all.cljs")`);
    };
    
    setTimeout(() => {
	lumoConnection.connect(5555, 'localhost', function() {
	    triggerPanaeolus();
	    console.log('Starting panaeolus, may take a while....');
	});
    }, 500); 

    lumoConnection.on('data', function(data) {
	var logStrCand = data.toString('utf8'); 
	if (!/=>/g.test(logStrCand))
	{
	    win.webContents.send('log', `<div>${logStrCand}</div>`);
	    // logBuffer += `<div>${logStrCand}</div>`;
	}
	// console.log(data.toString('utf8'));
    });

    lumoConnection.on('close', function() {
	console.log('Connection closed');
    });
    

    win.on('closed', () => {
	win = null;
	lumoConnection.destroy();
	proc.kill('SIGINT');
    });
}

app.on('ready', createWindow);

app.on('window-all-closed', () => {
  lumoConnection.destroy();
  proc.kill('SIGINT');
  if (process.platform !== 'darwin') {
      app.quit();
  }
});

app.on('activate', () => {
  // On macOS it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (win === null) {
      createWindow();
  }
});



ipcMain.on('cljs-command', (event, arg) => {
  lumoConnection.write(`${arg}\n`);  
  // event.sender.send('cljs-command', arg)
    // setTimeout(() => {
    //   if (logBuffer.length) {
    //     event.sender.send('cljs-command', logBuffer);
    //     logBuffer = '';
    //   }
    // }, 50);
});

// ipcMain.on('console-log-ping', (event, arg) => {
//   setInterval(() => {
//     if (!logBuffer.length) {
//       event.sender.send('console-log-ping', logBuffer);
//       logBuffer = '';
//     }
//   }, 100)
// })


// ipcMain.on('synchronous-message', (event, arg) => {
//   console.log(arg)  // prints "ping"
//   event.returnValue = 'pong'
// })
