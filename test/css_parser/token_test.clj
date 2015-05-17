(ns css-parser.token-test
  (:require [clojure.test :refer [deftest testing is]]
            [instaparse.core :as p]))

(def gp
  (p/parser (str "debug = rule | selector | token | type\n"
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
           [:debug [:token [:border "border" [:border-color-value [:color-type "transparent"]] [:border-color-value [:color-type [:rgb [:hexadecimal "#" "fff"]]]] [:border-width-value "medium"]]]])))
  (testing "border-radius"
    (is (= (p/parse gp "border-radius: 10px 5%")
           [:debug [:token [:border-radius [:vk] "border-radius" [:top-left-bottom-right-radius [:length [:integer "10"] "px"]] [:top-right-bottom-left-radius [:percentage [:integer "5"] "%"]]]]]))
    (is (= (p/parse gp "border-radius: 1px 0 3px 4px")
           [:debug [:token [:border-radius [:vk] "border-radius" [:top-left-radius [:length [:integer "1"] "px"]] [:top-right-radius [:length "0"]] [:bottom-right-radius [:length [:integer "3"] "px"]] [:bottom-left-radius [:length [:integer "4"] "px"]]]]]))
    (is (= (p/parse gp "border-radius: 10px 5px 2em / 20px 25px 30%")
           [:debug [:token [:border-radius [:vk] "border-radius" [:top-left-radius [:length [:integer "10"] "px"]] [:top-right-bottom-left-radius [:length [:integer "5"] "px"]] [:bottom-right-radius [:length [:integer "2"] "em"]] "/" [:top-left-radius [:length [:integer "20"] "px"]] [:top-right-bottom-left-radius [:length [:integer "25"] "px"]] [:bottom-right-radius [:percentage [:integer "30"] "%"]]]]]))
    (is (= (p/parse gp "border-radius: 10px 5px 2em / 20px 25px 30%")
           [:debug [:token [:border-radius [:vk] "border-radius" [:top-left-radius [:length [:integer "10"] "px"]] [:top-right-bottom-left-radius [:length [:integer "5"] "px"]] [:bottom-right-radius [:length [:integer "2"] "em"]] "/" [:top-left-radius [:length [:integer "20"] "px"]] [:top-right-bottom-left-radius [:length [:integer "25"] "px"]] [:bottom-right-radius [:percentage [:integer "30"] "%"]]]]]))
    (is (= (p/parse gp "border-radius: 10px 5px 2em / 20px 25px 30% 23px")
           [:debug [:token [:border-radius [:vk] "border-radius" [:top-left-radius [:length [:integer "10"] "px"]] [:top-right-bottom-left-radius [:length [:integer "5"] "px"]] [:bottom-right-radius [:length [:integer "2"] "em"]] "/" [:top-left-radius [:length [:integer "20"] "px"]] [:top-right-radius [:length [:integer "25"] "px"]] [:bottom-right-radius [:percentage [:integer "30"] "%"]] [:bottom-left-radius [:length [:integer "23"] "px"]]]]]))))

(deftest flex-test
  (testing "the flox property"
    (is (= (p/parse gp "flex-flow: column-reverse wrap")
           [:debug [:token [:flex-flow "flex-flow" [:flex-direction-value "column-reverse"] [:flex-wrap-value "wrap"]]]]))
    (is (= (p/parse gp "flex-flow: wrap column-reverse")
           [:debug [:token [:flex-flow "flex-flow" [:flex-wrap-value "wrap"] [:flex-direction-value "column-reverse"]]]])))
  (testing "flex"
    (is (= (p/parse gp "flex: none")
           [:debug [:token [:flex [:vk] "flex" "none"]]]))
    (is (= (p/parse gp "flex: 30%")
           [:debug [:token [:flex [:vk] "flex" [:flex-basis-value [:percentage [:integer "30"] "%"]]]]]))
    (is (= (p/parse gp "flex: 2")
           [:debug [:token [:flex [:vk] "flex" [:flex-grow-value [:integer "2"]]]]]))
    (is (= (p/parse gp "flex: 1 30px")
           [:debug [:token [:flex [:vk] "flex" [:flex-grow-value [:integer "1"]] [:flex-basis-value [:length [:integer "30"] "px"]]]]]))
    (is (= (p/parse gp "flex: 2 2")
           [:debug [:token [:flex [:vk] "flex" [:flex-grow-value [:integer "2"]] [:flex-shrink-value [:integer "2"]]]]]))
    (is (= (p/parse gp "flex: 2 2 10%")
           [:debug [:token [:flex [:vk] "flex" [:flex-grow-value [:integer "2"]] [:flex-shrink-value [:integer "2"]] [:flex-basis-value [:percentage [:integer "10"] "%"]]]]]))))

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


(deftest font-test
  (testing "font"
    (is (= (p/parse gp "font: bold italic large serif")
           [:debug [:token [:font "font" [:font-weight-value "bold"] [:font-style-value "italic"] [:font-size-value "large"] [:generic-font-family "serif"]]]]))
    (is (= (p/parse gp "font: 80% sans-serif")
           [:debug [:token [:font "font" [:font-size-value [:percentage [:integer "80"] "%"]] [:generic-font-family "sans-serif"]]]]))
    (is (= (p/parse gp "font: status-bar")
           [:debug [:token [:font "font" "status-bar"]]])))
  (testing "family-name"
    (is (= (p/parse gp "font-family: Times New, \"Times New Roman\", Georgia, serif")
           [:debug [:token [:font-family "font-family" [:family-font-name [:custom-ident "T" "imes" ""] [:custom-ident "N" "ew" ""]] [:family-font-name [:string "Times New Roman"]] [:family-font-name [:custom-ident "G" "eorgia" ""]] [:generic-font-family "serif"]]]]))
    (is (= (p/parse gp "font-family: serif")
           [:debug [:token [:font-family "font-family" [:generic-font-family "serif"]]]]))
    (is (= (p/parse gp "font-family: Times New Roman")
           [:debug [:token [:font-family "font-family" [:family-font-name [:custom-ident "T" "imes" ""] [:custom-ident "N" "ew" ""] [:custom-ident "R" "oman" ""]]]]]))))

(deftest column-test
  (testing "column"
    (is (= (p/parse gp "columns: 1 3em")
           [:debug [:token [:columns "columns" [:column-count-value [:integer "1"]] [:column-width-value [:length [:integer "3"] "em"]]]]]))
    (is (= (p/parse gp "columns: 3")
           [:debug [:token [:columns "columns" [:column-count-value [:integer "3"]]]]]))
    (is (= (p/parse gp "columns: 3em")
           [:debug [:token [:columns "columns" [:column-width-value [:length [:integer "3"] "em"]]]]]))))

(deftest margin-value
  (testing "margins"
    (is (= (p/parse gp "margin: 1em")
           [:debug [:token [:margin "margin" [:margin-all [:length [:integer "1"] "em"]]]]]))
    (is (= (p/parse gp "margin: 1em 2em")
           [:debug [:token [:margin "margin" [:margin-top-value [:length [:integer "1"] "em"]] [:margin-hor-value [:margin-all [:length [:integer "2"] "em"]]]]]]))
    (is (= (p/parse gp "margin: 1em 2em auto")
           [:debug [:token [:margin "margin" [:margin-top-value [:length [:integer "1"] "em"]] [:margin-hor-value [:margin-all [:length [:integer "2"] "em"]]] [:margin-bottom-value "auto"]]]]))
    (is (= (p/parse gp "margin: 1em 2em auto 3%")
           [:debug [:token [:margin "margin" [:margin-top-value [:length [:integer "1"] "em"]] [:margin-right-value [:length [:integer "2"] "em"]] [:margin-bottom-value "auto"] [:margin-left-value [:percentage [:integer "3"] "%"]]]]]))))


(deftest counter
  (testing "counter-reset"
    (is (= (p/parse gp "counter-reset: counter1 1")
           [:debug [:token [:counter-reset "counter-reset" [:identifier [:custom-ident "c" "ounter" "1"]] [:integer "1"]]]]))
    (is (= (p/parse gp "counter-reset: counter1 1 counter2 4")
           [:debug [:token [:counter-reset "counter-reset" [:identifier [:custom-ident "c" "ounter" "1"]] [:integer "1"] [:identifier [:custom-ident "c" "ounter" "2"]] [:integer "4"]]]]))))

(deftest column-rule-test
  (testing "column-rule"
    (is (= (p/parse gp "column-rule: blue")
           [:debug [:token [:column-rule "column-rule" [:column-rule-color-value [:color-type [:color-keyword "blue"]]]]]]))
    (is (= (p/parse gp "column-rule: inset blue")
           [:debug [:token [:column-rule "column-rule" [:column-rule-style-value "inset"] [:column-rule-color-value [:color-type [:color-keyword "blue"]]]]]]))
    (is (= (p/parse gp "column-rule: 23px inset blue")
           [:debug [:token [:column-rule "column-rule" [:column-rule-width-value [:length [:integer "23"] "px"]] [:column-rule-style-value "inset"] [:column-rule-color-value [:color-type [:color-keyword "blue"]]]]]]))))

(deftest padding
  (testing "padding"
    (is (= (p/parse gp "padding: 1em")
           [:debug [:token [:padding "padding" [:padding-value [:length [:integer "1"] "em"]]]]]))
    (is (= (p/parse gp "padding: 1em 3px")
           [:debug [:token [:padding "padding" [:padding-horizontal [:padding-value [:length [:integer "1"] "em"]]] [:padding-vertical [:padding-value [:length [:integer "3"] "px"]]]]]]))
    (is (= (p/parse gp "padding: 1em 3px 30px")
           [:debug [:token [:padding "padding" [:padding-top-value [:length [:integer "1"] "em"]] [:padding-horizontal [:padding-value [:length [:integer "3"] "px"]]] [:padding-bottom-value [:length [:integer "30"] "px"]]]]]))
    (is (= (p/parse gp "padding: 1em 3px 30px 5px")
           [:debug [:token [:padding "padding" [:padding-top-value [:length [:integer "1"] "em"]] [:padding-right-value [:length [:integer "3"] "px"]] [:padding-left-value [:length [:integer "30"] "px"]] [:padding-bottom-value [:length [:integer "5"] "px"]]]]]))))

(deftest outline-test
  (testing "outline"
    (is (= (p/parse gp "outline: solid")
           [:debug [:token [:outline "outline" [:outline-style-value "solid"]]]]))
    (is (= (p/parse gp "outline: solid 1px")
           [:debug [:token [:outline "outline" [:outline-style-value "solid"] [:outline-width-value [:length [:integer "1"] "px"]]]]]))
    (is (= (p/parse gp "outline: solid black 1px")
           [:debug [:token [:outline "outline" [:outline-style-value "solid"] [:outline-color-value [:color-type [:color-keyword "black"]]] [:outline-width-value [:length [:integer "1"] "px"]]]]]))
    (is (= (p/parse gp "outline: 1px #000")
           [:debug [:token [:outline "outline" [:outline-width-value [:length [:integer "1"] "px"]] [:outline-color-value [:color-type [:rgb [:hexadecimal "#" "000"]]]]]]]))))

(deftest cursor
  (testing "cursor"
    (is (= (p/parse gp "cursor:  url(cursor1.png) 4 12, auto")
           [:debug [:token [:cursor [:vk] "cursor" [:url "cursor1.png"] [:integer "4"] [:integer "12"] "auto"]]]))))

(deftest animation
  (testing "animation"
    (is (= (p/parse gp "animation: slidein 3s")
           [:debug [:token [:animation [:vk] "animation" [:animation-name-value [:identifier [:custom-ident "s" "lidein" ""]]] [:animation-duration-value [:time [:integer "3"] "s"]]]]]))
    (is (= (p/parse gp "animation: slidein 3s ease-in 1s")
           [:debug [:token [:animation [:vk] "animation" [:animation-name-value [:identifier [:custom-ident "s" "lidein" ""]]] [:animation-duration-value [:time [:integer "3"] "s"]] [:animation-timing-function-value [:transition-timing-function-value [:timing-function "ease-in"]]] [:animation-delay-value [:time [:integer "1"] "s"]]]]]))
    (is (= (p/parse gp "animation: slidein 3s ease-in 1s 2 reverse both paused")
           [:debug [:token [:animation [:vk] "animation" [:animation-name-value [:identifier [:custom-ident "s" "lidein" ""]]] [:animation-duration-value [:time [:integer "3"] "s"]] [:animation-timing-function-value [:transition-timing-function-value [:timing-function "ease-in"]]] [:animation-delay-value [:time [:integer "1"] "s"]] [:animation-iteration-count-value [:integer "2"]] [:animation-direction-value "reverse"] [:animation-fill-mode-value "both"] [:animation-play-state-value "paused"]]]]))))

(deftest content-test
  (testing "content"
    (is (= (p/parse gp "content: open-quote")
           [:debug [:token [:content "content" "open-quote"]]]))
    (is (= (p/parse gp "content: \"Chapter \"")
           [:debug [:token [:content "content" [:string "Chapter "]]]]))
    (is (= (p/parse gp "content : url(https://www.mozilla.org/favicon.ico) \" MOZILLA: \"")
           [:debug [:token [:content "content" [:url "https://www.mozilla.org/favicon.ico"] [:string " MOZILLA: "]]]]))))

(deftest text-decoration-test
  (testing "text-decoration"
    (is (= (p/parse gp "text-decoration: wavy")
           [:debug [:token [:text-decoration "text-decoration" [:text-decoration-style-value "wavy"]]]]))
    (is (= (p/parse gp "text-decoration: underline overline blink")
           [:debug [:token [:text-decoration "text-decoration" [:text-decoration-line-value "underline"] [:text-decoration-line-value "overline"] [:text-decoration-line-value "blink"]]]]))
    (is (= (p/parse gp "text-decoration: underline wavy red")
           [:debug [:token [:text-decoration "text-decoration" [:text-decoration-line-value "underline"] [:text-decoration-style-value "wavy"] [:text-decoration-color-value [:color-type [:color-keyword "red"]]]]]]))
    (is (= (p/parse gp "text-decoration: inherit")
           [:debug [:token [:text-decoration "text-decoration" "inherit"]]]))))

(deftest background-test
  (testing "background"
    (is (= (p/parse gp "background: url(\"topbanner.png\") #00D repeat-y fixed")
           [:debug [:token [:background "background" [:background-image-value [:image [:url "\"topbanner.png\""]]] [:background-color-value [:color-type [:rgb [:hexadecimal "#" "00D"]]]] [:background-repeat-value "repeat-y"] [:background-attachment-value "fixed"]]]]))
    (is (= (p/parse gp "background: red")
           [:debug [:token [:background "background" [:background-color-value [:color-type [:color-keyword "red"]]]]]])))
  (testing "background-image"
    (is (= (p/parse gp "background-image: url(\"images/starsolid2.gif\")")
           [:debug [:token [:background-image "background-image" [:background-image-value [:image [:url "\"images/starsolid2.gif\""]]]]]]))))

(deftest list-style-test
  (testing "list-style"
    (is (= (p/parse gp "list-style: none")
           [:debug [:token [:list-style "list-style" [:list-style-image-value "none"]]]]))
    (is (= (p/parse gp "list-style: url('../img/dino.png')")
           [:debug [:token [:list-style "list-style" [:list-style-image-value [:url "'../img/dino.png'"]]]]]))
    (is (= (p/parse gp "list-style: inside")
           [:debug [:token [:list-style "list-style" [:list-style-position-value "inside"]]]]))
    (is (= (p/parse gp "list-style: georgian inside")
           [:debug [:token [:list-style "list-style" [:list-style-type-value "georgian"] [:list-style-position-value "inside"]]]]))
    (is (= (p/parse gp "list-style: lower-roman url('../img/dino.png') outside")
           [:debug [:token [:list-style "list-style" [:list-style-type-value "lower-roman"] [:list-style-image-value [:url "'../img/dino.png'"]] [:list-style-position-value "outside"]]]]))
    (is (= (p/parse gp "list-style: square")
           [:debug [:token [:list-style "list-style" [:list-style-type-value "square"]]]]))))

(deftest transform-test
  (testing "transform function"
    (is (= (p/parse gp "transform: matrix(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:matrix "matrix(" [:float "1" "." "0"] "," [:float "2" "." "0"] "," [:float "3" "." "0"] "," [:float "4" "." "0"] "," [:float "5" "." "0"] "," [:float "6" "." "0"] ")"]]]]]))
    (is (= (p/parse gp "transform: translate(12px, 50%)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:translate "translate(" [:length [:integer "12"] "px"] "," [:percentage [:integer "50"] "%"] ")"]]]]]))
    (is (= (p/parse gp "transform: translate(2em)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:translate "translate(" [:length [:integer "2"] "em"] ")"]]]]]))
    (is (= (p/parse gp "transform: translate(3in)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:translate "translate(" [:length [:integer "3"] "in"] ")"]]]]]))
    (is (= (p/parse gp "transform: scale(2, 0.5)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:scale "scale(" [:integer "2"] "," [:float "0" "." "5"] ")"]]]]]))
    (is (= (p/parse gp "transform: scaleX(2)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:scaleX "scaleX(" [:integer "2"] ")"]]]]]))
    (is (= (p/parse gp "transform: scaleY(0.5)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:scaleY "scaleY(" [:float "0" "." "5"] ")"]]]]]))
    (is (= (p/parse gp "transform: rotate(0.5turn)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:rotate "rotate(" [:angle [:float "0" "." "5"] "turn"] ")"]]]]]))
    (is (= (p/parse gp "transform: skewX(30deg)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:skewX "skewX(" [:angle [:integer "30"] "deg"] ")"]]]]]))
    (is (= (p/parse gp "transform: skewY(1.07rad)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:skewY "skewY(" [:angle [:float "1" "." "07"] "rad"] ")"]]]]]))
    (is (= (p/parse gp "transform: skew(30deg, 1.07rad)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:skew "skew(" [:angle [:integer "30"] "deg"] "," [:angle [:float "1" "." "07"] "rad"] ")"]]]]]))
    (is (= (p/parse gp "transform: matrix3d(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:matrix3d "matrix3d(" [:float "1" "." "0"] "," [:float "2" "." "0"] "," [:float "3" "." "0"] "," [:float "4" "." "0"] "," [:float "5" "." "0"] "," [:float "6" "." "0"] "," [:float "7" "." "0"] "," [:float "8" "." "0"] "," [:float "9" "." "0"] "," [:float "10" "." "0"] "," [:float "11" "." "0"] "," [:float "12" "." "0"] "," [:float "13" "." "0"] "," [:float "14" "." "0"] "," [:float "15" "." "0"] "," [:float "16" "." "0"] ")"]]]]]))
    (is (= (p/parse gp "transform: translate3d(12px, 50%, 3em)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:translate3d "translate3d(" [:length [:integer "12"] "px"] "," [:percentage [:integer "50"] "%"] "," [:length [:integer "3"] "em"] ")"]]]]]))
    (is (= (p/parse gp "transform: translateZ(2px)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:translateZ "translateZ(" [:length [:integer "2"] "px"] ")"]]]]]))
    (is (= (p/parse gp "transform: scale3d(2.5, 1.2, 0.3)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:scale3d "scale3d(" [:float "2" "." "5"] "," [:float "1" "." "2"] "," [:float "0" "." "3"] ")"]]]]]))
    (is (= (p/parse gp "transform: scaleZ(0.5)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:scaleZ "scaleZ(" [:float "0" "." "5"] ")"]]]]]))
    (is (= (p/parse gp "transform: rotate3d(1, 2.0, 3.0, 10deg)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:rotate3d "rotate3d(" [:integer "1"] "," [:float "2" "." "0"] "," [:float "3" "." "0"] "," [:angle [:integer "10"] "deg"] ")"]]]]]))
    (is (= (p/parse gp "transform: rotateX(10deg)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:rotateX "rotateX(" [:angle [:integer "10"] "deg"] ")"]]]]]))
    (is (= (p/parse gp "transform: rotateY(10deg)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:rotateY "rotateY(" [:angle [:integer "10"] "deg"] ")"]]]]]))
    (is (= (p/parse gp "transform: rotateZ(10deg)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:rotateZ "rotateZ(" [:angle [:integer "10"] "deg"] ")"]]]]]))
    (is (= (p/parse gp "transform: perspective(17px)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:perspective-transform "perspective(" [:length [:integer "17"] "px"] ")"]]]]]))
    (is (= (p/parse gp "transform: translateX(10px) rotate(10deg) translateY(5px)")
           [:debug [:token [:transform [:vk] "transform" [:transform-function [:translateX "translateX(" [:length [:integer "10"] "px"] ")"]] [:transform-function [:rotate "rotate(" [:angle [:integer "10"] "deg"] ")"]] [:transform-function [:translateY "translateY(" [:length [:integer "5"] "px"] ")"]]]]]))))
