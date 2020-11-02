(ns sql-compose.core-test
  (:require [clojure.test :refer :all]
            [sql-compose.core :refer [sql where AND OR insert-into set-values]]))

(deftest sql-test
  (testing "it works"
    (let [got (sql "select * from users"
                    (where (AND ["a =" 1]
                                (OR ["b =" 2]
                                    ["c =" 3]))))
          expected ["select * from users WHERE (a = ? AND (b = ? OR c = ?))" 1 2 3]]
      (is (= got expected)))))

(deftest test-where
  (testing "if vector applies 'AND'"
    (let [got (sql (where ["a =" 1] ["b =" 2]))
          expected ["WHERE (a = ? AND b = ?)" 1 2]]
      (is (= got expected))))

  (testing "if string it simply joins the clause"
    (let [got (sql (where "a = 1"))
          expected ["WHERE a = 1"]]
      (is (= got expected))))
  
  (testing "it handles map"
    (let [got (sql (where {:a 1 :b 2}))
          expected ["WHERE (a = ? AND b = ?)" 1 2]]
      (is (= got expected)))))

(deftest test-AND
  (testing "it works"
    (let [got (sql (AND ["a =" 1] ["b =" 2]))
          expected ["(a = ? AND b = ?)" 1 2]]
      (is (= got expected)))))

(deftest test-OR
  (testing "it works"
    (let [got (sql (OR ["a =" 1] ["b =" 2]))
          expected ["(a = ? OR b = ?)" 1 2]]
      (is (= got expected)))))

(deftest test-insert-into
  (testing "it handles single record"
    (let [got (sql (insert-into :users {:id 1 :address "sydney"}))
          expected ["INSERT INTO users (id, address) VALUES (?, ?)"
                    1
                    "sydney"]]
      (is (= got expected))))

  (testing "it handles multiple records"
    (let [got (sql (insert-into :users [{:id 1 :address "sydney"}
                                    {:id 2 :address "kathmandu"}]))
          expected ["INSERT INTO users (id, address) VALUES (?, ?), (?, ?)" 1 "sydney" 2 "kathmandu"]]
      (is (= got expected)))))


(deftest test-set-values
  (let [got (sql (set-values {:a 1 :b 2}))
        expected ["SET a = ?, b = ?" 1 2]]
    (is (= got expected))))
