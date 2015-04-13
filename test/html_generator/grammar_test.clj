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

(deftest basic-shape)

(deftest blend-mode
  (testing "just keyword"
    (is (= (p/parse gp "normal")
           [:type [:blend-mode "normal"]]))
    (is (= (p/parse gp "difference")
           [:type [:blend-mode "difference"]]))
    (is (= (p/parse gp "hue")
           [:type [:blend-mode "hue"]]))))

(deftest color-test
  (testing "color keyword"
    (is (= (p/parse gp "rebeccapurple")
           [:type [:color [:color-keyword "rebeccapurple"]]]))
    (is (= (p/parse gp "black")
           [:type [:color [:color-keyword "black"]]]))))
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
             [:type [:color [:hsla [:integer "240"] [:percentage [:integer "100"] "%"] [:percentage [:integer "50"] "%"] [:float "0" "." "05"]]]])))

(deftest custom-ident-test
  (testing "valid identifiers"
    (is (= (p/parse gp "nono79")
           [:type [:custom-ident "n" "ono" "79"]]))
    (is (= (p/parse gp "ground-level")
           [:type [:custom-ident "g" "round" "-level"]]))
    (is (= (p/parse gp "-test")
           [:type [:custom-ident "-" "test" ""]]))
    (is (= (p/parse gp "_internal")
           [:type [:custom-ident "_" "internal" ""]]))
    ;; (is (= (p/parse gp "\22 toto")
    ;;        [:type [:custom-ident "\22" "toto" ""]]))
    ;; (is (= (p/parse gp "bili\.bob")
    ;;        [:type [:custom-ident "b" "ili\.bob" ""]]))
    )
  (testing "wrong identifier"
    (is (not (= :custom-ident
                (-> (p/parse gp "34rem")
                    second first))))
    (is (not (= :custom-ident
                (-> (p/parse gp "-12rad")
                    second first))))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "bili.bob")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "--toto")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "'bilibob'")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "\"bilibob\"")))
    ))
    

(deftest gradient-test
  (testing "gradient linear"
    (is (= (p/parse gp "-moz-linear-gradient(left,yellow,blue)")
           (p/parse gp "-moz-linear-gradient( left, yellow, blue )")
           [:type  [:gradient [:linear-gradient [:linear-gradient-name "-moz-linear-gradient"] [:side-or-corner "left"] [:color-stop [:color [:color-keyword "yellow"]]] [:color-stop [:color [:color-keyword "blue"]]]]]]))
    (is (= (p/parse gp "linear-gradient(to right,red,yellow)")
           (p/parse gp "linear-gradient( to   right, red , yellow )")
           [:type [:gradient [:linear-gradient [:linear-gradient-name "linear-gradient"] [:side-or-corner "right"] [:color-stop [:color [:color-keyword "red"]]] [:color-stop [:color [:color-keyword "yellow"]]]]]])))
  (testing "radial gradient"
    (is (= (p/parse gp "radial-gradient(ellipse farthest-corner at 45px 45px , #00FFFF 0%, rgba(0, 0, 255, 0) 50%, #0000FF 95%)")
           (p/parse gp "radial-gradient( ellipse farthest-corner at  45px  45px, #00FFFF 0% , rgba(0, 0, 255 , 0) 50%, #0000FF  95%)")
           [:type [:gradient [:radial-gradient [:radial-gradient-name "radial-gradient"] [:radial-gradient-center "ellipse" [:extent-keyword "farthest-corner"] [:position [:length [:integer "45"] "px"] [:length [:integer "45"] "px"]]] [:color-stop [:color [:rgb [:hexadecimal "#" "00FFFF"]]] [:percentage [:integer "0"] "%"]] [:color-stop [:color [:rgba [:integers-rgb [:integer "0"] [:integer "0"] [:integer "255"]] [:integer "0"]]] [:percentage [:integer "50"] "%"]] [:color-stop [:color [:rgb [:hexadecimal "#" "0000FF"]]] [:percentage [:integer "95"] "%"]]]]]))
    (is (= (p/parse gp "radial-gradient(ellipse farthest-corner at 470px 47px , #FFFF80 20%, rgba(204, 153, 153, 0.4) 30%, #E6E6FF 60%)")
           [:type [:gradient [:radial-gradient [:radial-gradient-name "radial-gradient"] [:radial-gradient-center "ellipse" [:extent-keyword "farthest-corner"] [:position [:length [:integer "470"] "px"] [:length [:integer "47"] "px"]]] [:color-stop [:color [:rgb [:hexadecimal "#" "FFFF80"]]] [:percentage [:integer "20"] "%"]] [:color-stop [:color [:rgba [:integers-rgb [:integer "204"] [:integer "153"] [:integer "153"]] [:float "0" "." "4"]]] [:percentage [:integer "30"] "%"]] [:color-stop [:color [:rgb [:hexadecimal "#" "E6E6FF"]]] [:percentage [:integer "60"] "%"]]]]]))
    (is (= (p/parse gp "radial-gradient(farthest-corner at 45px 45px , #FF0000 0%, #0000FF 100%)")
           (p/parse gp "radial-gradient(  farthest-corner at 45px 45px , #FF0000   0%  , #0000FF   100%)")
           [:type [:gradient [:radial-gradient [:radial-gradient-name "radial-gradient"] [:radial-gradient-center [:extent-keyword "farthest-corner"] [:position [:length [:integer "45"] "px"] [:length [:integer "45"] "px"]]] [:color-stop [:color [:rgb [:hexadecimal "#" "FF0000"]]] [:percentage [:integer "0"] "%"]] [:color-stop [:color [:rgb [:hexadecimal "#" "0000FF"]]] [:percentage [:integer "100"] "%"]]]]]))
    (is (= (p/parse gp "radial-gradient(16px at 60px 50% , #000000 0%, #000000 14px, rgba(0, 0, 0, 0.3) 18px, rgba(0, 0, 0, 0) 19px)")
           (p/parse gp "radial-gradient(16px at 60px 50% , #000000 0%, #000000 14px, rgba(0, 0, 0, 0.3) 18px, rgba(0, 0, 0, 0) 19px)")
           [:type  [:gradient [:radial-gradient [:radial-gradient-name "radial-gradient"] [:radial-gradient-center [:length [:integer "16"] "px"] [:position [:length [:integer "60"] "px"] [:percentage [:integer "50"] "%"]]] [:color-stop [:color [:rgb [:hexadecimal "#" "000000"]]] [:percentage [:integer "0"] "%"]] [:color-stop [:color [:rgb [:hexadecimal "#" "000000"]]] [:length [:integer "14"] "px"]] [:color-stop [:color [:rgba [:integers-rgb [:integer "0"] [:integer "0"] [:integer "0"]] [:float "0" "." "3"]]] [:length [:integer "18"] "px"]] [:color-stop [:color [:rgba [:integers-rgb [:integer "0"] [:integer "0"] [:integer "0"]] [:integer "0"]]] [:length [:integer "19"] "px"]]]]]))))

(deftest image-test
  (testing "image, not that an image is either an url, a gradient or an element, an element is not a css type"
    (is (= (p/parse gp "url(test.jpg)")
           [:type [:url "test.jpg"]]))
    (is (= (p/parse gp "linear-gradient(to bottom, blue, red)")
           [:type [:gradient [:linear-gradient [:linear-gradient-name "linear-gradient"] [:side-or-corner "bottom"] [:color-stop [:color [:color-keyword "blue"]]] [:color-stop [:color [:color-keyword "red"]]]]]]))
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
    (some #(= % :integer)
          (map (comp first second) (p/parses gp "0")))
    (some #(= % :integer)
          (map (comp first second) (p/parses gp "+0")))
    (some #(= % :integer)
          (map (comp first second) (p/parses gp "-0"))))
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
    (is (not (= :integer
                (-> (p/parse gp "ten")
                    second first))))
    (is (= :custom-ident (-> (p/parse gp "ten")
                             second first)))
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
                 second first)))))

(deftest length-test
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

(deftest mq-boolean-test
  (testing "mq-boolean"
    (is (some #(= % :mq-boolean)
              (map (comp first second) (p/parses gp "1"))))
    (is (some #(= % :mq-boolean)
              (map (comp first second) (p/parses gp "0"))))
    (is (some #(= % :mq-boolean)
              (map (comp first second) (p/parses gp "+0"))))
    (is (some #(= % :mq-boolean)
              (map (comp first second) (p/parses gp "-0"))))))

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

(deftest percentage-test
  (testing "valid percentage"
    (is (= (p/parse gp "30%")
           [:type [:percentage [:integer "30"] "%"]]))
    (is (= (p/parse gp "25.5%")
           [:type [:percentage [:float "25" "." "5"] "%"]])))
  (testing "wrong percentage"
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "25.5 %")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "25 %")))))

(deftest position-test)

(deftest ratio-test
  (testing "right ratios"
    (is (= (p/parse gp "4/3")
           [:type [:ratio "4" "3"]]))
    (is (= (p/parse gp "100/99")
           [:type [:ratio "100" "99"]])))
  (testing "wrong ratios"
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "4.3/5")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "4/3.0")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "4 / 3")))))

(deftest resolution-test
  (testing "correct resolution"
    (is (= (p/parse gp "333dpi")
           [:type [:resolution [:integer "333"] "dpi"]]))
    (is (= (p/parse gp "333.45dpi")
           [:type [:resolution [:float "333" "." "45"] "dpi"]]))
    (is (= (p/parse gp "333dpcm")
           [:type [:resolution [:integer "333"] "dpcm"]]))
    (is (= (p/parse gp "333dppx")
           [:type [:resolution [:integer "333"] "dppx"]])))
  (testing "wrong resolution"
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "333 dpi")))))

(deftest shape-test)

(deftest string-test)

(deftest time-test
  (testing "right time"
    (is (= (p/parse gp "12s")
           [:type [:time [:integer "12"] "s"]]))
    (is (= (p/parse gp "-456ms")
           [:type [:time [:integer "-" "456"] "ms"]]))
    (is (= (p/parse gp "4.3ms")
           [:type [:time [:float "4" "." "3"] "ms"]]))
    (is (= (p/parse gp "14mS")
           [:type [:time [:integer "14"] "mS"]]))
    (is (= (p/parse gp "+0s")
           [:type [:time [:integer "+" "0"] "s"]]))
    (is (= (p/parse gp "-0ms")
           [:type [:time [:integer "-" "0"] "ms"]])))
  (testing "wrong times"
    (is (not (= :time
                (-> (p/parse gp "0")
                    second first))))
    (is (not (= :time
                (-> (p/parse gp "12")
                    second first))))
    (is (= :integer (-> (p/parse gp "12")
                        second first)))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "7 ms")))))


(deftest timing-function
  (testing "cubic bezier"
    (is (= (p/parse gp "cubic-bezier(0.1, 0.7, 1.0, 0.1)")
           [:type [:timing-function [:cubic-bezier [:float "0" "." "1"] [:float "0" "." "7"] [:float "1" "." "0"] [:float "0" "." "1"]]]]))
    (is (= (p/parse gp "cubic-bezier(0.1, -0.6, 0.2, 0)")
           [:type [:timing-function [:cubic-bezier [:float "0" "." "1"] [:float "-" "0" "." "6"] [:float "0" "." "2"] [:integer "0"]]]]))
    (is (= (p/parse gp "cubic-bezier(0, 1.1, 0.8, 4)")
           [:type [:timing-function [:cubic-bezier [:integer "0"] [:float "1" "." "1"] [:float "0" "." "8"] [:integer "4"]]]])))
  (testing "wrong bezier no logic"
    ;; (is (error? (p/parse gp "cubic-bezier(10, 0, 20, 1)"))) ;; xs must be between 0 and 1
    
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "cubic-bezier(0.1, red, 1.0, green)"))))
  (testing "steps function"
    (is (= (p/parse gp "steps(5, start)")
           [:type [:timing-function [:steps "5" [:direction-timing "start"]]]]))
    (is (= (p/parse gp "steps(5, end)")
           [:type [:timing-function [:steps "5" [:direction-timing "end"]]]]))
    (is (= (p/parse gp "steps(2)")
           [:type [:timing-function [:steps "2"]]])))
  (testing "wrong steps"
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "steps(2.0, end)")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "steps(-3, start)")))
    ;; (is (instance? instaparse.gll.Failure
    ;;                (p/parse gp "steps(0, end)"))) ;; no logic implemented
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "steps(start, 3)")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "step(1, end)")))
    (is (instance? instaparse.gll.Failure
                   (p/parse gp "steps(3 end)"))))
  (testing "keword for timing"
    (is (= (p/parse gp "linear")
           [:type [:timing-function "linear"]]))
    (is (= (p/parse gp "ease")
           [:type [:timing-function "ease"]]))
    (is (= (p/parse gp "ease-in")
           [:type [:timing-function "ease-in"]]))
    (is (= (p/parse gp "ease-in-out")
           [:type [:timing-function "ease-in-out"]]))
    (is (= (p/parse gp "ease-out")
           [:type [:timing-function "ease-out"]]))
    (is (= (p/parse gp "step-start")
           [:type [:timing-function "step-start"]]))
    (is (= (p/parse gp "step-end")
           [:type [:timing-function "step-end"]]))))

(deftest url-test
  (testing "simple url"
    (is (= (p/parse gp "url(http://aaa.com)")
           [:type [:url "http://aaa.com"]]))
    (is (= (p/parse gp "url('http://aaa.com')")
           [:type [:url "'http://aaa.com'"]]))))
