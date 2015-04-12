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

(deftest gradient-test
  (testing "gradient linear"
    (is (= (p/parse gp "-moz-linear-gradient(left,yellow,blue)")
           (p/parse gp "-moz-linear-gradient( left, yellow, blue )")
[:type [:image [:gradient [:linear-gradient [:linear-gradient-name "-moz-linear-gradient"] [:side-or-corner "left"] [:color-stop [:color [:color-keyword "yellow"]]] [:color-stop [:color [:color-keyword "blue"]]]]]]]))
    (is (= (p/parse gp "linear-gradient(to right,red,yellow)")
           (p/parse gp "linear-gradient( to   right, red , yellow )")
           [:type [:image [:gradient [:linear-gradient [:linear-gradient-name "linear-gradient"] [:side-or-corner "right"] [:color-stop [:color [:color-keyword "red"]]] [:color-stop [:color [:color-keyword "yellow"]]]]]]])))
  (testing "radial gradient"
    (is (= (p/parse gp "radial-gradient(ellipse farthest-corner at 45px 45px , #00FFFF 0%, rgba(0, 0, 255, 0) 50%, #0000FF 95%)")
           (p/parse gp "radial-gradient( ellipse farthest-corner at  45px  45px, #00FFFF 0% , rgba(0, 0, 255 , 0) 50%, #0000FF  95%)")
           [:type [:image [:gradient [:radial-gradient [:radial-gradient-name "radial-gradient"] [:radial-gradient-center "ellipse" [:extent-keyword "farthest-corner"] [:position [:length [:integer "45"] "px"] [:length [:integer "45"] "px"]]] [:color-stop [:color [:rgb [:hexadecimal "#" "00FFFF"]]] [:percentage [:integer "0"] "%"]] [:color-stop [:color [:rgba [:integers-rgb [:integer "0"] [:integer "0"] [:integer "255"]] [:integer "0"]]] [:percentage [:integer "50"] "%"]] [:color-stop [:color [:rgb [:hexadecimal "#" "0000FF"]]] [:percentage [:integer "95"] "%"]]]]]]))
    (is (= (p/parse gp "radial-gradient(ellipse farthest-corner at 470px 47px , #FFFF80 20%, rgba(204, 153, 153, 0.4) 30%, #E6E6FF 60%)")
           [:type [:image [:gradient [:radial-gradient [:radial-gradient-name "radial-gradient"] [:radial-gradient-center "ellipse" [:extent-keyword "farthest-corner"] [:position [:length [:integer "470"] "px"] [:length [:integer "47"] "px"]]] [:color-stop [:color [:rgb [:hexadecimal "#" "FFFF80"]]] [:percentage [:integer "20"] "%"]] [:color-stop [:color [:rgba [:integers-rgb [:integer "204"] [:integer "153"] [:integer "153"]] [:float "0" "." "4"]]] [:percentage [:integer "30"] "%"]] [:color-stop [:color [:rgb [:hexadecimal "#" "E6E6FF"]]] [:percentage [:integer "60"] "%"]]]]]]))
    (is (= (p/parse gp "radial-gradient(farthest-corner at 45px 45px , #FF0000 0%, #0000FF 100%)")
           (p/parse gp "radial-gradient(  farthest-corner at 45px 45px , #FF0000   0%  , #0000FF   100%)")
           [:type [:image [:gradient [:radial-gradient [:radial-gradient-name "radial-gradient"] [:radial-gradient-center [:extent-keyword "farthest-corner"] [:position [:length [:integer "45"] "px"] [:length [:integer "45"] "px"]]] [:color-stop [:color [:rgb [:hexadecimal "#" "FF0000"]]] [:percentage [:integer "0"] "%"]] [:color-stop [:color [:rgb [:hexadecimal "#" "0000FF"]]] [:percentage [:integer "100"] "%"]]]]]]))
    (is (= (p/parse gp "radial-gradient(16px at 60px 50% , #000000 0%, #000000 14px, rgba(0, 0, 0, 0.3) 18px, rgba(0, 0, 0, 0) 19px)")
           (p/parse gp "radial-gradient(16px at 60px 50% , #000000 0%, #000000 14px, rgba(0, 0, 0, 0.3) 18px, rgba(0, 0, 0, 0) 19px)")
           [:type [:image [:gradient [:radial-gradient [:radial-gradient-name "radial-gradient"] [:radial-gradient-center [:length [:integer "16"] "px"] [:position [:length [:integer "60"] "px"] [:percentage [:integer "50"] "%"]]] [:color-stop [:color [:rgb [:hexadecimal "#" "000000"]]] [:percentage [:integer "0"] "%"]] [:color-stop [:color [:rgb [:hexadecimal "#" "000000"]]] [:length [:integer "14"] "px"]] [:color-stop [:color [:rgba [:integers-rgb [:integer "0"] [:integer "0"] [:integer "0"]] [:float "0" "." "3"]]] [:length [:integer "18"] "px"]] [:color-stop [:color [:rgba [:integers-rgb [:integer "0"] [:integer "0"] [:integer "0"]] [:integer "0"]]] [:length [:integer "19"] "px"]]]]]]))))

(deftest image-test
  (testing "image"
    (is (= (p/parse gp "url(test.jpg)")
           [:type [:image [:url "test.jpg"]]]))
    (is (= (p/parse gp "linear-gradient(to bottom, blue, red)")
           [:type [:image [:gradient [:linear-gradient [:linear-gradient-name "linear-gradient"] [:side-or-corner "bottom"] [:color-stop [:color [:color-keyword "blue"]]] [:color-stop [:color [:color-keyword "red"]]]]]]]))
    (is (= (p/parse gp "element(#colonne3)")
           [:type [:image [:element "colonne3"]]]))))

(deftest integer-test
  (testing "good integers"
    (is (= (p/parse gp "12")
           [:type [:integer "12"]]))
    (is (= (p/parse gp "+123")
           [:type [:integer "+" "123"]]))
    (is (= (p/parse gp "-456")
           [:type [:integer "-" "456"]]))
    (is (= (p/parse gp "0")
           [:type [:integer "0"]]))
    (is (= (p/parse gp "+0")
           [:type [:integer "+" "0"]]))
    (is (= (p/parse gp "-0")
           [:type [:integer "-" "0"]])))
  (testing "wrong integers"
    (is (not (= :integer
                (-> (p/parse gp "12.0")
                    second first))))
    (is (= :float (-> (p/parse gp "12.0")
                    second first)))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "12.")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "+---12")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "ten")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "_5")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "\35")))
    ;; (is (instance? instaparse.gll.Failure ;; not work in clojure 
    ;;                (p/parse gp "\4E94")))
    (is (not (= :integer
                (-> (p/parse gp "3e4")
                    second first))))
    (is (= :scientific-number
             (-> (p/parse gp "3e4")
                 second first)))
    ))

(deftest lenght-test
  (testing "Relative units"
    (is (= (p/parse gp "0.1em")
           [:type [:length [:float "0" "." "1"] "em"]]))
    (is (= (p/parse gp "2em")
           [:type [:length [:integer "2"] "em"]]))
    (is (= (p/parse gp "2rem")
           [:type [:length [:integer "2"] "rem"]]))
    (is (= (p/parse gp "2ex")
           [:type [:length [:integer "2"] "ex"]]))
    (is (= (p/parse gp "2ch")
           [:type [:length [:integer "2"] "ch"]])))
  (testing "Viewport-percentage lenght"
    (is (= (p/parse gp "2vh")
           [:type [:length [:integer "2"] "vh"]]))
    (is (= (p/parse gp "2vw")
           [:type [:length [:integer "2"] "vw"]]))
    (is (= (p/parse gp "2vmin")
           [:type [:length [:integer "2"] "vmin"]]))
    (is (= (p/parse gp "2vmax")
           [:type [:length [:integer "2"] "vmax"]])))
  (testing "Absolute lenght units"
    (is (= (p/parse gp "2px")
           [:type [:length [:integer "2"] "px"]]))
    (is (= (p/parse gp "2mm")
           [:type [:length [:integer "2"] "mm"]]))
    (is (= (p/parse gp "2cm")
           [:type [:length [:integer "2"] "cm"]]))
    (is (= (p/parse gp "2in")
           [:type [:length [:integer "2"] "in"]]))
    (is (= (p/parse gp "2pt")
           [:type [:length [:integer "2"] "pt"]]))
    (is (= (p/parse gp "2pc")
           [:type [:length [:integer "2"] "pc"]])))
  (testing ""
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "2 pc")))))

(deftest number-test
  (testing "valid numbers"
    (is (= (p/parse gp "12")
           [:type [:integer "12"]]))
    (is (= (p/parse gp "4.01")
           [:type [:float "4" "." "01"]]))
    (is (= (p/parse gp "-456.8")
           [:type [:float "-" "456" "." "8"]]))
    (is (= (p/parse gp "0.0")
           [:type [:float "0" "." "0"]]))
    (is (= (p/parse gp "+0.0")
           [:type [:float "+" "0" "." "0"]]))
    (is (= (p/parse gp "-0.0")
           [:type [:float "-" "0" "." "0"]]))
    (is (= (p/parse gp ".60")
           [:type [:only-decimal "." "60"]]))
    (is (= (p/parse gp "60e3")
           [:type [:scientific-number [:integer "60"] "e" [:integer "3"]]]))
    (is (= (p/parse gp "-3.4e-2")
           [:type [:scientific-number [:float "-" "3" "." "4"] "e" [:integer "-" "2"]]])))
  (testing "invalid numbers"
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "12.")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "+-12.2")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "12.2.1")))))
