var test = require('tape');
var ndpane = require('..');

test('get nearest on x axis', function (t) {
  t.plan(12);
  var pane = ndpane(4);
  pane.split();
  var p = pane.leafs[0].split(true);
  p.leafs[0].split(true);
  p.leafs[1].split(true);
  pane.leafs[1].split(true);
  // [ 
  //   [ 0,  1,  2,  3 ],
  //   [ 0,  1,  2,  3 ],
  //   [ 8,  8, 10, 10 ],
  //   [ 8,  8, 10, 10 ] 
  // ]
  t.equal(pane.east(0), 1);
  t.equal(pane.east(1), 2);
  t.equal(pane.east(2), 3);
  t.equal(pane.east(3), undefined);
  t.equal(pane.east(8), 10);
  t.equal(pane.east(10), undefined);
  t.equal(pane.west(10), 8);
  t.equal(pane.west(8), undefined);
  t.equal(pane.west(3), 2);
  t.equal(pane.west(2), 1);
  t.equal(pane.west(1), 0);
  t.equal(pane.west(0), undefined);
});