var frand, getCoulombEnergy, getForces, layout;

frand = function() {
  return Math.random() - 0.5;
};

getCoulombEnergy = function(snodeArray) {
  var N, e, i, j, _i, _j, _ref, _ref1, _ref2;
  e = 0;
  N = snodeArray.length;
  for (i = _i = 0, _ref = N - 1; 0 <= _ref ? _i <= _ref : _i >= _ref; i = 0 <= _ref ? ++_i : --_i) {
    if (i < N - 1) {
      for (j = _j = _ref1 = i + 1, _ref2 = N - 1; _ref1 <= _ref2 ? _j <= _ref2 : _j >= _ref2; j = _ref1 <= _ref2 ? ++_j : --_j) {
        e += 1 / v3diffLength(snodeArray[i].tpos, snodeArray[j].tpos);
      }
    }
  }
  return e;
};

getForces = function(snodeArray) {
  var N, ff, i, j, l, posi, posj, r, _i, _j, _ref, _ref1, _results;
  N = snodeArray.length;
  r = newv3;
  for (i = _i = 0, _ref = N - 1; 0 <= _ref ? _i <= _ref : _i >= _ref; i = 0 <= _ref ? ++_i : --_i) {
    snodeArray[i].f[0] = 0;
    snodeArray[i].f[1] = 0;
    snodeArray[i].f[2] = 0;
  }
  _results = [];
  for (i = _j = 0, _ref1 = N - 1; 0 <= _ref1 ? _j <= _ref1 : _j >= _ref1; i = 0 <= _ref1 ? ++_j : --_j) {
    posi = snodeArray[i].pos;
    if (i < N - 1) {
      _results.push((function() {
        var _k, _ref2, _ref3, _results1;
        _results1 = [];
        for (j = _k = _ref2 = i + 1, _ref3 = N - 1; _ref2 <= _ref3 ? _k <= _ref3 : _k >= _ref3; j = _ref2 <= _ref3 ? ++_k : --_k) {
          posj = snodeArray[j].pos;
          r[0] = posi[0] - posj[0];
          r[1] = posi[1] - posj[1];
          r[2] = posi[2] - posj[2];
          l = v3length(r);
          l = 1 / (l * l * l);
          ff = l * r[0];
          snodeArray[i].f[0] += ff;
          snodeArray[j].f[0] -= ff;
          ff = l * r[1];
          snodeArray[i].f[1] += ff;
          snodeArray[j].f[1] -= ff;
          ff = l * r[2];
          snodeArray[i].f[2] += ff;
          _results1.push(snodeArray[j].f[2] -= ff);
        }
        return _results1;
      })());
    } else {
      _results.push(void 0);
    }
  }
  return _results;
};

layout = function(snodeArray) {
  var N, Nstep, d, e, e0, f, i, k, l, minimalStep, pos, step, tpos, _i, _j, _k, _l, _ref, _ref1, _ref2, _ref3;
  N = snodeArray.length;
  if (N === 0) {
    return;
  }
  Nstep = 20;
  step = 0.01;
  minimalStep = 1e-10;
  for (i = _i = 0, _ref = N - 1; 0 <= _ref ? _i <= _ref : _i >= _ref; i = 0 <= _ref ? ++_i : --_i) {
    if (!snodeArray[i].layedOut) {
      snodeArray[i].pos[0] = 2 * frand();
      snodeArray[i].pos[1] = 2 * frand();
      snodeArray[i].pos[2] = 2 * frand();
    }
    l = v3length(snodeArray[i].pos);
    if (l !== 0.0) {
      if (!snodeArray[i].layedOut) {
        snodeArray[i].pos[0] /= l;
        snodeArray[i].pos[1] /= l;
        snodeArray[i].pos[2] /= l;
      }
      snodeArray[i].tpos[0] = snodeArray[i].pos[0];
      snodeArray[i].tpos[1] = snodeArray[i].pos[1];
      snodeArray[i].tpos[2] = snodeArray[i].pos[2];
    } else {
      i -= 1;
    }
    snodeArray[i].layedOut = true;
  }
  e0 = getCoulombEnergy(snodeArray);
  for (k = _j = 0, _ref1 = Nstep - 1; 0 <= _ref1 ? _j <= _ref1 : _j >= _ref1; k = 0 <= _ref1 ? ++_j : --_j) {
    getForces(snodeArray);
    for (i = _k = 0, _ref2 = N - 1; 0 <= _ref2 ? _k <= _ref2 : _k >= _ref2; i = 0 <= _ref2 ? ++_k : --_k) {
      f = snodeArray[i].f;
      pos = snodeArray[i].pos;
      tpos = snodeArray[i].tpos;
      d = v3dotv3(f, pos);
      f[0] -= pos[0] * d;
      f[1] -= pos[1] * d;
      f[2] -= pos[2] * d;
      tpos[0] = pos[0] + f[0] * step;
      tpos[1] = pos[1] + f[1] * step;
      tpos[2] = pos[2] + f[2] * step;
      l = v3length(tpos);
      tpos[0] /= l;
      tpos[1] /= l;
      tpos[2] /= l;
    }
    e = getCoulombEnergy(snodeArray);
    if (e >= e0) {
      step /= 2;
      if (step < minimalStep) {
        return;
      }
    } else {
      for (i = _l = 0, _ref3 = N - 1; 0 <= _ref3 ? _l <= _ref3 : _l >= _ref3; i = 0 <= _ref3 ? ++_l : --_l) {
        snodeArray[i].pos[0] = snodeArray[i].tpos[0];
        snodeArray[i].pos[1] = snodeArray[i].tpos[1];
        snodeArray[i].pos[2] = snodeArray[i].tpos[2];
      }
      e0 = e;
      step *= 2;
    }
  }
};
