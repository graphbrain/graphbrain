(ns graphbrain.gbui.quaternion)

(defn quaternion
  []
  {:x 0 :y 0 :z 0 :w 1})

(defn normalise
  "Normalise the quaternion so that it's length is 1
   Does not do anything if current length is within a certain tolerance"
  [quat]
  (let [TOLERANCE 0.00001
        x (:x quat)
        y (:y quat)
        z (:z quat)
        w (:w quat)
        l (+ (* x x) (* y y) (* z z) (* w w))]
    (if (> (Math/abs (- l 1)) TOLERANCE)
      (let [l (Math/sqrt l)
            x (/ x l)
            y (/ y l)
            z (/ z l)
            w (/ w l)]
        {:x x :y y :z z :w w})
      quat)))

(defn from-euler
  [quat pitch yaw roll]
  (let [sinp (Math/sin pitch)
        siny (Math/sin yaw)
        sinr (Math/sin roll)
        cosp (Math/cos pitch)
        cosy (Math/cos yaw)
        cosr (Math/cos roll)
        x (- (* sinr cosp cosy) (* cosr sinp siny))
        y (+ (* cosr sinp cosy) (* sinr cosp siny))
        z (- (* cosr cosp siny) (* sinr sinp cosy))
        w (+ (* cosr cosp cosy) (* sinr sinp siny))]
    (normalise {:x x :y y :z z :w w})))

(defn mul
  "Multiply quaternion q1 by q2
   Purpose:
   Changes rotation represented by q1 by rotation represented by q2"
  [q1 q2]
  (let [x1 (:x q1)
        y1 (:y q1)
        z1 (:z q1)
        w1 (:w q1)
        x2 (:x q2)
        y2 (:y q2)
        z2 (:z q2)
        w2 (:w q2)
        x (- (+ (* w1 x2) (* x1 w2) (* y1 z2)) (* z1 y2))
        y (+ (- (* w1 y2) (* x1 z2)) (* y1 w2) (* z1 x2))
        z (+ (- (+ (* w1 z2) (* x1 y2)) (* y1 x2)) (* z1 w2))
        w (- (* w1 w2) (* x1 x2) (* y1 y2) (* z1 z2))]
    {:x x :y y :z z :w w}))

(defn matrix
  "Creates affine transformation matrix for the rotation represented by
   this quaternion."
  [quat]
  (let [x (:x quat)
        y (:y quat)
        z (:z quat)
        w (:w quat)
        x2 (* x x)
        y2 (* y y)
        z2 (* z z)
        xy (* x y)
        xz (* x z)
        yz (* y z)
        wx (* w x)
        wy (* w y)
        wz (* w z)
        m0 (- 1 (* 2 (+ y2 z2)))
        m1 (* 2 (- xy wz))
        m2 (* 2 (+ xz wy))
        m3 0
        m4 (* 2 (+ xy wz))
        m5 (- 1 (* 2 (+ x2 z2)))
        m6 (* 2 (- yz wx))
        m7 0
        m8 (* 2 (- xz wy))
        m9 (* 2 (+ yz wx))
        m10 (- 1 (* 2 (+ x2 y2)))
        m11 0
        m12 0
        m13 0
        m14 0
        m15 1]
    [m0 m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14 m15]))
