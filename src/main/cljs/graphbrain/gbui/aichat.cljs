(ns graphbrain.gbui.aichat
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.newedges :as newedges])
  (:use [jayq.core :only [$]]))

(defonce ai-chat-visible (atom false))

(defonce chat-buffer (atom []))

(defonce chat-buffer-pos (atom 0))

(defonce chat-buffer-size (atom 100))

(defn ai-chat-goto-bottom!
  []
  (let [height (.-scrollHeight (first ($ "#ai-chat")))]
    (.scrollTop ($ "#ai-chat") height)))

(defn ai-chat-add-line-raw!
  [line]
  (.append ($ "#ai-chat-log") line)
  (ai-chat-goto-bottom!))

(defn ai-chat-add-line!
  [agent line]
  (let [html (str (if (= agent "gb")
                    "<div class='gb-line'><b>GraphBrain:</b> "
                    "<div class='user-line'><b>You:</b> ")
                  line "</div>")]
    (ai-chat-add-line-raw! html)
    (assoc @chat-buffer @chat-buffer-pos html)
    (.setItem js/localStorage (str "chatBuffer" @chat-buffer-pos) html))
  (swap! chat-buffer-pos inc)
  (if (>= @chat-buffer-pos @chat-buffer-size)
    (reset! chat-buffer-pos 0))
  (.setItem js/localStorage "chatBufferPos" @chat-buffer-pos))

(defn print-help!
  []
  (let [msg "GraphBrain allows you to record facts as relationships between
entities (web resources, objects, concepts).<br />\nTo add a fact, simply type a
sentence with a verb linking two entities (objects, concepts, websites), e.g.<br />
\n\n<b>GraphBrain likes people</b><br />\n<b>GraphBrain lives at
http://graphbrain.com</b><br />\n\nIn cases where there may be ambiguity, try to
use quotation marks, e.g.<br />\n<b>\"Burn after reading\" is a film</b> <br />"]
    (ai-chat-add-line! "gb" msg)))

(defn init-chat-buffer!
  []
  (if (.getItem js/localStorage "chatBufferPos")
    (reset! chat-buffer-pos
            (js/parseInt (.getItem js/localStorage "chatBufferPos"))))
  (doseq [pos (range @chat-buffer-size)]
    (reset! chat-buffer
            (conj @chat-buffer
                  (.getItem js/localStorage (str "chatBuffer" pos)))))
  (doseq [pos (range @chat-buffer-pos @chat-buffer-size)]
    (let [line (nth @chat-buffer pos)]
      (if line (ai-chat-add-line-raw! line))))
  (doseq [pos (range @chat-buffer-pos)]
    (let [line (nth @chat-buffer pos)]
      (if line (ai-chat-add-line-raw! line))))
  (if (nil? (.getItem js/localStorage "chatBufferPos"))
    (print-help!)))

(defn clear-chat-buffer!
  []
  (.removeItem js/localStorage "chatBufferPos")
  (doseq [pos (range @chat-buffer-size)]
    (.removeItem js/localStorage (str "chatBuffer" pos))))

(defn show-ai-chat!
  []
  (.css ($ "#ai-chat") "display" "block")
  (reset! ai-chat-visible true)
  (.setItem js/localStorage "aichat" "true")
  (ai-chat-goto-bottom!)
  (.focus ($ "#ai-chat-input")))

(defn hide-ai-chat!
  []
  (.css ($ "#ai-chat") "display" "none")
  (reset! ai-chat-visible false)
  (.setItem js/localStorage "aichat" "false"))

(defn ai-chat-reply
  [msg]
  (ai-chat-add-line! "gb" (.-sentence msg))
  (newedges/set-new-edges! (into [] (.-newedges msg)))
  (if (not (empty? (.-gotoid msg)))
    (set! (.-href js/window.location) (str "/node/" (.-gotoid msg)))))

(defn ai-chat-submit
  [msg]
  (let [sentence (.val ($ "#ai-chat-input"))]
    (ai-chat-add-line! "user" sentence)
    (.val ($ "#ai-chat-input") "")
    (if (= sentence "!clean")
      (clear-chat-buffer!)
      (if (= sentence "help")
        (print-help!)
        (jq/ajax {:type "POST"
                  :url "/ai"
                  :data (str "sentence=" sentence "&rootId="
                             (:id (first (:nodes ((:snodes @g/graph) "root")))))
                  :dataType "json"
                  :success ai-chat-reply}))))
  false)

(defn init-ai-chat!
  []
  (let [html (str "<div id='ai-chat-log' /><form id='ai-chat-form'>"
                  "<input id='ai-chat-input' type='text' /></form>")]
    (.html ($ "#ai-chat") html))
  (.submit ($ "#ai-chat-form") ai-chat-submit)
  (init-chat-buffer!)
  (if (= (.getItem js/localStorage "aichat") "false")
    (hide-ai-chat!)
    (do (.button ($ "#ai-chat-button") "toggle")
        (show-ai-chat!))))

(defn ai-chat-button-pressed!
  [msg]
  (if @ai-chat-visible
    (hide-ai-chat!)
    (show-ai-chat!)))
