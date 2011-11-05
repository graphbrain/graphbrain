// UI modes
var dragMode = function(event) {
    uiMode = 'drag';
    $("#dragModeButton").addClass("selModeButton");
    $("#addModeButton").removeClass("selModeButton");
}

var addMode = function(event) {
    uiMode = 'add';
    $("#dragModeButton").removeClass("selModeButton");
    $("#addModeButton").addClass("selModeButton");
    $("#tip").html('Try clicking & dragging!');
    $("#tip").fadeIn("slow", function(){tipVisible = true;});    
}