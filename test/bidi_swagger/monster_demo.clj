(ns bidi-swagger.monster-demo
  (:require [clojure.test :refer :all]
            [bidi-swagger.core :refer :all]
            [bidi.bidi :refer [route-seq]]
            [bidi.ring :refer [make-handler]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.resource :as resource]))

;;
(def monster-store (atom {}))
(def mon-count (atom 0))

(defn monster-list
  [req]
  {:body @monster-store})

(defn get-monster
  [req]
  {:body (get @monster-store (:id (:route-params req)))})

(defn delete-monster
  [req]
  (swap! monster-store #(dissoc % (:id (:route-params req)))))

(defn create-monster
  [req]
  (let [new-id (str (swap! mon-count inc))]
    (swap! monster-store #(assoc % new-id (:body req)))
    {:body (get @monster-store new-id)}))

;;Doc definitions
(def monster-get-docs
  {:summary  "Get a monster"
   :produces ["application/json"]})

(def monster-create-docs
  {:summary "Create a monster"
   :consumes ["application/json"]})

(def monster-delete-docs
  {:summary "Delete a monster"})

(def monster-list-params
  [{:name      "eyes"
    :type      "integer"
    :paramType "query"
    :required  false}])

(def monster-list-docs
  {:summary    "List monsters"
   :parameters monster-list-params})

(def docs
  {:mon-list   monster-list-docs
   :get-mon    monster-get-docs
   :delete-mon monster-delete-docs
   :create-mon monster-create-docs})

(def api-routes
  ["/" {:get    {"monsters" {""        :mon-list
                             ["/" :id] :get-mon}
                 "docs"     :docs}
        :post   {"monsters" :create-mon}
        :delete {["monsters/" :id] :delete-mon}}])


(defn docs-handler
  [req]
  {:body {:apiVersion     "0.1.0"
          :swaggerVersion "1.2"
          :info           {:description "Monster Demo API"
                           :title       "Monster Demo"}
          :produces       ["application/json"]
          :basePath       "/"
          :apis           (swag-routes api-routes docs)}})

(def handlers
  {:mon-list   monster-list
   :get-mon    get-monster
   :create-mon create-monster
   :delete-mon delete-monster
   :docs       docs-handler})

(defn get-handler
  [id]
  (get handlers id))

(def bidi-handler
  (-> (make-handler api-routes get-handler)
      wrap-json-response
      wrap-json-body
      (resource/wrap-resource "swagger-ui")))

(defn init
  []
  (swap! monster-store assoc
         (str (swap! mon-count inc)) {:name "cyclops"
                                      :eyes 1
                                      :legs 2}
         (str (swap! mon-count inc)) {:name "aaarrrg"
                                      :eyes 99999
                                      :legs 2}))