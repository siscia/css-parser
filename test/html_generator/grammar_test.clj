(ns grammar-test
  (:require [clojure.test :refer [deftest testing is]]
            [instaparse.core :as p]))

(def gp (p/parser (slurp "grammar.bnf")))

(deftest gp-ok
  (testing "The grammar parser is been initialized."
      (is (not (nil? gp)))))

(deftest angle-test
  (testing "the deg postfix"
    (is (= (p/parse gp "90deg")
           [:type [:angle [:integer "90"] "deg"]])))
  (testing "the grad postfix"
    (is (= (p/parse gp "85grad")
           [:type [:angle [:integer "85"] "grad"]])))
  (testing "the rad postfix"
    (is (= (p/parse gp "80rad")
           [:type [:angle [:integer "80"] "rad"]])))
  (testing "the turn postfix"
    (is (= (p/parse gp "75turn")
           [:type [:angle [:integer "75"] "turn"]])))
  (testing "the negative number and floats"
    (is (= (p/parse gp "-90deg")
           [:type [:angle [:integer "-" "90"] "deg"]]))
    (is (= (p/parse gp "-85.32rad")
           [:type [:angle [:float "-" "85" "." "32"] "rad"]])))
  (testing "the zero value"
    (is (= (p/parse gp "0rad")
           [:type [:angle [:integer "0"] "rad"]]))))

(deftest color-test
  (testing "color keyword"
    (is (= (p/parse gp "rebeccapurple")
           [:type [:color [:color-keyword "rebeccapurple"]]]))
    (is (= (p/parse gp "black")
           [:type [:color [:color-keyword "black"]]]))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "foobar"))))
  (testing "rgb color with #"
    (is (= (p/parse gp "#234567")
           [:type [:color [:rgb [:hexadecimal "#" "234567"]]]]))
    (is (= (p/parse gp "#aFb")
           [:type [:color [:rgb [:hexadecimal "#" "aFb"]]]]))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "#zzz"))) ;; not hexadecimal
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "#0000")))) ;; or 3 or 6 characters
  (testing "rgb color with function call"
    (is (= (p/parse gp "rgb(255,0,51)")
           (p/parse gp "rgb( 255, 0, 51 )")
           [:type [:color [:rgb [:functional [:integers-rgb [:integer "255"] [:integer "0"] [:integer "51"]]]]]]))
    (is (= (p/parse gp "rgb(100%,0%,20%)")
           (p/parse gp "rgb( 100%, 0%, 20% )")
           [:type [:color [:rgb [:functional [:percentage-rgb [:percentage [:integer "100"] "%"] [:percentage [:integer "0"] "%"] [:percentage [:integer "20"] "%"]]]]]]))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "rgb(255, 0, 51.2)")))
    (is (= (p/parse gp "rgb(100%,0%,20%)")
           (p/parse gp "rgb( 100% , 0% , 20% )")
           [:type [:color [:rgb [:functional [:percentage-rgb [:percentage [:integer "100"] "%"] [:percentage [:integer "0"] "%"] [:percentage [:integer "20"] "%"]]]]]]))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "rgb(100%, 0, 20%)"))))
  (testing "hsl color"
    (is (= (p/parse gp "hsl(60, 100%,50%)")
           (p/parse gp "hsl( 60,100%, 50% )")
           [:type [:color [:hsl [:integer "60"] [:percentage [:integer "100"] "%"] [:percentage [:integer "50"] "%"]]]]))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "hsl(60, 100,50%)"))))
  (testing "rgba color"
    (is (= (p/parse gp "rgba(255 ,0,0,0.1)")
           (p/parse gp "rgba( 255, 0, 0 , 0.1 )")
           [:type [:color [:rgba [:integers-rgb [:integer "255"] [:integer "0"] [:integer "0"]] [:float "0" "." "1"]]]])
        )
    (is (= (p/parse gp "rgba(100%, 0%, 20%, 0.6)")
           (p/parse gp "rgba( 100% , 0% , 20%, 0.6 )")
           [:type [:color [:rgba [:percentage-rgb [:percentage [:integer "100"] "%"] [:percentage [:integer "0"] "%"] [:percentage [:integer "20"] "%"]] [:float "0" "." "6"]]]])))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "rgba(60, 100, 45, 50%)")))
    (testing "hsla color"
      (is (= (p/parse gp "hsla(240,100%,50%,0.05)")
             (p/parse gp "hsla( 240 , 100% ,50% ,0.05 )")
             [:type [:color [:hsla [:integer "240"] [:percentage [:integer "100"] "%"] [:percentage [:integer "50"] "%"] [:float "0" "." "05"]]]]))))
