var CustomMouseHandlers = (function() {
  var lookAtHandlers = {};

  function LookAtHandler(matrix, center, up, pitchRate, yawRate) {
    var WT = Wt.WT;
    var vec3 = WT.glMatrix.vec3;
    var mat4 = WT.glMatrix.mat4;

    var cameraMatrix = matrix;
    var lookAtCenter = center;
    var lookAtUpDir = up;
    var lookAtPitchRate = pitchRate;
    var lookAtYawRate = yawRate;
    var pinchWidth = null;
    var singleTouch = null;
    var doubleTouch = null;
    var dragPreviousXY = null;
    var target = null;

    this.setTarget = function(newTarget) {
      target = newTarget;
    }

    this.mouseDown = function(o, event) {
      WT.capture(null);
      WT.capture(o);

      dragPreviousXY = WT.pageCoordinates(event);
    }

    this.mouseUp = function(o, event) {
      if (dragPreviousXY !== null)
	dragPreviousXY = null;
    };

    this.mouseDrag = function(o, event) {
      if (dragPreviousXY === null)
	return;
      var c = WT.pageCoordinates(event);
      if (WT.buttons === 5 || WT.buttons === 2) {
	mouseZoom(c);
      } else if (WT.buttons === 1) {
	rotate(c);
      } else if (WT.buttons === 4) {
	pan(o, c);
      }
    };

    function mouseZoom(newCoords) {
      var dy = newCoords.y - dragPreviousXY.y;
      zoom(-dy * 0.05);
      dragPreviousXY = newCoords;
    }

    // Mouse wheel = zoom in/out
    this.mouseWheel = function(o, event) {
      WT.cancelEvent(event);
      var d = WT.wheelDelta(event);
      zoom(d);
    };

    function zoom(delta) {
      var s = Math.pow(1.2, delta);
      mat4.translate(cameraMatrix, lookAtCenter);
      mat4.scale(cameraMatrix, [s, s, s]);
      vec3.negate(lookAtCenter);
      mat4.translate(cameraMatrix, lookAtCenter);
      vec3.negate(lookAtCenter);
      // Repaint!
      target.paintGL();
    }

    function rotate(newCoords) {
      var prevPitchCos = cameraMatrix[5] / vec3.length([cameraMatrix[1], cameraMatrix[5], cameraMatrix[9]]);
      var prevPitchSin = cameraMatrix[6] / vec3.length([cameraMatrix[2], cameraMatrix[6], cameraMatrix[10]]);
      var prevPitch = Math.atan2(prevPitchSin, prevPitchCos);
      var dx=(newCoords.x - dragPreviousXY.x);
      var dy=(newCoords.y - dragPreviousXY.y);
      var s=vec3.create();
      s[0]=cameraMatrix[0];
      s[1]=cameraMatrix[4];
      s[2]=cameraMatrix[8];
      var r=mat4.create();
      mat4.identity(r);
      mat4.translate(r, lookAtCenter);
      var dPitch = dy * lookAtPitchRate;
      if (Math.abs(prevPitch + dPitch) >= Math.PI / 2) {
	var sign = prevPitch > 0 ? 1 : -1;
	dPitch = sign * Math.PI / 2 - prevPitch;
      }
      mat4.rotate(r, dPitch, s);
      mat4.rotate(r, dx * lookAtYawRate, lookAtUpDir);
      vec3.negate(lookAtCenter);
      mat4.translate(r, lookAtCenter);
      vec3.negate(lookAtCenter);
      var before = mat4.create(cameraMatrix);
      mat4.multiply(cameraMatrix,r,cameraMatrix);
      // Repaint!
      target.paintGL();
      // store mouse coord for next action
      dragPreviousXY = newCoords;
    };

    function pan(o, newCoords) {
      var left = vec3.create([cameraMatrix[0], cameraMatrix[4], cameraMatrix[8]]);
      vec3.normalize(left);
      var up = vec3.create([cameraMatrix[1], cameraMatrix[5], cameraMatrix[9]]);
      vec3.normalize(up);
      var cameraDirection = vec3.create([cameraMatrix[2], cameraMatrix[6], cameraMatrix[10]]);
      vec3.normalize(cameraDirection);
      var ratio = WT.innerHeight(o) / WT.innerWidth(o);
      // 4 because: perspective transform puts -2 as the left plane, and 2 as the right plane
      var dx = (newCoords.x - dragPreviousXY.x) / WT.innerWidth(o) * 4;
      var dy = (newCoords.y - dragPreviousXY.y) / WT.innerHeight(o) * 4 * ratio;
      var norm = vec3.length([cameraMatrix[0], cameraMatrix[4], cameraMatrix[8]]);
      vec3.scale(left, dx / norm);
      vec3.scale(up, -dy / norm);
      mat4.translate(cameraMatrix, left);
      mat4.translate(cameraMatrix, up);
      vec3.scale(left, -1);
      vec3.scale(up, -1);
      vec3.add(lookAtCenter, left);
      vec3.add(lookAtCenter, up);
      target.paintGL();
      dragPreviousXY = newCoords;
    };

    this.touchStart = function(o, event) {
      singleTouch = event.touches.length === 1 ? true : false;
      doubleTouch = event.touches.length === 2 ? true : false;

      if (singleTouch) {
	WT.capture(null);
	WT.capture(o);
	dragPreviousXY = WT.pageCoordinates(event.touches[0]);
      } else if (doubleTouch) {
	var c0 = WT.pageCoordinates(event.touches[0]);
	var c1 = WT.pageCoordinates(event.touches[1]);
	pinchWidth = Math.sqrt( (c0.x-c1.x)*(c0.x-c1.x) + (c0.y-c1.y)*(c0.y-c1.y) );
      } else {
	return;
      }
      event.preventDefault();
    };

    this.touchEnd = function(o, event) {
      var noTouch = event.touches.length === 0 ? true : false;
      singleTouch = event.touches.length === 1 ? true : false;
      doubleTouch = event.touches.length === 2 ? true : false;

      if (noTouch)
	this.mouseUp(null, null);
      if (singleTouch || doubleTouch)
	this.touchStart(o, event);
    };

    this.touchMoved = function(o, event) {
      if ( (!singleTouch) && (!doubleTouch) )
	return;

      event.preventDefault();
      if (singleTouch)
	this.mouseDrag(o, event.touches[0]);
      if (doubleTouch) {
	var c0 = WT.pageCoordinates(event.touches[0]);
	var c1 = WT.pageCoordinates(event.touches[1]);
	var d = Math.sqrt( (c0.x-c1.x)*(c0.x-c1.x) + (c0.y-c1.y)*(c0.y-c1.y) );
	var scale = d / pinchWidth;
	if (Math.abs(scale-1) < 0.05) {
	  return;
	} else if (scale > 1) {
	  scale = 1;
	} else {
	  scale = -1;
	}
	pinchWidth = d;
	zoom(scale);
      }
    };
  };

  function createAndGetLookAtHandler(ref, matrix, center, up, pitchRate, yawRate) {
    if (lookAtHandlers[ref.id] === undefined) {
      lookAtHandlers[ref.id] = new LookAtHandler(matrix, center, up, pitchRate, yawRate);
    }
    return lookAtHandlers[ref.id];
  }

  var selectionHandlers = {};

  function SelectionHandler() {
    var WT = Wt.WT;

    var dragPreviousXY = null;

    this.mouseDown = function(o, event) {
      if (WT.button(event) === 4) {
	WT.capture(null);
	WT.capture(o);

	if (dragPreviousXY === null)
	  dragPreviousXY = WT.widgetCoordinates(o, event);
      }
    };
    this.mouseDrag = function(o, event) {
      if (dragPreviousXY !== null) {
	var currentPosition = WT.widgetCoordinates(o, event);
	var minX = Math.min(currentPosition.x, dragPreviousXY.x);
	var maxX = Math.max(currentPosition.x, dragPreviousXY.x);
	var minY = Math.min(currentPosition.y, dragPreviousXY.y);
	var maxY = Math.max(currentPosition.y, dragPreviousXY.y);
	var dx = maxX - minX;
	var dy = maxY - minY;
	var selectedRange = $(o).siblings('.selectedRange');
	selectedRange.css({'position': 'absolute','display': 'block','left': minX,'top': minY});
	selectedRange.width(dx);
	selectedRange.height(dy);
      }
    };
    this.mouseUp = function(o, event) {
      if (WT.button(event) === 4) {
	if (dragPreviousXY !== null) {
	  var currentXY = WT.widgetCoordinates(o, event);
	  if (currentXY.x != dragPreviousXY.x && currentXY.y != dragPreviousXY.y) {
	    Wt.emit(o, {name: 'rangeSelect', event: event, eventObject: o}, dragPreviousXY.x, dragPreviousXY.y);
	  }
	}
      }
      var selectedRange = $(o).siblings('.selectedRange');
      selectedRange.css('display','none');
      dragPreviousXY = null;
    };
  };

  function createAndGetSelectionHandler(ref) {
    if (selectionHandlers[ref.id] === undefined) {
      selectionHandlers[ref.id] = new SelectionHandler();
    }
    return selectionHandlers[ref.id];
  }

  return {
    lookAtHandler: createAndGetLookAtHandler,
    selectionHandler: createAndGetSelectionHandler
  };
})();
