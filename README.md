# CSS-grammar

This is a simple grammar to parse CSS.

The grammar is been tested, but CSS is a very strange beast and most likely a lot of corner case are not cover as well as they should.

The grammar is been developed using instaparse.

## Install


### A little caution about dependencies

In order to make everything as simple as possible we are going to use directly instaparse.

The API of instaparse is extremely simple and flexible, so I didn't find necessary to write a wrap around it.

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

The API of the library expose two major use:
1. Parse a whole css file all at one
2. Parse one css rule at the time

Depending on the dimension of the file the parsing of the whole css will be slow, the bigger the file the slower the parsing.

I don't know the time complexity of instaparse, but an educate guess could eliminate a linear time complexity.

On the other side, parse one css rule at the time will produce a `lazy-seq` and will parse way smaller chuck of text at the time.

Assuming the time complexity is not linear parse one css rule at the time and finally merge all together will be faster than parse the whole file.

The grammar definition is HUGE, it will take some time to load (4.8 sec), you should do it only once.

### Example

To parse a single, small file all in one passage you can simply do like this:

``` clojure
(ns your-name-space
  (:require [css-parser.core :as css]
   	        [instaparse.core :as p]))

(def gp-full-file (css/css-parser-full-file)) ;; this take ~5sec to complete

(p/parse gp-full-file (slurp "your-small-css-file.css"))
```

If your file is bigger, even if your file is pretty small probably this is the best way anyway, you can use the library as so:

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

If you return a lazy-seq from the expresion above (using `map` instead of `mapv` for example) is quite likely that the whole sequence is not yet be evaluated, but the file is already been closed, this will result in an exception.

If you still want to use a lazy-seq you need to use it inside the scope of `with-open`, or to manually open and close the file.

Also consider that `split-char-seq-in-rules` itself returns a lazy-seq, so the same consideration of above apply.

The output is something similar to this one:

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

I invite you to check the test to see more messy and complex rules.

## Maturity

The library does parse css files, however the resulting tree is very ugly and not always coherent.

The output tree does reflect the grammar internal mechanism, so it is all but pretty, however I find it quite manageble.

I tried to include as many rules as possible, however I may have miss something, if you find anything that is not parsed please open a issue or submit a pull request.

## Next steps

The obvious next step is to make the resulting tree more human.

However the necessity to make a human usable tree should be balanced with the future plans to generate css files from the tree itself, a compiler to css.

Actually it could be possible to generate two different trees, one for the computer, one for the humans, not really sure if it is a better idea than to keep a balaced tree.

## License

Copyright © 2015 Simone Mosciatti

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
