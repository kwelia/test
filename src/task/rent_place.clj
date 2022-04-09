(ns task.rent-place
  (:require [clj-http.client :as client]))

(defn fetch-all
  [base-url geolocation-params]
  (-> (str base-url "/search/properties/for-rent")
      (client/get {:query-params geolocation-params
                   :headers      {"X-Requested-With" "XMLHttpRequest"}
                   :as           :json})
      :body
      :properties
      (as-> $
            (map :id $))))

(defn make-rental-link
  [base-url id]
  (str base-url "/rental-listings/" id))

(defn make-rental-link-list
  [base-url ids]
  (pmap (partial make-rental-link base-url) (flatten ids)))
