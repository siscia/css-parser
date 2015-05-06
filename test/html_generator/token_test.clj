(ns css-parser.test.token-test
  (:require [clojure.test :refer [deftest testing is]]
            [instaparse.core :as p]))

(def gp
  (p/parser (str "debug = rule | selector | token\n"
                 (slurp "rules.bnf") "\n" (slurp "types.bnf"))))

(deftest gp-ok
  (testing "The grammar parser is been initialized."
      (is (not (nil? gp)))))

(deftest borders-test
  (testing "several shorthand for the borders"
    (is (= (p/parse gp "border-bottom: red dotted")
           [:debug [:token [:border-bottom "border-bottom" [:border-bottom-color-value [:color-type [:color-keyword "red"]]] [:border-bottom-style-value "dotted"]]]]))
    (is (= (p/parse gp "border-right: red")
           [:debug [:token [:border-right "border-right" [:border-right-color-value [:color-type [:color-keyword "red"]]]]]]))
    (is (= (p/parse gp "border-left: dotted red")
           [:debug [:token [:border-left "border-left" [:border-left-style-value "dotted"] [:border-left-color-value [:color-type [:color-keyword "red"]]]]]]))
    (is (= (p/parse gp "border-top: dotted red thin")
           [:debug [:token [:border-top "border-top" [:border-top-style-value "dotted"] [:border-top-color-value [:color-type [:color-keyword "red"]]] [:border-top-width-value "thin"]]]]))
    (is (= (p/parse gp "border: transparent #fff medium")
           [:debug [:token [:border "border" [:border-color-value [:color-type "transparent"]] [:border-color-value [:color-type [:rgb [:hexadecimal "#" "fff"]]]] [:border-width-value "medium"]]]]))))

(deftest flex-test
  (testing "the flox property"
    (is (= (p/parse gp "flex-flow: column-reverse wrap")
           [:debug [:token [:flex-flow "flex-flow" [:flex-direction-value "column-reverse"] [:flex-wrap-value "wrap"]]]]))
    (is (= (p/parse gp "flex-flow: wrap column-reverse")
           [:debug [:token [:flex-flow "flex-flow" [:flex-wrap-value "wrap"] [:flex-direction-value "column-reverse"]]]]))))

(deftest border-images-test
  (testing "border-image-slice"
    (is (= (p/parse gp "border-image-slice: 10% fill 7 12")
           [:debug [:token [:border-image-slice "border-image-slice" [:percentage [:integer "10"] "%"] "fill" [:integer "7"] [:integer "12"]]]]))
    (is (= (p/parse gp "border-image-slice: 10% ")
           [:debug [:token [:border-image-slice "border-image-slice" [:percentage [:integer "10"] "%"]]]])))
  (testing "border-image-source"
    (is (= (p/parse gp "border-image-source: url('image.jpg')")
           [:debug [:token [:border-image-source "border-image-source" [:image [:url "'image.jpg'"]]]]]))
    (is (= (p/parse gp "border-image-source: none")
           [:debug [:token [:border-image-source "border-image-source" "none"]]])))
  (testing "border-image-outset"
    (is (= (p/parse gp "border-image-outset: 1.5")
           [:debug [:token [:border-image-outset "border-image-outset" [:float "1" "." "5"]]]]))
    (is (= (p/parse gp "border-image-outset: 7px 12px 14 5px")
           [:debug [:token [:border-image-outset "border-image-outset" [:length [:integer "7"] "px"] [:length [:integer "12"] "px"] [:integer "14"] [:length [:integer "5"] "px"]]]])))
  (testing "border-image-repeat"
    (is (= (p/parse gp "border-image-repeat: space round")
           [:debug [:token [:border-image-repeat "border-image-repeat" "space" "round"]]]))
    (is (= (p/parse gp "border-image-repeat: space")
           [:debug [:token [:border-image-repeat "border-image-repeat" "space"]]])))
  (testing "border-image-width"
    (is (= (p/parse gp "border-image-width: 5")
           [:debug [:token [:border-image-width "border-image-width" [:integer "5"]]]]))
    (is (= (p/parse gp "border-image-width: 5% 2 auto")
           [:debug [:token [:border-image-width "border-image-width" [:percentage [:integer "5"] "%"] [:integer "2"] "auto"]]]))
    (is (= (p/parse gp "border-image-width: 5% 2em 10% auto")
           [:debug [:token [:border-image-width "border-image-width" [:percentage [:integer "5"] "%"] [:length [:integer "2"] "em"] [:percentage [:integer "10"] "%"] "auto"]]])))
  (testing "border-image"
    (is (= (p/parse gp "border-image-width: 5")
           [:debug [:token [:border-image-width "border-image-width" [:integer "5"]]]]))
    (is (= (p/parse gp "-moz-border-image:url('/files/4127/border.png') 30 30 repeat")
           [:debug [:token [:border-image [:vk "-moz-"] "border-image" [:image [:url "'/files/4127/border.png'"]] [:integer "30"] [:integer "30"] "repeat"]]]))))

(deftest transition-test
  (testing "transition-property"
    (is (= (p/parse gp "transition-property: sliding-vertically")
           [:debug [:token [:transition-property [:vk] "transition-property" [:transition-property-value [:identifier [:custom-ident "s" "liding" "-vertically"]]]]]]))
    (is (= (p/parse gp "transition-property: all, -moz-specific, sliding")
           [:debug [:token [:transition-property [:vk] "transition-property" [:transition-property-value [:identifier [:custom-ident "a" "ll" ""]]] [:transition-property-value [:identifier [:custom-ident "-" "moz" "-specific"]]] [:transition-property-value [:identifier [:custom-ident "s" "liding" ""]]]]]])))
  (testing "transition-timing-function"
    (is (= (p/parse gp "transition-timing-function: steps(3)")
           [:debug [:token [:transition-timing-function [:vk] "transition-timing-function" [:transition-timing-function-value [:timing-function [:steps "3"]]]]]]))
    (is (= (p/parse gp "transition-timing-function: step-start")
           [:debug [:token [:transition-timing-function [:vk] "transition-timing-function" [:transition-timing-function-value [:timing-function "step-start"]]]]])))
  (testing "transition-duration"
    (is (= (p/parse gp "-o-transition-duration: 10s")
           [:debug [:token [:transition-duration [:vk "-o-"] "transition-duration" [:transition-duration-value [:time [:integer "10"] "s"]]]]]))
    (is (= (p/parse gp "transition-duration: 10ms, 20s")
           [:debug [:token [:transition-duration [:vk] "transition-duration" [:transition-duration-value [:time [:integer "10"] "ms"]] [:transition-duration-value [:time [:integer "20"] "s"]]]]])))
  (testing "transition-delay"
    (is (= (p/parse gp "transition-delay: 3s")
           [:debug [:token [:transition-delay "transition-delay" [:transition-delay-value [:time [:integer "3"] "s"]]]]]))
    (is (= (p/parse gp "transition-delay: 3s, 4ms")
           [:debug [:token [:transition-delay "transition-delay" [:transition-delay-value [:time [:integer "3"] "s"]] [:transition-delay-value [:time [:integer "4"] "ms"]]]]])))
  (testing "transition"
    (is (= (p/parse gp "transition: margin-left 4s")
           [:debug [:token [:transition [:vk] "transition" [:transition-property-value [:identifier [:custom-ident "m" "argin" "-left"]]] [:transition-duration-value [:time [:integer "4"] "s"]]]]]))
    (is (= (p/parse gp "transition: margin-left 4s, color 1s")
           [:debug [:token [:transition [:vk] "transition" [:transition-property-value [:identifier [:custom-ident "m" "argin" "-left"]]] [:transition-duration-value [:time [:integer "4"] "s"]] [:transition-property-value [:identifier [:custom-ident "c" "olor" ""]]] [:transition-duration-value [:time [:integer "1"] "s"]]]]]))))
