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
    case 13: console.log('CTRL + ENTER')
      break;
    }
  }
  lastkey = code;
}
