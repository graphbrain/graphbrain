(ns graphbrain.hg.edgestr)

(defn- open-pars
  "Number of consecutive open parenthesis at the beginning of the string."
  [str]
  (loop [s str
         n 0]
    (if (not= \( (first s))
      n
      (recur (rest s) (inc n)))))

(defn- close-pars
  "Number of consecutive close parenthesis at the end of the string."
  [str]
  (loop [s str
         n 0]
    (if (not= \) (last s))
      n
      (recur (drop-last s) (inc n)))))

(declare str->edge)

(defn- token-type
  "Determine the type of a string token: string, integer or double."
  [token]
  (loop [s token
         pos 0
         point false]
    (if (empty? s)
      (if point :double :integer)
      (let [c (first s)]
        (cond (= c \-) (if (> pos 0)
                         :string
                         (recur (rest s) (inc pos) point))
              (= c \.) (if point
                         :string
                         (recur (rest s) (inc pos) true))
              (or (< (int c) (int \0)) (> (int c) (int \9))) :string
              :default (recur (rest s) (inc pos) point))))))

(defn- parsed-token
  "Transform a string token into a value of the correct type."
  [token]
  (if (= (first token) \()
    (str->edge token)
    (case (token-type token)
      :string token
      :integer (Integer. token)
      :double (Double. token))))

(defn split-edge-str
  "Shallow split into tokens of a string representation of an edge, with or without outer parenthesis."
  [edge-str]
  (let [edge-inner-str (if (= (first edge-str) \()
                         (.substring edge-str 1 (dec (count edge-str)))
                         edge-str)
        stoks (clojure.string/split edge-inner-str #" ")]
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
  "Convert a string representation of an edge to an edge."
  [edge-str]
  (apply vector
         (map parsed-token (split-edge-str edge-str))))

(defn edge->str
  "Convert an edge to its string representation."
  [edge]
  (if (coll? edge)
    (str "(" (clojure.string/join " "
                                  (map #(if (coll? %) (edge->str %) (str %)) edge)) ")")
    (str edge)))
