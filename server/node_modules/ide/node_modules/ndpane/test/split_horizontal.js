var test = require('tape');
var ndpane = require('..');
var unpack = require('ndarray-unpack');

test('split horizontally', function (t) {
  t.plan(1);
  var pane = ndpane(4);
  pane.split();
  t.deepEqual(unpack(pane.data), 
    [
      [0, 0, 0, 0],
      [0, 0, 0, 0],
      [8, 8, 8, 8],
      [8, 8, 8, 8]
    ]
  );
});
