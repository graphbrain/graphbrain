// Link
var Link = function(id, orig, sorig, targ, starg, type) {
    this.id = id;
    this.orig = orig;
    this.sorig = sorig;
    this.targ = targ;
    this.starg = starg;
    this.type = type;
    this.ox = 0;
    this.oy = 0;
    this.tx = 0;
    this.ty = 0;
}

Link.prototype.draw = function(context) {
    var x0 = this.ox;
    var y0 = this.oy;
    if (this.orig) {
        x0 = this.orig.x;
        y0 = this.orig.y;
    }
    else if (this.sorig) {
        x0 = this.sorig.x;
        y0 = this.sorig.y;
    }
    var x1 = this.tx;
    var y1 = this.ty;
    if (this.targ) {
        x1 = this.targ.x;
        y1 = this.targ.y;
    }
    else if (this.starg) {
        x1 = this.starg.x;
        y1 = this.starg.y;
    }

    var cx = x0 + ((x1 - x0) / 2)
    var cy = y0 + ((y1 - y0) / 2)

    var slope = (y1 - y0) / (x1 - x0);
    var angle = Math.atan(slope);

    var color = '#FFD326';
    var textcolor = '#000'

    if (~this.type.indexOf('direct'))
        color = '#BEE512';
    else if (~this.type.indexOf('char'))
        color = '#DFFD59';
    else if (~this.type.indexOf('play'))
        color = '#FFFC26';
    else if (~this.type.indexOf('is')) {
        color = '#ED9107';
        textcolor = '#FFF'
    }

    context.strokeStyle = color;
    context.fillStyle = color;
    context.lineWidth = 0.7;
    context.beginPath();
    context.moveTo(x0, y0);
    context.lineTo(x1, y1);
    context.stroke();

    context.font = "10pt Sans-Serif";
    var dim = context.measureText(this.type);
    var width = dim.width + 6;
    var height = 18;
    
    var p = [[0, 0], [0, 0], [0, 0], [0,0], [0,0]];
    this.points = p;

    context.beginPath();
    if ((x0 < x1) || ((x0 == x1) && (y0 < y1))) {
        p[0][0] = -(width / 2);     p[0][1] = -(height / 2);
        p[1][0] = -(width / 2);     p[1][1] = (height / 2);
        p[2][0] = (width / 2);      p[2][1] = (height / 2);
        p[3][0] = (width / 2) + 6;  p[3][1] = 0;
        p[4][0] = (width / 2);      p[4][1] = -(height / 2);
    }
    else {
        p[0][0] = -(width / 2);     p[0][1] = -(height / 2);
        p[1][0] = -(width / 2) - 6; p[1][1] = 0;
        p[2][0] = -(width / 2);     p[2][1] = (height / 2);
        p[3][0] = (width / 2);      p[3][1] = (height / 2);
        p[4][0] = (width / 2);      p[4][1] = -(height / 2);
    }

    for (i = 0; i < 5; i++) {
        rotateAndTranslate(p[i], angle, cx, cy);
    }

    context.moveTo(p[0][0], p[0][1]);
    for (i = 1; i < 5; i++) {
        context.lineTo(p[i][0], p[i][1]);
    }
    
    context.closePath();
    context.fill();

    context.save();
    context.translate(cx, cy);
    context.rotate(angle);
    context.fillStyle = textcolor;
    context.textAlign = "center";
    context.textBaseline = "middle";
    context.fillText(this.type, 0, 0);
    context.restore();
}

Link.prototype.pointInLabel = function(p) {
    return (pointInTriangle(this.points[0], this.points[1], this.points[2], p)
        || pointInTriangle(this.points[2], this.points[3], this.points[4], p)
        || pointInTriangle(this.points[0], this.points[2], this.points[4], p));
}
