(ns graphbrain.gbui.browsers)

(defn test-css
  [prop]
  (some #{prop} (.-style (.-documentElement js/document))))

(defn opera?
  "Opera 8.0+"
  []
  (and (.-opera js/window) (.-version (.-opera js/window))))
  
(defn firefox?
  "FF 0.8+"
  []
  (test-css "MozBoxSizing"))
           
(defn safari?
  "At least Safari 3+: '[object HTMLElementConstructor]'"
  []
  (> (.indexOf (str (.-HTMLElement js/window)) "Constructor") 0))
  
(defn chrome?
  "Chrome 1+"
  []
  (and (not (safari?)) (test-css "WebkitTransform")))

(defn ie?
  "At least IE6"
  []
  (test-css "msTransform"))
