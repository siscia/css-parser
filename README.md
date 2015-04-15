# CSS-grammar

This is a simple grammar to parse CSS type.

The grammar is been tested, but CSS is a very strange beast and most likely a lot of corner case are not cover as well as they should.

The grammar is been developed using instaparse.

The next obvious step is to parse not only the CSS type, but the CSS itself.

## Usage

The use is extremelly intuitive, just load the grammar and parse any CSS type.

It is not extremelly usefull, but it is a good start.

``` clojure
user> (require '[instaparse.core :as g])
nil
user> (def parser (g/parser "grammar.bnf"))
#'user/parser
user> (g/parse parser "32px")
[:type [:length [:integer "32"] "px"]]
```

## License

Copyright © 2015 Simone Mosciatti

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
