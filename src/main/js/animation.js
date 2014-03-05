var AnimInitRotation, AnimLookAt, AnimNodeGlow, Animation, addAnim, animCycle, anims, intervalID, stopAnims,
  __hasProp = {}.hasOwnProperty,
  __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; };

intervalID = false;

anims = [];

addAnim = function(anim) {
  if (anim.name === 'lookat') {
    anims = anims.filter(function(anim) {
      return (anim.name !== 'initrotation') && (anim.name !== 'lookat');
    });
  }
  if (anims.length === 0) {
    intervalID = window.setInterval(animCycle, 30);
  }
  return anims.push(anim);
};

animCycle = function() {
  var a, _i, _len;
  for (_i = 0, _len = anims.length; _i < _len; _i++) {
    a = anims[_i];
    a.runCycle();
  }
  anims = anims.filter(function(anim) {
    return anim.active;
  });
  if (anims.length === 0) {
    window.clearInterval(intervalID);
    return intervalID = false;
  }
};

stopAnims = function() {
  anims = anims.filter(function(anim) {
    return !anim.stoppable;
  });
  if (anims.length === 0) {
    window.clearInterval(intervalID);
    return intervalID = false;
  }
};

Animation = (function() {

  function Animation() {
    this.name = '';
    this.active = true;
    this.stoppable = false;
  }

  Animation.prototype.runCycle = function() {
    return this.active = this.cycle();
  };

  return Animation;

})();

AnimInitRotation = (function(_super) {

  __extends(AnimInitRotation, _super);

  function AnimInitRotation() {
    this.name = 'initrotation';
    this.stoppable = true;
    this.animSpeedX = 0.007;
    this.animSpeedY = 0.005;
  }

  AnimInitRotation.prototype.cycle = function() {
    g.rotateX(-this.animSpeedX);
    g.rotateY(this.animSpeedY);
    g.updateView();
    this.animSpeedX *= 0.98;
    this.animSpeedY *= 0.98;
    if (this.animSpeedX < 0.0001) {
      return false;
    } else {
      return true;
    }
  };

  return AnimInitRotation;

})(Animation);

AnimLookAt = (function(_super) {

  __extends(AnimLookAt, _super);

  function AnimLookAt(targetSNode) {
    AnimLookAt.__super__.constructor.call(this);
    this.name = 'lookat';
    this.stoppable = true;
    this.targetSNode = targetSNode;
  }

  AnimLookAt.prototype.cycle = function() {
    var precision, speedFactor, speedX, speedY;
    speedFactor = 0.05;
    precision = 0.01;
    speedX = this.targetSNode.angleX * speedFactor;
    speedY = this.targetSNode.angleY * speedFactor;
    g.rotateX(speedY);
    g.rotateY(-speedX);
    g.updateView();
    if ((Math.abs(this.targetSNode.angleX) < precision) && (Math.abs(this.targetSNode.angleY) < precision)) {
      return false;
    } else {
      return true;
    }
  };

  return AnimLookAt;

})(Animation);

AnimNodeGlow = (function(_super) {

  __extends(AnimNodeGlow, _super);

  function AnimNodeGlow(node) {
    AnimNodeGlow.__super__.constructor.call(this);
    this.name = 'nodeglow';
    this.node = node;
    this.x = 0;
    this.cycles = 0.0;
    this.delta = 0.05;
    this.r1 = 224.0;
    this.g1 = 224.0;
    this.b1 = 224.0;
    this.r2 = 189.0;
    this.g2 = 218.0;
    this.b2 = 249.0;
  }

  AnimNodeGlow.prototype.cycle = function() {
    var b, g, r, rgb;
    this.x += this.delta;
    if (this.x > 1) {
      this.x = 1;
      this.delta = -this.delta;
    }
    if (this.x < 0) {
      this.x = 0;
      this.delta = -this.delta;
      this.cycles += 1;
    }
    r = Math.round(this.r1 + ((this.r2 - this.r1) * this.x));
    g = Math.round(this.g1 + ((this.g2 - this.g1) * this.x));
    b = Math.round(this.b1 + ((this.b2 - this.b1) * this.x));
    rgb = 'rgb(' + r + ',' + g + ',' + b + ')';
    $('#' + this.node.divid).css({
      background: rgb
    });
    if (this.cycles > 3) {
      return false;
    } else {
      return true;
    }
  };

  return AnimNodeGlow;

})(Animation);
