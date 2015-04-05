var test = require('tape');
var ndpane = require('..');
var unpack = require('ndarray-unpack');

test('merge', function (t) {
  t.plan(3);
  var pane = ndpane(4);
  pane.split(true);
  pane.leafs[1].split().leafs[0].split(true).merge();
  pane.leafs[1].leafs[0].merge();
  t.deepEqual(unpack(pane.data), 
    [
      [0, 0,  2,  2],
      [0, 0,  2,  2],
      [0, 0, 10, 10],
      [0, 0, 10, 10]
    ]
  );
  pane.leafs[1].merge();
  t.deepEqual(unpack(pane.data), 
    [
      [0, 0, 2, 2],
      [0, 0, 2, 2],
      [0, 0, 2, 2],
      [0, 0, 2, 2]
    ]
  );
  pane.merge();
  t.deepEqual(unpack(pane.data), 
    [
      [0, 0, 0, 0],
      [0, 0, 0, 0],
      [0, 0, 0, 0],
      [0, 0, 0, 0]
    ]
  );
});
