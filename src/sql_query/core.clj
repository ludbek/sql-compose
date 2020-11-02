(ns sql-query.core
  (:require [clojure.string :as string]
            [clojure.test :refer [function?]]))

(defn get-name [kword]
  (string/replace (name kword) "-" "_"))

(defn sql [& frags]
  (let [values (atom [])
        get-placeholder (fn [value]
                          (swap! values #(conj %1 value))
                          "?")
        str-frags (map #(if (string? %) % (% get-placeholder)) frags)]
    (into [(string/join " " str-frags)] @values)))

(defn AND [& args]
  (fn [get-placeholder]
    (str
      "("
      (string/join " AND " (map #(if (string? %)
                            %
                            (if (function? %)
                              (% get-placeholder)
                              (str (first %)
                                 " "
                                 (get-placeholder (second %)))))
                         args))
      ")")))

(defn OR [& args]
  (fn [get-placeholder]
    (str
      "("
      (string/join " OR " (map #(if (string? %)
                            %
                            (if (function? %)
                              (% get-placeholder)
                              (str (first %)
                                 " "
                                 (get-placeholder (second %)))))
                         args))
      ")")))

(defn where [& clauses]
  (fn [get-placeholder]
    (let [the-clause (first clauses)]
      (str "WHERE "
       (cond 
        (string? the-clause) the-clause
        (map? the-clause) ((apply AND
                                 (map #(conj []
                                             (str (get-name (first %))
                                                  " =")
                                             (second %))
                                      (seq the-clause)))
                           get-placeholder)
        (function? the-clause) (the-clause get-placeholder)
        :else ((apply AND clauses) get-placeholder))))))

(defn get-columns [columns]
  (str "(" (string/join ", " (map #(get-name %) columns)) ")"))

(defn get-value [columns row get-placeholder]
  (str "("
       (string/join ", " (map #(get-placeholder (get row %)) columns))
       ")"))

(defn insert-into [table-name values]
  (fn [get-placeholder]
    (if (map? values)
      (let [columns (keys values)]
        (string/join " " ["INSERT INTO"
                   (get-name table-name)
                   (get-columns columns)
                   "VALUES"
                   (get-value columns values get-placeholder)]))
      (if (vector? values)
        (let [columns (keys (first values))]
          (string/join " " ["INSERT INTO"
                     (get-name table-name)
                     (get-columns columns)
                     "VALUES"
                     (string/join ", "
                           (map #(get-value columns % get-placeholder)
                                values))]))))))

(defn set-values [values]
  (fn [get-placeholder]
    (str "SET "
         (string/join ", "
                 (map #(str (get-name (first %))
                            " = "
                            (get-placeholder (second %))) (seq values))))))

