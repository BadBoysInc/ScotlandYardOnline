var test = require('tape');
var ndpane = require('..');

test('get nearest on y axis', function (t) {
  t.plan(12);
  var pane = ndpane(4);
  pane.split(true);
  var p = pane.leafs[0].split();
  p.leafs[0].split();
  p.leafs[1].split();
  pane.leafs[1].split();
  // [ 
  //   [ 0,   0,  2,  2 ],
  //   [ 4,   4,  2,  2 ],
  //   [ 8,   8, 10, 10 ],
  //   [ 12, 12, 10, 10 ] 
  // ]
  t.equal(pane.north(0), undefined);
  t.equal(pane.north(4), 0);
  t.equal(pane.north(8), 4);
  t.equal(pane.north(12), 8);
  t.equal(pane.north(2), undefined);
  t.equal(pane.north(10), 2);
  t.equal(pane.south(0), 4);
  t.equal(pane.south(4), 8);
  t.equal(pane.south(8), 12);
  t.equal(pane.south(12), undefined);
  t.equal(pane.south(2), 10);
  t.equal(pane.south(10), undefined);
});