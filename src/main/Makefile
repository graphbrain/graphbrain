SOURCES =	js/seedrandom.js \
    		js/jquery.cookie.js \
    		js/jquery.mousewheel.js \
    		js/slimScroll.js \
    		js/browsers.js \
    		js/vec3mat4x4.js \
    		js/Quaternion.js \
    		js/alerts.js \
    		js/animation.js \
    		js/interface.js \
    		js/Node.js \
    		js/SphericalCoords.js \
    		js/SNode.js \
    		js/layout.js \
    		js/Graph.js \
    		js/search.js \
    		js/disambiguate.js \
    		js/undo.js \
    		js/user.js \
    		js/relations.js \
    		js/aichat.js \
    		js/remove.js \
    		js/State.js \
    		js/main.js

.PHONY: all push clean

all: resources/js/gbui.js

resources/js/gbui.js: $(SOURCES)
	@echo "/* graphbrain.com */" > $@
	@cat $^ >> $@

clean:
	@rm resources/js/gbui.js
