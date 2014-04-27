(ns graphbrain.db.edgeparser)

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

(defn split-edge
  [ep]
  (let [stoks (clojure.string/split ep #" ")]
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
