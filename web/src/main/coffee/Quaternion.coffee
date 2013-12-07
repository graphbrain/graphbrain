# (c) 2012 GraphBrain Ltd. All rigths reserved.

###
This class implements certain aspects of quaternion arithmetic
necessary to perfrom 3D rotations without gimbal lock.
More info: http://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation
###

class Quaternion
    # initialize to identity quaternion
    constructor: ->
        @x = 0
        @y = 0
        @z = 0
        @w = 1

    # Init quaternion from eurler angles
    fromEuler: (pitch, yaw, roll) ->
        sinp = Math.sin(pitch)
        siny = Math.sin(yaw)
        sinr = Math.sin(roll)
        cosp = Math.cos(pitch)
        cosy = Math.cos(yaw)
        cosr = Math.cos(roll)
 
        @x = sinr * cosp * cosy - cosr * sinp * siny
        @y = cosr * sinp * cosy + sinr * cosp * siny
        @z = cosr * cosp * siny - sinr * sinp * cosy
        @w = cosr * cosp * cosy + sinr * sinp * siny
 
        @normalise()

    ###
    Normalise the quaternion so that it's length is 1
    Does not do anything if current length is within a certain tolerance
    ###
    normalise: ->
        TOLERANCE = 0.00001;
        l = (@x * @x) + (@y * @y) + (@z * @z) + (@w * @w)
    
        if Math.abs(l - 1) > TOLERANCE
            l = Math.sqrt(l)
            @x /= l
            @y /= l
            @z /= l
            @w /= l

    ###
    Multiply quaternion q by this and store result in this
    (this = q * this)
    Purpose:
    Changes rotation represented by this by rotation represented by q
    ###
    mul: (q) ->
        _x = (@w * q.x) + (@x * q.w) + (@y * q.z) - (@z * q.y)
        _y = (@w * q.y) - (@x * q.z) + (@y * q.w) + (@z * q.x)
        _z = (@w * q.z) + (@x * q.y) - (@y * q.x) + (@z * q.w)
        _w = (@w * q.w) - (@x * q.x) - (@y * q.y) - (@z * q.z)
        @x = _x
        @y = _y
        @z = _z
        @w = _w

    ###
    Creates affine transformation matrix for the rotation represented by
    this quaternion.
    Matrix is written to the array with length 16 that must be provided as parameter.
    (for eficiency, avoid unnecesssary creation and destruction of arrays)
    ###
    getMatrix: (m) ->
        x2 = @x * @x
        y2 = @y * @y
        z2 = @z * @z
        xy = @x * @y
        xz = @x * @z
        yz = @y * @z
        wx = @w * @x
        wy = @w * @y
        wz = @w * @z
 
        m[0] = 1 - (2 * (y2 + z2))
        m[1] = 2 * (xy - wz)
        m[2] = 2 * (xz + wy)
        m[3] = 0
        m[4] = 2 * (xy + wz)
        m[5] = 1 - (2 * (x2 + z2))
        m[6] = 2 * (yz - wx)
        m[7] = 0
        m[8] = 2 * (xz - wy)
        m[9] = 2 * (yz + wx)
        m[10] = 1 - (2 * (x2 + y2))
        m[11] = 0
        m[12] = 0
        m[13] = 0
        m[14] = 0
        m[15] = 1