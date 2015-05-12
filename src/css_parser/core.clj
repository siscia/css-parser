(ns html-generator.core
  (:require [instaparse.core :as p]))

(defn css-parser []
  (p/parser (str (slurp "rules.bnf")
                 "\n"
                 (slurp "types.bnf"))))

(defn parse-css
  ([parser file]
     (p/parse parser file))
  ([parser]
     (partial parse-css parser))
  ([]
     (partial parse-css (css-parser))))

(defn split-file-in-rules [sequence]
  (loop [s sequence
         counter 0
         buffer (transient [])]
    (if (= -1 counter) ;; 
      (cons (apply str (persistent! buffer))
            (lazy-seq (split-block-lazy s)))
      (if (not (seq s)) ;; if the sequence is terminate we return the buffer and nil to indicate the end
        (cons (apply str (persistent! buffer)) nil)
        (let [actual (first s)]
          (case counter
            0 (recur (drop 1 s) ;; "normal" state
                     (if (= \{ actual)
                       1 0)
                     (conj! buffer actual))
            -1 (recur (drop 1 s) ;; sequence to cut here
                      0
                      (transient []))
            1 (recur (drop 1 s) ;; possible next cut
                     (case actual
                       \{ (inc counter)
                       \} -1
                       counter)
                     (conj! buffer actual))
            (recur (drop 1 s)   ;; most generic state
                   (case actual
                     \{ (inc counter)
                     \} (dec counter)
                     counter)
                   (conj! buffer actual))))))))

