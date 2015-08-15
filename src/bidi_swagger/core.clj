(ns bidi-swagger.core
  (:require
    [bidi.bidi :refer [match-route gather route-seq tag path-for]]
    [bidi.ring :refer [make-handler]]
    [ring.util.response :refer [not-found status response]]
    [ring.middleware.params :refer [wrap-params]]))

(defn swag-path-param
  "Create swagger info for a path parameter."
  [param]
  {:name      (name param)
   :paramType "path"
   :type      "string"
   :required  true})

(defn param-to-string
  "Convert a keyword parameter to swagger parameter string."
  [param]
  (if (keyword? param)
    (str "{" (name param) "}")
    param))

(defprotocol PathElement
  "Bidi path elements define the path and request properties of a route."
  (match-p [_]))

(extend-protocol PathElement
  clojure.lang.Keyword                                      ;request-method
  (match-p [key]
    {:methods [(.toUpperCase (name key))]})

  String                                                    ;path element
  (match-p [this]
    {:path this})

  clojure.lang.APersistentVector
  (match-p [segmented]
    {:path        (map param-to-string segmented)
     :path-params (map swag-path-param (filter keyword? segmented))})

  java.util.regex.Pattern
  (match-p [this]
    {:path (str this)}))

;TODO handle more complex guards than just method type

(defn swag-path
  "Find the swagger URL path and request methods for a route"
  [path]
  (let [elements (apply merge-with (comp vec flatten vector) (map match-p path))]
    {:path       (apply str (:path elements))
     :operations (for [method (:methods elements)]
                   {:method     method
                    :parameters (or (:path-params elements) [])})}))

(defn swag-docs
  "Create base swagger info for a route. Docs-fn should take a handler id and return
  some swagger docs info to be merged into the swagger path definition. "
  [route docs docs-fn]
  (let [path (swag-path (:path route))
        docs (docs-fn docs (:handler route))]
    (println docs " - " (:operations path))
    {(:path path)
     (map #(merge-with concat % docs) (:operations path))}))

(defn merge-paths
  [docs]
  (apply merge-with into docs))

(defn swaggup
  [docs]
  (for [d docs] {:path       (first d)
                 :operations (second d)}))

(defn swag-routes
  "Takes a set of bidi routes and additional documentation and returns swagger API documenttation.
  Docs-fn should be a function (fn [docs handler]) that returns appropriate documentation for a
  handler id. The default implementation is (get docs handler)."
  ([routes docs docs-fn]
   (let [docs (map #(swag-docs % docs docs-fn) (route-seq routes))]
     (-> docs merge-paths swaggup)))
  ([routes docs]
   (swag-routes routes docs get)))



