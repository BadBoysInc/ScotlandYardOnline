# ndpane

represents a splitpane.


# example

```js
var ndpane = require('ndpane');
var unpack = require('ndarray-unpack');

var pane = ndpane(4); // 16 panes
pane.split(); // split horizontally
pane.leafs[1].split(true); // split bottom half vertically

console.log(unpack(pane.data));
// [
//  [1, 1,  1,  1],
//  [1, 1,  1,  1],
//  [9, 9, 11, 11],
//  [9, 9, 11, 11]
// ]
```


# api

## ndpane(size)

## .split(vertical)

## .merge()

## .flip()

## .north(offset)

## .south(offset)

## .west(offset)

## .east(offset)


# license

MIT
