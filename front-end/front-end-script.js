var keysdown = {};
var lastkey = 0;

window.onkeydown = function(evt) {
  var e = evt || window.event;
  keysdown[e.keyCode ? e.keyCode : e.charCode] = true;
}

window.onkeydown = function(evt) {
  var e = evt || window.event;
  var code = e.keyCode ? e.keyCode : e.charCode;
  keysdown[code] = false;
  if (lastkey === 17) {
    switch (code) {
    case 13: {
      // console.log('CTRL + ENTER');
      evaluateSelectedText();
    }
      break;
    } 
  } else {
    lastkey = code;
  }
}


var baseLogFunction = console.log;
console.log = function(){
  baseLogFunction.apply(console, arguments);
  var console = document.querySelector("#console");
  var args = Array.prototype.slice.call(arguments);
  for(var i=0;i<args.length;i++){
    
    if (!/=>/.test(args[i].match)){
      console.innerHTML += args[i]
    };
    
    console.scrollTop = console.scrollHeight - console.clientHeight;
  }

}

function createLogNode(message){
  var node = document.createElement("div");
  var textNode = document.createTextNode(message);
  node.appendChild(textNode);
  return node;
}

window.onerror = function(message, url, linenumber) {
  console.log("JavaScript error: " + message + " on line " +
              linenumber + " for " + url);
}


function evaluateSelectedText()
{
  var selectedText = editor.getSession().doc.getTextRange(editor.selection.getRange());
  ipcRenderer.send('cljs-command', selectedText)
  // console.log(selectedText);
  // do something with the selected content
}

const {ipcRenderer} = require('electron')


ipcRenderer.on('cljs-command', (event, arg) => {
  console.log(arg) // prints "pong"
})


// ipcRenderer.send('console-log-ping');

// ipcRenderer.on('console-log-ping', (event, arg) => {
//   console.log(arg)
// })


