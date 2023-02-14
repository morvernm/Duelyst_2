package events;


import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import game.logic.Gui;
import game.logic.Utility;
import structures.GameState;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case a tile.
 * The event returns the x (horizontal) and y (vertical) indices of the tile that was
 * clicked. Tile indices start at 1.
 * 
 * { 
 *   messageType = “tileClicked”
 *   tilex = <x index of the tile>
 *   tiley = <y index of the tile>
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class TileClicked implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		int tilex = message.get("tilex").asInt();
		int tiley = message.get("tiley").asInt();

		if (gameState.board[tilex][tiley].getOccupier() != null) {  // check if selected tile has a unit on it
			
			
			if (gameState.previousAction.isEmpty()) {
				Unit unit = gameState.board[tilex][tiley].getOccupier();
				
				gameState.validMoves = Utility.determineValidMoves(gameState.board, unit);
				Gui.highlightTiles(out, gameState.validMoves, 1);
				
				Gui.highlightTiles(out, Utility.determineTargets(gameState.board[unit.getPosition().getTilex()][unit.getPosition().getTiley()], gameState.validMoves, GameState.enemy, gameState.board), 2);
				
				GameState.previousAction.push(unit);
				
				System.err.println("Added to the Stack " + GameState.previousAction.peek());
				
				
			}
		
			
		}
		
		// check if tile is free - can only move to an empty place 
		if (gameState.board[tilex][tiley].getOccupier() == null && !gameState.previousAction.isEmpty()) {  
			
			if(gameState.validMoves.contains(gameState.board[tilex][tiley])) { // check if unit can move to selected tile
								
				Unit unit = (Unit) GameState.getPreviousAction(); //get unit from stack 
				
				System.out.println("Can move " + unit.hasMoved());
				
				gameState.board[unit.getPosition().getTilex()][unit.getPosition().getTiley()].setOccupier(null); //clear unit from tile
				
				BasicCommands.moveUnitToTile(out, unit,gameState.board[tilex][tiley]); //move unit to chosen tiles
				unit.setPositionByTile(gameState.board[tilex][tiley]); //change position of unit to new tiles
				
				gameState.board[tilex][tiley].setOccupier(unit); //set unit as occupier of tiles
				
				
				Gui.removeHighlightTiles(out, gameState.board); //clearing board 
				
			}
//			need to do y movement too
		}

	}

	
}			


