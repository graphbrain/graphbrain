/**
 * (c) 2012 GraphBrain Ltd. All rigths reserved.
 */

/**
 * This class implements certain aspects of quaternion arithmetic
 * necessary to perfrom 3D rotations without gimbal lock.
 * More info: http://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation
 */

var Quaternion = function()
{
    // initialize to identity quaternion
    this.x = 0;
    this.y = 0;
    this.z = 0;
    this.w = 1;
}

/**
 * Normalise the quaternion so that it's length is 1
 * Does not do anything if current length is within a certain tolerance
 */
Quaternion.prototype.normalise = function()
{
    var TOLERANCE = 0.00001;

    var l = (this.x * this.x) + (this.y * this.y) + (this.z * this.z) + (this.w * this.w);
    
    if (Math.abs(l - 1.0f) > TOLERANCE) {
        l = Math.sqrt(l);
        this.x /= l;
        this.y /= l;
        this.z /= l;
        this.w /= l;
    }
}

/**
 * Multiply quaternion q by this and store result in this
 * (this = q * this)
 * Purpose:
 * Changes rotation represented by this by rotation represented by q
 */
Quaternion.prototype.mul = function(q)
{
    var x = (this.w * q.x) + (this.x * q.w) + (this.y * q.z) - (this.z * q.y);
    var y = (this.w * q.y) - (this.x * q.z) + (this.y * q.w) + (this.z * q.x);
    var z = (this.w * q.z) + (this.x * q.y) - (this.y * q.x) + (this.z * q.w);
    var w = (this.w * q.w) - (this.x * q.x) - (this.y * q.y) - (this.z * q.z);
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
}