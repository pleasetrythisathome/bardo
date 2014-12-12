(ns build.util
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]))

(defn deps []
  (->> "deps"
       io/file
       file-seq
       (filter #(.isFile %))
       (filter (comp (partial = "edn") last #(str/split % #"\.") #(.getName %)))
       (sort-by #(.getName %))
       (map (comp read-string slurp))
       (reduce into [])))
