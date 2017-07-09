var ctrlDown = false;

window.onkeyup = function(evt) {
  var e = evt || window.event;
  var code = e.keyCode ? e.keyCode : e.charCode;
  if (code === 17) {
    ctrlDown = false;
  }
}

window.onkeydown = function(evt) {
  var e = evt || window.event;
  var code = e.keyCode ? e.keyCode : e.charCode;
  if (code === 17) {
    ctrlDown = true;
  }

  if (ctrlDown && code === 13) {
    evaluateExpression();
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


function evaluateExpression()
{
  var selectedText = editor.getSession().doc.getTextRange(editor.selection.getRange());
  
  var sexpIndx = sexpAtPoint(editor.getValue(),
			     editor.session.doc.positionToIndex(editor.getCursorPosition()));

  if (selectedText.length === 0) {
    if (sexpIndx) {
      var sexp = editor.getSession().doc.getTextRange(
	{start: editor.session.doc.indexToPosition(sexpIndx[0]),
	 end: editor.session.doc.indexToPosition(sexpIndx[1])});
      ipcRenderer.send('cljs-command', sexp);
      // console.log(sexp, "SEXP");
    }
  } else {
    ipcRenderer.send('cljs-command', selectedText);
  }
  // console.log(sexp, selectedText);
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


