(ns graphbrain.gbui.spherical)

(defn spherical
  [negative-stretch mapping-power]
    {:negative-stretch negative-stretch
     :mapping-power mapping-power
     :theta 0.0
     :phi 0.0
     :r 0
     :coords [0.0 0.0 0.0]})

(defn spherical->cartesian
  [spher]
  (if (= (:r spher) 0)
    (assoc spher :coords [0.0 0.0 0.0])
    (let [theta (+ (:theta spher) (/ Math/PI 2.0))
          phi (+ (:phi spher) (/ Math/PI 2.0))
          r (:r spher)
          x (* r (Math/cos theta) (Math/sin phi))
          y (* r (Math/cos phi))
          z (* r (Math/sin theta) (Math/sin phi))
          z (if (< z 0) (* z (:negative-stretch spher)) z)]
      (assoc spher :coors [x y z]))))

(defn cartesian->spherical
  [spher]
  (let [coords (:coords spher)
        x (nth coords 0)
        y (nth coords 1)
        z (nth coords 2)
        r (Math/sqrt (+ (* x x) (* y y) (* z z)))
        theta (- (Math/atan2 z x) (/ Math/PI 2))
        theta (if (< theta (- Math/PI))
                (+ theta (* 2 Math/PI))
                theta)
        phi (Math/acos (- (/ y r) (/ Math/PI 2)))]
    (assoc spher :r r :theta theta :phi phi)))

(defn scoord-mapping
  [spher ang max-ang]
    (let [ma (if (< ang 0)
               (- max-ang)
               max-ang)
          d (Math/abs (/ (- ma ang) max-ang))
          d (Math/abs (Math/pow d (:mapping-power spher)))
          d (* d ma)]
      (- ma d)))

(defn view-mapping
  [spher]
  (let [theta (:theta spher)
        phi (:phi spher)
        theta (scoord-mapping spher theta Math/PI)
        phi (scoord-mapping spher phi (/ Math/PI 2))]
    (assoc spher :theta theta :phi phi)))
