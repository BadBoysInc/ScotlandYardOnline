var test = require('tape');
var ndpane = require('..');
var unpack = require('ndarray-unpack');

test('split vertically', function (t) {
  t.plan(1);
  var pane = ndpane(4);
  pane.split(true);
  t.deepEqual(unpack(pane.data), 
    [
      [0, 0, 2, 2],
      [0, 0, 2, 2],
      [0, 0, 2, 2],
      [0, 0, 2, 2]
    ]
  );
});
