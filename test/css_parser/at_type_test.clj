(ns css-parser.at-type-test
  (:require [clojure.test :refer [deftest testing is]]
            [instaparse.core :as p]))

(def gp
  (p/parser (str "debug = at-rules\n"
                 (slurp "resources/rules.bnf") "\n" (slurp "resources/types.bnf"))))

(deftest gp-ok
  (testing "The grammar parser is been initialized."
      (is (not (nil? gp)))))

(deftest charset-rule
  (testing "charset"
    (is (= (p/parse gp "@charset 'iso-8859-15'")
           [:debug [:at-rules [:charset "@" [:vk] "charset" [:string "iso-8859-15"]]]]))
    (is (= (p/parse gp "@charset \"UTF-8\"")
           [:debug [:at-rules [:charset "@" [:vk] "charset" [:string "UTF-8"]]]]))))

(deftest import-test
  (testing "import"
    (is (= (p/parse gp "@import url('iso-8859-15'), aaa, ttge")
           [:debug [:at-rules [:import "@" [:vk] "import" [:url "'iso-8859-15'"] ", aaa, ttge"]]]))
    (is (= (p/parse gp "@import url(\"fineprint.css\") print")
           [:debug [:at-rules [:import "@" [:vk] "import" [:url "\"fineprint.css\""] " print"]]]))
    (is (= (p/parse gp "@import url(\"chrome://communicator/skin/\")")
           [:debug [:at-rules [:import "@" [:vk] "import" [:url "\"chrome://communicator/skin/\""]]]]))
    (is (= (p/parse gp "@import url('landscape.css') screen and (orientation:landscape)")
           [:debug [:at-rules [:import "@" [:vk] "import" [:url "'landscape.css'"] " screen and (orientation:landscape)"]]]))))

(deftest namespace-test
  (testing "namespace"
    (is (= (p/parse gp "@namespace svg url(XML-namespace-URL)")
           [:debug [:at-rules [:namespace "@" [:vk] "namespace" "svg" [:url "XML-namespace-URL"]]]]))
    (is (= (p/parse gp "@namespace svg url(\"aaaa\")")
           [:debug [:at-rules [:namespace "@" [:vk] "namespace" "svg" [:url "\"aaaa\""]]]]))))

(deftest media-test
  (testing "media key"
    (is (= (p/parse gp "@media screen { body { font-size: 13px }}")
           [:debug [:at-rules [:media "@" [:vk] "media" "screen" [:rule [:selector [:primitive-selector [:html-element "body"]]] [:declarations [:token [:font-size "font-size" [:font-size-value [:length [:integer "13"] "px"]]]]]]]]]))
    (is (= (p/parse gp "@media screen, print { body { font-size: 13px; color: red } body { line-height: 1.2 }}")
           [:debug [:at-rules [:media "@" [:vk] "media" "screen" "print" [:rule [:selector [:primitive-selector [:html-element "body"]]] [:declarations [:token [:font-size "font-size" [:font-size-value [:length [:integer "13"] "px"]]]] [:token [:color "color" [:color-value [:color-type [:color-keyword "red"]]]]]]] [:rule [:selector [:primitive-selector [:html-element "body"]]] [:declarations [:token [:line-height "line-height" [:line-height-value [:float "1" "." "2"]]]]]]]]]))))

(deftest page-test
  (testing "at page"
    (is (= (p/parse gp "@page :first { margin:2in; }")
           [:debug [:at-rules [:page "@" [:vk] "page" ":first" [:declarations [:token [:margin "margin" [:margin-all [:length [:integer "2"] "in"]]]]]]]]))
    (is (= (p/parse gp "@page :first { margin:2in; background: red;}")
           [:debug [:at-rules [:page "@" [:vk] "page" ":first" [:declarations [:token [:margin "margin" [:margin-all [:length [:integer "2"] "in"]]]] [:token [:background "background" [:background-color-value [:color-type [:color-keyword "red"]]]]]]]]]))))

(deftest font-face-test
  (testing "font-face"
    (is (= (p/parse gp "@font-face {
      font-family: \"Bitstream Vera Serif Bold\";
      src: url(\"https://mdn.mozillademos.org/files/2468/VeraSeBd.ttf\");}")
           [:debug [:at-rules [:font-face "@" [:vk] "font-face" "{" [:declarations [:token [:font-family "font-family" [:family-font-name [:string "Bitstream Vera Serif Bold"]]]] [:token [:src "src" [:src-value [:url "\"https://mdn.mozillademos.org/files/2468/VeraSeBd.ttf\""]]]]] "}"]]]))
    (is (= (p/parse gp "@font-face { font-family: MyHelvetica; src: local(\"Helvetica Neue Bold\"), local(\"HelveticaNeue-Bold\"), url(MgOpenModernaBold.ttf); font-weight: bold;}")
           [:debug [:at-rules [:font-face "@" [:vk] "font-face" "{" [:declarations [:token [:font-family "font-family" [:family-font-name [:custom-ident "M" "yHelvetica" ""]]]] [:token [:src "src" [:src-value "local(" [:string "Helvetica Neue Bold"] ")" "local(" [:string "HelveticaNeue-Bold"] ")" [:url "MgOpenModernaBold.ttf"]]]] [:token [:font-weight "font-weight" [:font-weight-value "bold"]]]] "}"]]]))))

(deftest keyframes-test
  (testing "keyframes"
    (is (= (p/parse gp "@keyframes identifier { 0% { top: 0; left: 0; }
100% { top: 100px; left: 100%; }}")
           [:debug [:at-rules [:keyframes "@" [:vk] "keyframes" [:identifier [:custom-ident "i" "dentifier" ""]] "{" [:percentage [:integer "0"] "%"] "{" [:declarations [:token [:top "top" [:top-value [:length "0"]]]] [:token [:left "left" [:left-value [:length "0"]]]]] "}" [:percentage [:integer "100"] "%"] "{" [:declarations [:token [:top "top" [:top-value [:length [:integer "100"] "px"]]]] [:token [:left "left" [:left-value [:percentage [:integer "100"] "%"]]]]] "}" "}"]]]))
    (is (= (p/parse gp "@keyframes important1 { from { margin-top: 50px; } 50%  { margin-top: 150px !important; } to   { margin-top: 100px; }}")
           [:debug [:at-rules [:keyframes "@" [:vk] "keyframes" [:identifier [:custom-ident "i" "mportant" "1"]] "{" "from" "{" [:declarations [:token [:margin-top "margin-top" [:margin-top-value [:length [:integer "50"] "px"]]]]] "}" [:percentage [:integer "50"] "%"] "{" [:declarations [:token [:margin-top "margin-top" [:margin-top-value [:length [:integer "150"] "px"]]]] "!important"] "}" "to" "{" [:declarations [:token [:margin-top "margin-top" [:margin-top-value [:length [:integer "100"] "px"]]]]] "}" "}"]]]))))


(deftest supports-test
  (testing "support condition"
    (is (= (p/parse gp "@supports ( transform-origin: 5% 5% ) { background: red}")
           [:debug [:at-rules [:supports "@" [:vk] "supports" [:support-condition "(" " transform-origin" ":" " 5% 5% " ")"] "{" [:declarations [:token [:background "background" [:background-color-value [:color-type [:color-keyword "red"]]]]]] "}"]]]))
    (is (= (p/parse gp "@supports (background:red) and ( transform-origin: 5% 5% ) { background: red}")
           [:debug [:at-rules [:supports "@" [:vk] "supports" [:support-condition "(" "background" ":" "red" ")" "and" "(" " transform-origin" ":" " 5% 5% " ")"] "{" [:declarations [:token [:background "background" [:background-color-value [:color-type [:color-keyword "red"]]]]]] "}"]]]))))


(deftest document-test
  (testing "document"
    (is (= (p/parse gp "@document url(http://www.w3.org/), url-prefix(http://www.w3.org/Style/), domain(mozilla.org), regexp(\"https:.*\") { body { color: purple; background: yellow; } }")
           [:debug [:at-rules [:document "@" [:vk] "document" "url(http://www.w3.org/), url-prefix(http://www.w3.org/Style/), domain(mozilla.org), regexp(\"https:.*\") " "{" [:rule [:selector [:primitive-selector [:html-element "body"]]] [:declarations [:token [:color "color" [:color-value [:color-type [:color-keyword "purple"]]]]] [:token [:background "background" [:background-color-value [:color-type [:color-keyword "yellow"]]]]]]] "}"]]]))))
