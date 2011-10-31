// Aux
var rotateAndTranslate = function(point, angle, tx, ty) {
    var x = point[0];
    var y = point[1];

    var rx = Math.cos(angle) * x - Math.sin(angle) * y;
    var ry = Math.sin(angle) * x + Math.cos(angle) * y;

    x = rx + tx;
    y = ry + ty;

    point[0] = x;
    point[1] = y;
}


var dotProduct = function(p0, p1) {
    return (p0[0] * p1[0]) + (p0[1] * p1[1]);
}


var pointInTriangle = function(A, B, C, P) {
    var v0 = [0, 0];
    var v1 = [0, 0];
    var v2 = [0, 0];
    
    v0[0] = C[0] - A[0];
    v0[1] = C[1] - A[1];
    v1[0] = B[0] - A[0];
    v1[1] = B[1] - A[1];
    v2[0] = P[0] - A[0];
    v2[1] = P[1] - A[1];

    var dot00 = dotProduct(v0, v0);
    var dot01 = dotProduct(v0, v1);
    var dot02 = dotProduct(v0, v2);
    var dot11 = dotProduct(v1, v1);
    var dot12 = dotProduct(v1, v2);

    var invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
    var u = (dot11 * dot02 - dot01 * dot12) * invDenom;
    var v = (dot00 * dot12 - dot01 * dot02) * invDenom;

    return (u > 0) && (v > 0) && (u + v < 1);
}
