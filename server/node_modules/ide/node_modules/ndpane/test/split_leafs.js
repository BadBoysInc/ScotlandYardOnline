var test = require('tape');
var ndpane = require('..');
var unpack = require('ndarray-unpack');

test('split leafs', function (t) {
  t.plan(4);
  var pane = ndpane(4);
  pane.split(true);
  pane.leafs[0].split();
  t.deepEqual(unpack(pane.data),
    [
      [0, 0, 2, 2],
      [0, 0, 2, 2],
      [8, 8, 2, 2],
      [8, 8, 2, 2]
    ]
  );
  pane.leafs[1].split(true);
  t.deepEqual(unpack(pane.data), 
    [
      [0, 0, 2, 3],
      [0, 0, 2, 3],
      [8, 8, 2, 3],
      [8, 8, 2, 3]
    ]
  );
  pane.leafs[1].leafs[0].split();
  t.deepEqual(unpack(pane.data), 
    [
      [0, 0,  2, 3],
      [0, 0,  2, 3],
      [8, 8, 10, 3],
      [8, 8, 10, 3]
    ]
  );
  pane.leafs[0].leafs[1].split();
  t.deepEqual(unpack(pane.data), 
    [
      [0,   0,  2, 3],
      [0,   0,  2, 3],
      [8,   8, 10, 3],
      [12, 12, 10, 3]
    ]
  );
});
