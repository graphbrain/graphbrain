/**
 * (c) 2012 GraphBrain Ltd. All rigths reserved.
 */

// Geom

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


/*
Return the intersection point between the line segment defined by (x1, y1) and (x2, y2)
and a rectangle defined by (rleft, rtop, rright, rbottom)

(x1, y1) is assumed to be inside the rectangle and (x2, y2) outside 
*/
var interRect = function(x1, y1, x2, y2, rleft, rtop, rright, rbottom) {
    var t, tx, ty, edge;
    
    var dx = x2 - x1;
    var dy = y2 - y1;
    
    if ((dx == 0) && (dy == 0)) {
        return 0;
    }

    // Let x = x1 + dx * t  and calculate t at the intersection point with a vertical border.
    if (dx != 0) {
        var edge;
        if (dx > 0) {
            edge = rright;
        }
        else {
            edge = rleft;
        }
        tx = (edge - x1) / dx;
    }

    // Let y = y1 + dy * t and calculate t for the vertical border.
    if (dy != 0) {
        var edge;
        if (dy > 0) {
            edge = rbottom;
        }
        else {
            edge = rtop;
        }
        ty = (edge - y1) / dy;
    }

    // Then take the shorter one.
    if (dx == 0) {
        t = ty;
    }
    else if (dy == 0) {
        t = tx;
    }
    else {
        if (tx < ty) {
            t = tx;
        }
        else {
            t = ty;
        }
    }

    // Calculate the coordinates of the intersection point.
    var ix = x1 + dx * t;
    var iy = y1 + dy * t;
    return [ix, iy];
}


var rectsOverlap = function(r1_x1, r1_y1, r1_x2, r1_y2, r2_x1, r2_y1, r2_x2, r2_y2) {
    if (r1_x1 < r2_x2 && r1_x2 > r2_x1 && r1_y1 < r2_y2 && r1_y2 > r2_y1) {
        return true;
    }
    return false;
}


var sepAxisSide = function(a1, a2, point) {
    //var rx = -a1.y;
    //var ry = a1.x;

    //var dp = rx * (point.x - a2.x) + ry * (point.y - a2.y);

    var dp = ((a2.x - a1.x) * (point.y - a1.y)) - ((a2.y - a1.y) * (point.x - a1.x));

    if (dp < 0)
        return -1;
    else
        return 1;
}


var sepAxis = function(a1, a2, point, rect) {
    var sign1 = sepAxisSide(a1, a2, point);
    var sign2 = sepAxisSide(a1, a2, rect.v1);

    if (sign1 == sign2)
        return false;

    if (sign2 != sepAxisSide(a1, a2, rect.v2))
        return false;
    if (sign2 != sepAxisSide(a1, a2, rect.v3))
        return false;
    if (sign2 != sepAxisSide(a1, a2, rect.v4))
        return false;
    
    return true;
}


var rotRectsOverlap = function(rect1, rect2) {
    if (sepAxis(rect1.v1, rect1.v2, rect1.v3, rect2))
        return false;
    if (sepAxis(rect1.v2, rect1.v3, rect1.v1, rect2))
        return false;
    if (sepAxis(rect1.v3, rect1.v4, rect1.v1, rect2))
        return false;
    if (sepAxis(rect1.v4, rect1.v1, rect1.v2, rect2))
        return false;
    if (sepAxis(rect2.v1, rect2.v2, rect2.v3, rect1))
        return false;
    if (sepAxis(rect2.v2, rect2.v3, rect2.v1, rect1))
        return false;
    if (sepAxis(rect2.v3, rect2.v4, rect2.v1, rect1))
        return false;
    if (sepAxis(rect2.v4, rect2.v1, rect2.v2, rect1))
        return false;
    
    return true;
}


var rectsDist2 = function(r1_x1, r1_y1, r1_x2, r1_y2, r2_x1, r2_y1, r2_x2, r2_y2) {
    if (rectsOverlap(r1_x1, r1_y1, r1_x2, r1_y2, r2_x1, r2_y1, r2_x2, r2_y2)) {
        return 0;
    }

    var c1_x = r1_x1 + ((r1_x2 - r1_x1) / 2);
    var c1_y = r1_y1 + ((r1_y2 - r1_y1) / 2);
    var c2_x = r2_x1 + ((r2_x2 - r2_x1) / 2);
    var c2_y = r2_y1 + ((r2_y2 - r2_y1) / 2);

    var p1 = interRect(c1_x, c1_y, c2_x, c2_y, r1_x1, r1_y1, r1_x2, r1_y2);
    var p2 = interRect(c2_x, c2_y, c1_x, c1_y, r2_x1, r2_y1, r2_x2, r2_y2);

    var deltaX = p1[0] - p2[0];
    var deltaY = p1[1] - p2[1];

    var dist = (deltaX * deltaX) + (deltaY * deltaY);

    return dist;
}


var rectsDist = function(r1_x1, r1_y1, r1_x2, r1_y2, r2_x1, r2_y1, r2_x2, r2_y2) {
    var dist = rectsDist2(r1_x1, r1_y1, r1_x2, r1_y2, r2_x1, r2_y1, r2_x2, r2_y2);
    dist = Math.sqrt(dist);
    return dist;
}


var lineSegsOverlap = function(x1, y1, x2, y2, x3, y3, x4, y4) {
    var denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);

    var ua = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);
    var ub = (x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3);

    // lines are parallel
    if (denom == 0) {
        // coincident?
        if ((ua == 0) && (ub == 0))
            return true;
        else
            return false;
    }

    ua /= denom;
    ub /= denom;

    if ((ua >= 0) && (ua <= 1) && (ub >= 0) && (ub <= 1))
        return true;
    else
        return false;
}


var lineRectOverlap = function(x1, y1, x2, y2, rect) {
    if (lineSegsOverlap(x1, y1, x2, y2, rect.v1.x, rect.v1.y, rect.v2.x, rect.v2.y)) return true;
    if (lineSegsOverlap(x1, y1, x2, y2, rect.v2.x, rect.v2.y, rect.v3.x, rect.v3.y)) return true;
    if (lineSegsOverlap(x1, y1, x2, y2, rect.v3.x, rect.v3.y, rect.v4.x, rect.v4.y)) return true;
    if (lineSegsOverlap(x1, y1, x2, y2, rect.v4.x, rect.v4.y, rect.v1.x, rect.v1.y)) return true;
    return false;
}