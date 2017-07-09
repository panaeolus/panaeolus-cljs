
var tokens = [ ['{','}'] , ['[',']'] , ['(',')'] ];


// *** Check if character is an opening bracket ***
function isOpenParenthesis(parenthesisChar) {
  for (var j = 0; j < tokens.length; j++) {
    if (tokens[j][0] === parenthesisChar) {
      return true;
    }
  }
  return false;
}

// *** Check if opening bracket matches closing bracket ***
function matches(topOfStack, closedParenthesis) {
  for (var k = 0; k < tokens.length; k++) {
    if (tokens[k][0] === topOfStack && tokens[k][1] === closedParenthesis) {
      return true;
    }
  }
  return false;
}

// *** Checks if item is any sort of paranthesis ***
function isParanthesis(char) {
  var str = '{}[]()';
  if (str.indexOf(char) > -1) {
    return true;
  } else {
    return false;
  }
}


function sexpAtPoint(inputStr, cursorPos) {
  // var inputStr = 

  var expression = inputStr.split(''); 
  var stack = [];
  var returnValue = true;

  // var cursorPos = 
  var sexpBegin = 0;
  var sexpEnd = 0;
  
  for (var i = 0; i < expression.length; i++) {
    if ((cursorPos >= sexpBegin) &&
	(cursorPos <= sexpEnd) &&
	(sexpEnd != 0)) {
      return [sexpBegin, sexpEnd];
    } else {
      if (isParanthesis(expression[i])) {
	if (isOpenParenthesis(expression[i])) {
	  if (stack.length === 0) {
	    sexpBegin = i;
	  }
	  stack.push(expression[i]);
	} else {
          if (stack.length === 0) {
	    console.log("Warning: unbalanced parenthesis!\n")
            // return false;
          }
	  if (stack.length === 1) {
            sexpEnd = i + 1;
          }
          var top = stack.pop(); // pop off the top element from stack
          if (!matches(top, expression[i])) {
	    console.log("Warning: unbalanced parenthesis!\n")
            // return false;
          }
	}
      }
    }
  }

  if (stack.length != 0) {
    console.log("Warning: unbalanced parenthesis!\n")
  }
  
  if ((cursorPos >= sexpBegin) &&
      (cursorPos <= sexpEnd) &&
      (sexpEnd != 0)) {
    return [sexpBegin, sexpEnd];
  } else {
    return false;
  }
  
  // return returnValue;
  // return [cursorPos, sexpBegin, sexpEnd];
}
