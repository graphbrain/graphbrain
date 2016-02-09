(ns graphbrain.db.edgestr)

(defn- open-pars
  [str]
  (loop [s str
         n 0]
    (if (not= \( (first s))
      n
      (recur (rest s) (inc n)))))

(defn- close-pars
  [str]
  (loop [s str
         n 0]
    (if (not= \) (last s))
      n
      (recur (drop-last s) (inc n)))))

(declare str->edge)

(defn- token-type
  [token]
  (loop [s token
         pos 0
         point false]
    (if (empty? s)
      (if point :double :integer)
      (let [c (first s)]
        (cond (and (= c \-) (> pos 0)) :string
              (or (< c \0) (> c \9)) :string
              (= c \.) (if point
                         :string
                         (recur (rest s) (inc pos) true)))))))

(defn- parsed-token
  [token]
  (if (= (first token) \()
    (str->edge token)
    (case (token-type token)
      :string token
      :integer (.Integer token)
      :double (.Double token))))

(defn split-edge-inner-str
  [edge-inner-str]
  (let [stoks (clojure.string/split edge-inner-str #" ")]
    (loop [st stoks
           tokens []
           curtok nil
           depth 0]
      (if (empty? st) tokens
          (let [tok (first st)
                depth (- (+ depth (open-pars tok)) (close-pars tok))
                bottom (= depth 0)
                curtok (str curtok (if curtok " ") tok)
                tokens (if bottom (conj tokens curtok) tokens)
                curtok (if bottom nil curtok)]
            (recur (rest st) tokens curtok depth))))))

(defn str->edge
  [edge-str]
  (let [edge-inner-str (.substring edge-str 1 (dec (count edge-str)))]
    (apply vector
           (map parsed-token (split-edge-inner-str edge-inner-str)))))

(defn edge->str
  [edge]
  (if (coll? edge)
    (str "(" (clojure.string/join " "
                                  #(if (coll? %) (edge->str %) (str %)) edge) ")")
    (str edge)))
