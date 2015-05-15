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

(deftest import-test
  (testing "import"
    (is (= (p/parse gp "@import url('iso-8859-15'), aaa, ttge")
           [:debug [:at-rules [:import "@import " [:url "'iso-8859-15'"] ", aaa, ttge"]]]))
    (is (= (p/parse gp "@import url(\"fineprint.css\") print")
           [:debug [:at-rules [:import "@import " [:url "\"fineprint.css\""] " print"]]]))
    (is (= (p/parse gp "@import url(\"chrome://communicator/skin/\")")
           [:debug [:at-rules [:import "@import " [:url "\"chrome://communicator/skin/\""]]]]))
    (is (= (p/parse gp "@import url('landscape.css') screen and (orientation:landscape)")
           [:debug [:at-rules [:import "@import " [:url "'landscape.css'"] " screen and (orientation:landscape)"]]]))))

(deftest namespace-test
  (testing "namespace"
    (is (= (p/parse gp "@namespace prefix url(XML-namespace-URL)")
           [:debug [:at-rules [:namespace "@namespace " "prefix" [:url "XML-namespace-URL"]]]]))
    (is (= (p/parse gp "@namespace svg url(XML-namespace-URL)")
           [:debug [:at-rules [:namespace "@namespace " "svg" [:url "XML-namespace-URL"]]]]))
    (is (= (p/parse gp "@namespace svg url(\"aaaa\")")
           [:debug [:at-rules [:namespace "@namespace " "svg" [:url "\"aaaa\""]]]]))))

(deftest media-test
  (testing "media key"
    (is (= (p/parse gp "@media screen { body { font-size: 13px }}")
[:debug [:at-rules [:media "@media " "screen" [:rule [:selector [:relationship-selector [:nested [:primitive-selector [:html-element "body"]]]]] [:token [:font-size "font-size" [:font-size-value [:length [:integer "13"] "px"]]]]]]]]))
    (is (= (p/parse gp "@media screen,print {
                               body { font-size: 13px;
                                      color: red }
                               body { line-height: 1.2 }}")
           [:debug [:at-rules [:media "@media " "screen" "print" [:rule [:selector [:relationship-selector [:nested [:primitive-selector [:html-element "body"]]]]] [:token [:font-size "font-size" [:font-size-value [:length [:integer "13"] "px"]]]] [:token [:color "color" [:color-value [:color-type [:color-keyword "red"]]]]]] [:rule [:selector [:relationship-selector [:nested [:primitive-selector [:html-element "body"]]]]] [:token [:line-height "line-height" [:line-height-value [:float "1" "." "2"]]]]]]]]))))

(deftest page-test
  (testing "at page"
    (is (= (p/parse gp "@page :first { margin:2in; }")
           [:debug [:at-rules [:page "@page " ":first" [:token [:margin "margin" [:margin-all [:length [:integer "2"] "in"]]]]]]]))
    (is (= (p/parse gp "@page :first { margin:2in;
                                       background: red;}")
           [:debug [:at-rules [:page "@page " ":first" [:token [:margin "margin" [:margin-all [:length [:integer "2"] "in"]]]] [:token [:background "background" [:background-color-value [:color-type [:color-keyword "red"]]]]]]]]))))
