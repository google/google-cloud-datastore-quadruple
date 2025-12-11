#!/bin/sh -e
(cd cpp/quadruple-converter; make test)
(cd java/quadruple-converter; mvn test)
(cd python; python3 quadruple_builder_test.py)
(cd typescript; npx tsc && npm test)
