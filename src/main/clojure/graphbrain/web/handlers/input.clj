(ns graphbrain.web.handlers.input
  (:require [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.web.handlers.search :as search]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.urlnode :as url]
            [graphbrain.db.knowledge :as k]
            [graphbrain.db.perms :as perms]
            [graphbrain.disambig.edgeguesser :as edg]
            [graphbrain.kr.pagereader :as pr]
            [graphbrain.eco.eco :as eco]
            [graphbrain.eco.parsers.chat :as chat]
            [graphbrain.string :as gbstr]))

(defn- goto-id
  [root-id vertex]
  (if (maps/edge? vertex)
    (if (= (maps/edge-type vertex) "r/*edges")
      (goto-id root-id
               (maps/id->edge
                (first
                 (maps/participant-ids vertex))))
      (id/eid->id
       (second
        (maps/ids vertex))))
    root-id)
  )

(defn- input-reply-fact
  [root-id vertex]
  (pr-str {:type :fact
           :newedges (list (:id vertex))
           :gotoid (goto-id root-id vertex)}))

(defn- input-reply-url
  [root-id]
  (pr-str {:type :url
           :gotoid root-id}))

(defn- input-reply-search
  [count results mode]
  (pr-str {:type :search
           :mode mode
           :count count
           :results results}))

(defn- input-reply-definition
  [root-id rel param]
  (pr-str {:type :def
           :root-id root-id
           :rel rel
           :param param}))

(defn- input-reply-error
  [msg]
  (pr-str {:type :error
           :msg msg}))

(defn- sentence-type
  [sentence]
  (cond
   (and (gbstr/no-spaces? sentence)
        (or (.startsWith sentence "http://")
            (.startsWith sentence "https://"))) :url
            (.startsWith sentence "x ") :intersect
            :else :fact))

(defn- is-definer?
  [edge]
  (= (id/edge-rel edge) "r/is"))

(defn- definer->rel
  [edge]
  (case (id/edge-rel edge)
    "r/is" "r/+t"))

(defn- definer->param
  [edge]
  (case (id/edge-rel edge)
    "r/is" (-> edge
               id/id->ids
               (nth 2))))

(defn- process-fact
  [request user root sentence ctxt ctxts]
  (let
      [root-id (:id root)
       env {:root root-id
            :user (:id user)}
       res (eco/parse-str chat/chat sentence env)]
    (if (id/edge? res)
      (if (perms/can-edit? common/gbdb (:id user) ctxt)
        (if (and (id/undefined-eid? (:eid root))
                 (is-definer? res))
          (input-reply-definition root-id
                                  (definer->rel res)
                                  (definer->param res))
          (let [edge-id (edg/guess common/gbdb res sentence (:id user) ctxts)
                edge (maps/id->vertex edge-id)
                edge (assoc edge :score 1)]
            (k/addfact! common/gbdb edge ctxt (:id user))
            (common/log request (str "fact added: " edge
                                     "; input: " sentence
                                     "; ctxt: " ctxt
                                     (if root-id (str "; root: " root-id))))
            (input-reply-fact root-id edge)))
        (do
          (common/log request (str "INPUT FAILED (no permissions). "
                                   "input: " sentence
                                   "; ctxt: " ctxt
                                   (if root-id (str "; root: " root-id))))
          (input-reply-error
           "Sorry, you don't have permissions to edit this GraphBrain.")))
      (do
        (common/log request (str "INPUT FAILED (don't understand). "
                                 "input: " sentence
                                 "; ctxt: " ctxt
                                 (if root-id (str "; root: " root-id))))
        (input-reply-error "Sorry, I don't understand.")))))

(defn process-search
  [request user root q ctxt ctxts mode]
  (let [q (if (= mode :intersect)
            (clojure.string/trim (subs q 1))
            q)
        results (search/results q ctxts)
        root-id (:id root)]
    (if (empty? results)
      (process-fact request user root q ctxt ctxts)
      (do
        (common/log request (str "search "
                                 "input: " q
                                 "; ctxt: " ctxt
                                 (if root-id (str "; root: " root-id))))
        (search/reply results mode)))))

(defn process-url
  [request user root sentence ctxt ctxts]
  (let [url-id (url/url->id sentence)
        root-id (:id root)]
    (pr/extract-knowledge! common/gbdb sentence ctxt ctxts (:id user))
    (common/log request (str "extract knowledge from url "
                             "input: " sentence
                             "; ctxt: " ctxt
                             (if root-id (str "; root: " root-id))))
    (input-reply-url url-id)))

(defn handle
  [request]
  (let [sentence ((request :form-params) "sentence")
        root-id ((request :form-params) "root")
        targ-ctxt ((request :form-params) "targ-ctxt")
        user (common/get-user request)
        ctxts (contexts/active-ctxts targ-ctxt user)
        root (if root-id (gb/getv common/gbdb root-id ctxts))]
    (common/log request (str "input: " sentence
                             "; ctxt: " targ-ctxt
                             (if root-id (str "; root: " root-id))))
    (case (sentence-type sentence)
      :fact (process-search request user root sentence targ-ctxt ctxts :search)
      :url (process-url request user root sentence targ-ctxt ctxts)
      :intersect (process-search
                  request user root sentence targ-ctxt ctxts :intersect))))
