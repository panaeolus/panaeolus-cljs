const {app, BrowserWindow, ipcMain} = require('electron')
const path = require('path')
const url = require('url')

const exec = require('child_process').exec;
const spawn = require('child_process').spawn;


// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let win

function createWindow () {
  // Create the browser window.
  var proc = spawn('lumo', ['-c', 'src:~/.m2/repository/andare/andare/0.6.0/andare-0.6.0.jar:~/.m2/repository/macchiato/fs/0.0.6/fs-0.0.6.jar', '-e', `(println "HASHD")`]);
  
  proc.stdout.setEncoding('utf8');
  proc.stdout.on('data', function (data) {
    var str = data.toString()
    var lines = str.split(/(\r?\n)/g);
    console.log(lines.join(""));
    // ipcMain.send(lines.join(""));
    
  });
  win = new BrowserWindow({width: 800, height: 600})
  process.stdout.write('your output to command prompt console or node js ');
  // and load the index.html of the app.
  win.loadURL(url.format({
    pathname: path.join(__dirname, 'index.html'),
    protocol: 'file:',
    slashes: true
  }))

  // Open the DevTools.
  // win.webContents.openDevTools()
  
  // console.log('filename: ' + path.basename(fileName).replace('cljs', 'js'));
  
  
  
  // Emitted when the window is closed.
  win.on('closed', () => {
    // Dereference the window object, usually you would store windows
    // in an array if your app supports multi windows, this is the time
    // when you should delete the corresponding element.
    win = null
  })
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', createWindow)

// Quit when all windows are closed.
app.on('window-all-closed', () => {
  // On macOS it is common for applications and their menu bar
  // to stay active until the user quits explicitly with Cmd + Q
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('activate', () => {
  // On macOS it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (win === null) {
    createWindow()
  }
})


ipcMain.on('asynchronous-message', (event, arg) => {
  console.log(arg)  // prints "ping"
  event.sender.send('asynchronous-reply', 'pong')
})

ipcMain.on('synchronous-message', (event, arg) => {
  console.log(arg)  // prints "ping"
  event.returnValue = 'pong'
})
