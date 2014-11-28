(ns graphbrain.gbui.mat
  "Collection of linear algebra functions for vectors with 3 elements and 4x4 matrices. Useful for 3D calculations.")

(defn v3dotv3
  "dot product of n vectors"
  [& matrix]
  (apply + (apply map * matrix)))

(defn v3length
  [v]
  (Math/sqrt (apply + (map #(* % %) v))))

(defn v3diff-length
  [v1 v2]
  (v3length (map - v2 v1)))

(defn m4x4mulv3
  "m: 4x4 matrix
   v: vector with 3 elements
   returns m * v"
  [m v]
  (let [tx (nth m 3)
        ty (nth m 7)
        tz (nth m 11)
        w (+ (v3dotv3 v [tx ty tz]) (nth m 15))
        tx (nth m 0)
        ty (nth m 4)
        tz (nth m 8)
        rx (/ (+ (v3dotv3 v [tx ty tz]) (nth m 12)) w)
        tx (nth m 1)
        ty (nth m 5)
        tz (nth m 9)
        ry (/ (+ (v3dotv3 v [tx ty tz]) (nth m 13)) w)
        tx (nth m 2)
        ty (nth m 6)
        tz (nth m 10)
        rz (/ (+ (v3dotv3 v [tx ty tz]) (nth m 14)) w)]
    [rx ry rz]))
