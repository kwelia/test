(ns task.office
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as str]
            [clj-http.client :as client])
  (:import (java.net URL)))

(defn- fetch-data [url]
  (html/html-resource (URL. url)))

(defn- extract-data
  "avoid \n values and get content inside links of cities"
  [html-offices]
  (reduce
    (fn [acc curr]
      (let [content (first (:content curr))]
        (cond
          (= curr "\n") acc
          (:content content) (conj acc (:content content))
          :else (conj acc content))))
    []
    html-offices))

(defn- html->offices
  "Transforms HTML elements to office. Logic: every collection is a city of the last state found according to the array order."
  [html]
  (let [country "USA"]
    (loop [elements html
           state (first html)
           offices []]
      (let [element (first elements)]
        (cond
          (nil? element) offices
          (coll? element) (recur (next elements)
                                 state
                                 (conj offices (str/join ", " [(first element) state country])))
          :else (recur (next elements)
                       element
                       offices))))))

(defn fetch-all
  [base-url]
  (-> (str base-url "/offices")
      fetch-data
      (html/select [:.state-offices-list])
      (as-> $
            (map :content $))
      flatten
      extract-data
      html->offices))

(defn- fetch-geolocation-values
  [base-url place-id]
  (-> (str base-url "/api/getPlacesDetails")
      (client/get {:query-params {:place_id place-id}
                   :as           :json})

      :body
      (get-in [:result :geometry :location])))

(defn- fetch-place-id
  [base-url location]
  (-> (str base-url "/api/getSearchSuggestions")
      (client/get {:query-params {:term location}
                   :as           :json})
      :body
      (get-in [:predictions 0 :place_id])))

(defn- make-min-max-map
  [{:keys [lat lng]}]
  (let [lat-step 2
        lng-step 6]
    (hash-map :min_lat (- lat lat-step)
              :max_lat (+ lat lat-step)
              :min_lng (- lng lng-step)
              :max_lng (+ lng lng-step))))

(defn fetch-geolocation
  [base-url location]
  (->> (fetch-place-id base-url location)
      (fetch-geolocation-values base-url)
      make-min-max-map))
