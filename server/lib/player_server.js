'use strict'
var net = require('net');
var events = require('events');
var util = require('util');


function PlayerServer() {
    this.server = net.createServer(function(player) {
        //TODO
    });
}
util.inherits(PlayerServer, events.EventEmitter);


PlayerServer.prototype.onInput = function(player, data) {
    //TODO
}


PlayerServer.prototype.listen = function(port) {
    //TODO
}


PlayerServer.prototype.close = function() {
    //TODO
}


module.exports = PlayerServer;