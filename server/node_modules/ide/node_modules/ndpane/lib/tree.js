var ndarray = require('ndarray');
var leaf = require('./leaf');

function inherits (ctor, superCtor) {
  ctor.super_ = superCtor;
  ctor.prototype = Object.create(superCtor.prototype, {
    constructor: {
      value: ctor,
      enumerable: false,
      writable: true,
      configurable: true
    }
  });
}

module.exports = tree;
inherits(tree, leaf);

function tree (size) {
  if (!(this instanceof tree)) return new tree(size);
  leaf.call(this, ndarray(new Uint8Array(size * size), [size, size]));
  this.size = size;
}

tree.prototype.north = function (offset) {
  var k = Math.floor(offset / this.size);
  var frag = this.data.pick(null, offset % this.size).hi(k);
  for (var i = k; i >= 0; i--) {
    var n = frag.get(i);
    if (n != offset) return n;
  }
};

tree.prototype.south = function (offset) {
  var k = Math.floor(offset / this.size);
  var frag = this.data.pick(null, offset % this.size).lo(k)
  for (var i = 0; i < this.size - k; i++) {
    var n = frag.get(i);
    if (n != offset) return n;
  }
};

tree.prototype.west = function (offset) {
  var k = offset % this.size;
  var frag = this.data.pick(Math.floor(offset/this.size), null).hi(k);
  for (var i = k; i >= 0; i--) {
    var n = frag.get(i);
    if (n != offset) return n;
  }
};

tree.prototype.east = function (offset) {
  var k = Math.abs((this.size - (offset % this.size)));
  var frag = this.data.pick(offset / this.size, null).hi(k);
  for (var i = 0; i < k; i++) {
    var n = frag.get(i);
    if (n != offset) return n;
  }
};
