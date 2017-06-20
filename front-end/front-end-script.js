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
    case 13: {console.log('CTRL + ENTER');
	      getSelectedText();
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

  var args = Array.prototype.slice.call(arguments);
  for(var i=0;i<args.length;i++){
    var node = createLogNode(args[i]);
    var console = document.querySelector("#console");
    console.appendChild(node);
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


function getSelectedText()
{
  // obtain the object reference for the <textarea>
  var txtarea = document.getElementById("editor");
  // obtain the index of the first selected character
  var start = txtarea.selectionStart;
  // obtain the index of the last selected character
  var finish = txtarea.selectionEnd;
  // obtain the selected text
  var sel = txtarea.value.substring(start, finish);
  console.log(sel);
  // do something with the selected content
}

const {ipcRenderer} = require('electron')
console.log(ipcRenderer.sendSync('synchronous-message', 'ping'))

ipcRenderer.on('asynchronous-reply', (event, arg) => {
  console.log(arg) // prints "pong"
})

ipcRenderer.send('asynchronous-message', 'ping')
