/*
This class implements certain aspects of quaternion arithmetic
necessary to perfrom 3D rotations without gimbal lock.
More info: http://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation
*/

var Quaternion;

Quaternion = (function() {

  function Quaternion() {
    this.x = 0;
    this.y = 0;
    this.z = 0;
    this.w = 1;
  }

  Quaternion.prototype.fromEuler = function(pitch, yaw, roll) {
    var cosp, cosr, cosy, sinp, sinr, siny;
    sinp = Math.sin(pitch);
    siny = Math.sin(yaw);
    sinr = Math.sin(roll);
    cosp = Math.cos(pitch);
    cosy = Math.cos(yaw);
    cosr = Math.cos(roll);
    this.x = sinr * cosp * cosy - cosr * sinp * siny;
    this.y = cosr * sinp * cosy + sinr * cosp * siny;
    this.z = cosr * cosp * siny - sinr * sinp * cosy;
    this.w = cosr * cosp * cosy + sinr * sinp * siny;
    return this.normalise();
  };

  /*
      Normalise the quaternion so that it's length is 1
      Does not do anything if current length is within a certain tolerance
  */


  Quaternion.prototype.normalise = function() {
    var TOLERANCE, l;
    TOLERANCE = 0.00001;
    l = (this.x * this.x) + (this.y * this.y) + (this.z * this.z) + (this.w * this.w);
    if (Math.abs(l - 1) > TOLERANCE) {
      l = Math.sqrt(l);
      this.x /= l;
      this.y /= l;
      this.z /= l;
      return this.w /= l;
    }
  };

  /*
      Multiply quaternion q by this and store result in this
      (this = q * this)
      Purpose:
      Changes rotation represented by this by rotation represented by q
  */


  Quaternion.prototype.mul = function(q) {
    var _w, _x, _y, _z;
    _x = (this.w * q.x) + (this.x * q.w) + (this.y * q.z) - (this.z * q.y);
    _y = (this.w * q.y) - (this.x * q.z) + (this.y * q.w) + (this.z * q.x);
    _z = (this.w * q.z) + (this.x * q.y) - (this.y * q.x) + (this.z * q.w);
    _w = (this.w * q.w) - (this.x * q.x) - (this.y * q.y) - (this.z * q.z);
    this.x = _x;
    this.y = _y;
    this.z = _z;
    return this.w = _w;
  };

  /*
      Creates affine transformation matrix for the rotation represented by
      this quaternion.
      Matrix is written to the array with length 16 that must be provided as parameter.
      (for eficiency, avoid unnecesssary creation and destruction of arrays)
  */


  Quaternion.prototype.getMatrix = function(m) {
    var wx, wy, wz, x2, xy, xz, y2, yz, z2;
    x2 = this.x * this.x;
    y2 = this.y * this.y;
    z2 = this.z * this.z;
    xy = this.x * this.y;
    xz = this.x * this.z;
    yz = this.y * this.z;
    wx = this.w * this.x;
    wy = this.w * this.y;
    wz = this.w * this.z;
    m[0] = 1 - (2 * (y2 + z2));
    m[1] = 2 * (xy - wz);
    m[2] = 2 * (xz + wy);
    m[3] = 0;
    m[4] = 2 * (xy + wz);
    m[5] = 1 - (2 * (x2 + z2));
    m[6] = 2 * (yz - wx);
    m[7] = 0;
    m[8] = 2 * (xz - wy);
    m[9] = 2 * (yz + wx);
    m[10] = 1 - (2 * (x2 + y2));
    m[11] = 0;
    m[12] = 0;
    m[13] = 0;
    m[14] = 0;
    return m[15] = 1;
  };

  return Quaternion;

})();
