var ctrlDown = false;

window.onkeyup = function(evt) {
  var e = evt || window.event;
  var code = e.keyCode ? e.keyCode : e.charCode;
  if (code === 17) {
    ctrlDown = true;
  }
}

window.onkeydown = function(evt) {
  var e = evt || window.event;
  var code = e.keyCode ? e.keyCode : e.charCode;
  if (code === 17) {
    ctrlDown = true;
  }

  if (ctrlDown && code === 13) {
    evaluateSelectedText();
  }
}




var baseLogFunction = console.log;
console.log = function(){
  baseLogFunction.apply(console, arguments);
  var printArea = document.querySelector("#console");
  var args = Array.prototype.slice.call(arguments);
  for(var i=0;i<args.length;i++){
    
    if (!/=>/g.test(args[i].match)){
      console.info(args[i], /=>/g.test(args[i].match));
      printArea.innerHTML += args[i]
    };
    
    printArea.scrollTop = printArea.scrollHeight - printArea.clientHeight;
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


