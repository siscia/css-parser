(ns css-parser.core
  (:require [instaparse.core :as p]))

(defn css-parser-full-file []
  (p/parser (str "file = (rule | sh+ | at-rules)+ \n"
                 (slurp "rules.bnf")
                 "\n"
                 (slurp "types.bnf"))))

(defn css-parser-single-rule []
  (p/parser (str "single-rule = sh? (rule | at-rules) \n"
                 (slurp "rules.bnf")
                 "\n"
                 (slurp "types.bnf"))))

(defn eliminate-comment [string]
  (let [re (re-pattern #"\*\/(?s)(.*)\*/")]
    (clojure.string/replace string re "")))

(defn eliminate-blank [string]
  (clojure.string/replace string #"(\{|;)(\s)" "$1"))

(defn- merge-lines [seq-of-lines]
  (concat (first seq-of-lines)
          (lazy-seq
           (merge-lines (rest seq-of-lines)))))

(defn file-reader->char-seq [f]
  (let [lines (line-seq f)]
    (merge-lines lines)))

(defn split-char-seq-in-rules [sequence]
  (loop [s sequence
         counter 0
         buffer (transient [])]
    (if (= -1 counter) ;;
      (cons (apply str (persistent! buffer))
            (lazy-seq (split-char-seq-in-rules s)))
      (if (not (seq s)) ;; if the sequence is terminate we return the buffer and nil to indicate the end
        (cons (apply str (persistent! buffer)) nil)
        (let [actual (first s)]
          (case counter
            1 (recur (rest s) ;; possible next cut
                     (case actual
                       \{ (inc counter)
                       \} -1
                       counter)
                     (conj! buffer actual))
            0 (recur (rest s) ;; "normal" state
                     (if (= \{ actual)
                       1 0)
                     (conj! buffer actual))
            -1 (recur (rest s) ;; sequence to cut here
                      0
                      (transient []))
            (recur (rest s)   ;; most generic state
                   (case actual
                     \{ (inc counter)
                     \} (dec counter)
                     counter)
                   (conj! buffer actual))))))))
