# CSS-grammar

This is a simple grammar to parse CSS.

The grammar is been tested, but CSS is a very strange beast and most likely a lot of corner case are not cover as well as they should.

The grammar is been developed using instaparse.

## Usage

The API of the library expose two major use:
1. Parse a whole css file all at one
2. Parse one css rule at the time

Depending on the dimension of the file the parsing of the whole css will be slow, the bigger the file the slower the parsing.

I don't know the time complexity of instaparse, but an educate guess could eliminate a linear time complexity.

On the other side, parse one css rule at the time will produce a `lazy-seq` and will parse way smaller chuck of text at the time.

Assuming the time complexity is not linear parse one css rule at the time and finally merge all together will be faster than parse the whole file.

The grammar definition is HUGE, it will take some time to load (4.8 sec), you should do it only once.

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
