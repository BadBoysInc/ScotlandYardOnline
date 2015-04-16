'use strict'
var net = require('net');
var events = require('events');
var util = require('util');


function PlayerServer() {
	var self = this;
    this.server = net.createServer(function(player) {
    	player.on('data', function(data) {
    		 self.onInput(player, data.toString());
        });
    });
}
util.inherits(PlayerServer, events.EventEmitter);


PlayerServer.prototype.onInput = function(player, data) {
	var obj = JSON.parse(data);
	 if(obj.type == 'REGISTER'){
		 this.emit('register', player, obj.student_id);
	 }else if(obj.type == 'MOVE'){
		 this.emit('move', player, obj);
	 }
}


PlayerServer.prototype.listen = function(port) {
	this.server.listen(port);
}


PlayerServer.prototype.close = function() {
    this.server.close();
}


module.exports = PlayerServer;
