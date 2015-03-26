'use strict'
var PlayerServer = require('./player_server.js');
var GameServer   = require('./game_server.js');

/**
 * Constructor for the class. Needs to initialise the players server
 * and game server, create the list of colours and initialise a game id
 * @constructor
 */
function Server() {
    this.game_id = -1;
    this.colours = [];

    //TODO
}

/**
 * Function that will start up the server. It should set the game_server
 * and the player_server to listen on the correct ports. It should also
 * connect up the events emitted by the player server so that the correct
 * information is passed onto the game server
 * @param player_port
 * @param game_port
 */
Server.prototype.start = function(player_port, game_port) {
    //TODO
}


/**
 * Function to close down the player server and the game server
 */
Server.prototype.close = function() {
    //TODO
}


/**
 * Function to retrieve the id of the game being played
 * @returns {number|*}
 */
Server.prototype.gameId = function() {
    //TODO
}


/**
 * Function to extract the next colour out of the arrays of colours
 * When a colour is extracted, that colour is removed from the list
 * @returns {*}
 */
Server.prototype.getNextColour = function() {
    //TODO
}


module.exports = Server;

