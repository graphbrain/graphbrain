// VisualObj
var makeVisualObj = function(that) {
	that.rect = [];
    that.rect.v1 = [];
    that.rect.v2 = [];
    that.rect.v3 = [];
    that.rect.v4 = [];

    that.rect.v1.x = 0;
    that.rect.v1.y = 0;
    that.rect.v2.x = 0;
    that.rect.v2.y = 0;
    that.rect.v3.x = 0;
    that.rect.v3.y = 0;
    that.rect.v4.x = 0;
    that.rect.v4.y = 0;

	that.overlaps = function(obj) {
		return rotRectsOverlap(that.rect, obj.rect);
	}
}