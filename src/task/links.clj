(ns task.links
  (:require
    [task.office :as office]
    [task.rent-place :as rent-place]))

(def ^:dynamic *base-url* "https://renterswarehouse.com")

(defn fetch-all-links
  []
  (->> (office/fetch-all *base-url*)
       (pmap (partial office/fetch-geolocation *base-url*))
       (pmap (partial rent-place/fetch-all *base-url*))
       (rent-place/make-rental-link-list *base-url*)))

(comment
  (fetch-all-links))
