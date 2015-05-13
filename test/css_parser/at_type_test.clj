(ns css-parser.at-type-test
  (:require [clojure.test :refer [deftest testing is]]
            [instaparse.core :as p]))

(def gp
  (p/parser (str "debug = at-rules\n"
                 (slurp "rules.bnf") "\n" (slurp "types.bnf"))))

(deftest gp-ok
  (testing "The grammar parser is been initialized."
      (is (not (nil? gp)))))

(deftest charset-rule
  (testing "charset"
    (is (= (p/parse gp "@charset 'iso-8859-15'")
           [:debug [:at-rules [:charset "@charset " [:string "iso-8859-15"]]]]))
    (is (= (p/parse gp "@charset \"UTF-8\"")
           [:debug [:at-rules [:charset "@charset " [:string "UTF-8"]]]]))))


