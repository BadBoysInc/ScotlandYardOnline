BIN=./node_modules/.bin

dist:
	@$(BIN)/browserify lib/tree.js --bare -o dist/ndpane.js
	@$(BIN)/uglifyjs dist/ndpane.js --mangle -c hoist_vars=true,if_return=true -o dist/ndpane.min.js --source-map dist/ndpane.min.map --source-map-include-sources

.PHONY: dist