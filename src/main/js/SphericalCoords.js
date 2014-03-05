var SphericalCoords;

SphericalCoords = (function() {

  function SphericalCoords(negativeStretch, mappingPower) {
    this.negativeStretch = negativeStretch;
    this.mappingPower = mappingPower;
    this.theta = 0;
    this.phi = 0;
    this.r = 0;
    this.x = 0;
    this.y = 0;
    this.z = 0;
  }

  SphericalCoords.prototype.sphericalToCartesian = function() {
    var phi, theta;
    if (this.r === 0) {
      this.x = 0;
      this.y = 0;
      return this.z = 0;
    } else {
      theta = this.theta + (Math.PI / 2);
      phi = this.phi + (Math.PI / 2);
      this.x = this.r * Math.cos(theta) * Math.sin(phi);
      this.y = this.r * Math.cos(phi);
      this.z = this.r * Math.sin(theta) * Math.sin(phi);
      if (this.z < 0) {
        return this.z *= this.negativeStretch;
      }
    }
  };

  SphericalCoords.prototype.cartesianToSpherical = function() {
    this.r = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    this.theta = Math.atan2(this.z, this.x) - (Math.PI / 2);
    if (this.theta < -Math.PI) {
      this.theta += 2 * Math.PI;
    }
    return this.phi = Math.acos(this.y / this.r) - (Math.PI / 2);
  };

  SphericalCoords.prototype.scoordMapping = function(ang, maxAng) {
    var d, _maxAng;
    _maxAng = maxAng;
    if (ang < 0) {
      _maxAng = -maxAng;
    }
    d = Math.abs((_maxAng - ang) / maxAng);
    d = Math.abs(Math.pow(d, this.mappingPower));
    d *= _maxAng;
    return _maxAng - d;
  };

  SphericalCoords.prototype.viewMapping = function() {
    this.theta = this.scoordMapping(this.theta, Math.PI);
    return this.phi = this.scoordMapping(this.phi, Math.PI / 2);
  };

  return SphericalCoords;

})();
