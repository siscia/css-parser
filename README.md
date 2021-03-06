# CSS-grammar

This is a simple grammar to parse CSS.

The grammar has been tested, but CSS is a very strange beast and most likely a lot of corner cases are not covered as well as they should be.

The grammar was developed using instaparse.

## Install

[![Clojars Project](http://clojars.org/css-parser/latest-version.svg)](http://clojars.org/css-parser)

### A little caution about dependencies

In order to make everything as simple as possible we are going to use instaparse directly.

The instaparse API is extremely simple and flexible, so I didn't find it necessary to write a wrapper around it.

This, however, means that you need to import instaparse itself.

This library does require instaparse as dependency, but you should required it too in your project.

In order to make everything as clean as possible, I believe that the best way is to write your `:dependencies` is:

``` clojure
:dependencies [ ...
	           [css-parser "x.x.x" :exclusions [instaparse]]
               [instaparse "x.x.x"]
			   ... ]
```

## Usage

The API of the library exposes two major uses:

1. Parse a whole CSS file at once

2. Parse one CSS rule at a time

Depending on the size of the file, parsing all of the CSS may be slow. The bigger the file, the slower the parsing.

I don't know the time complexity of instaparse, but an educated guess would probably eliminate a linear time complexity.

On the other hand, parsing one CSS rule at the time will produce a `lazy-seq` and will parse a way smaller chunk of text at a time.

Assuming the time complexity is not linear, parsing one CSS rule at a time and finally merging them all together will be faster than parsing the whole file.

The grammar definition is HUGE. It will take some time to load (4.8 sec). You should do it only once.

### Example

To parse a single, small file all in one pass, you can simply do this:

``` clojure
(ns your-name-space
  (:require [css-parser.core :as css]
   	        [instaparse.core :as p]))

(def gp-full-file (css/css-parser-full-file)) ;; this takes ~5sec to complete

(p/parse gp-full-file (slurp "your-small-css-file.css"))
```

If your file is bigger, even if your file is pretty small probably this is the best way anyway, you can use the library like so:

``` clojure
(ns your-name-space
  (:require [css-parser.core :as css]
   	        [instaparse.core :as p]))

(def gp-single-rule (css/css-parser-single-rule)) ;; please note that the function is different

(with-open [fl (clojure.java.io/reader "path-to-your-file.css")]
  (let [rules (-> fl file-reader->char-seq split-char-seq-in-rules)]
    ;; here rules is a lazy sequence of strings.
	(mapv #(p/parse gp-single-rule %) rules)))
```

It is very important to consider the scope of `with-open`.

If you return a `lazy-seq` from the expression above (using `map` instead of `mapv` for example) is quite likely that the whole sequence is not yet evaluated, but the file has already been closed. This will result in an exception.

If you still want to use a `lazy-seq`, you need to use it inside the scope of `with-open` or manually open and close the file.

Also consider that `split-char-seq-in-rules` itself returns a `lazy-seq`, so the same consideration above applies.

The output is something similar to this:

``` clojure
(pprint (p/parse gp "#example {background: red}"))
[:single-rule
 [:rule
  [:selector [:primitive-selector [:id "example"]]]
  [:declarations
   [:token
    [:background
     "background"
     [:background-color-value [:color-type [:color-keyword "red"]]]]]]]]
```

I invite you to check out the tests to see messier and more complex rules.

#### Comments and white space

In `css-parse.core` there are also two functions that eliminate comments (`eliminate-comment`) and blank characters (`eliminate-blank`) from a string.

The parser does not recognize comments, so you need to eliminate them.

The reason to eliminate blank characters is to speed up the parsing process. The speed up is substantial if a lot of spaces instead of tabs are used in the CSS.

## Maturity

The library does parse CSS files, however the resulting tree is very ugly and not always coherent.

The output tree reflects the grammar's internal mechanism, so it is all but pretty, however I find it quite manageable.

I tried to include as many rules as possible, however I may have missed something. If you find anything that is not parsed, please open an issue or submit a pull request.

## Next steps

The obvious next step is to make the resulting tree more human.

However the necessity to make a human-usable tree should be balanced with the future plans to generate CSS files from the tree itself, a compiler to CSS.

Actually it could be possible to generate two different trees, one for the computer, one for the humans. I'm not really sure if that is a better idea than to keep a balanced tree.

## License

Copyright © 2015 Simone Mosciatti

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
