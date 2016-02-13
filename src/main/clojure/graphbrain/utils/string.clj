(ns graphbrain.utils.string)

(defn contains-char?
  [s c]
  (some #(= c %) s))

(defn contains-space?
  [s]
  (contains-char? s \space))

(defn no-spaces?
  [s]
  (not (contains-space? s)))
