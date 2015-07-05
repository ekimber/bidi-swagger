(ns bidi-swagger.core
  (:require
    [bidi.bidi :refer [match-route gather route-seq tag path-for]]
    [bidi.ring :refer [make-handler]]
    [ring.util.response :refer [not-found status response]]
    [ring.middleware.params :refer [wrap-params]]))

(defn swag-path-param
  [param]
  {:name      (name param)
   :paramType "path"
   :type      "string"
   :required  true})

(defn param-to-string
  [param]
  (if (keyword? param)
    (str "{" (name param) "}")
    param))

(defprotocol PathElement
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
     :path-params (map swag-path-param (filter keyword? segmented))}))

(defn swag-path
  [path]
  (let [elements (apply merge-with (comp vec flatten vector) (map match-p path))]
    {:path       (apply str (:path elements))
     :operations (for [method (:methods elements)]
                   {:method     method
                    :parameters (or (:path-params elements) [])})}))

(defn swag-docs
  [route docs docs-fn]
  (let [path (swag-path (:path route))
        docs (docs-fn (:handler route) docs)]
    {(:path path)
     (map #(merge % docs) (:operations path))}))

(defn merge-paths
  [docs]
  (apply merge-with into docs))

(defn swaggup
  [docs]
  (for [d docs] {:path       (first d)
                 :operations (second d)}))

(defn swag-routes
  ([routes docs docs-fn]
   (let [docs (map #(swag-docs % docs docs-fn) (route-seq routes))]
     (-> docs merge-paths swaggup)))
  ([routes docs]
   (swag-routes routes docs (fn [handler docs] (get docs handler)))))



