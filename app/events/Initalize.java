package events;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import demo.CheckMoveLogic;
import demo.CommandDemo;
import game.logic.Gui;
import play.shaded.ahc.io.netty.util.internal.SystemPropertyUtil;
import play.twirl.api.TemplateMagic;
import structures.GameState;
import structures.basic.AIPlayer;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import structures.basic.SpecialUnits.Pureblade;
import structures.basic.SpecialUnits.Windshrike;

import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import commands.BasicCommands;
import game.logic.Utility;
import game.logic.Gui;

/*
 * Indicates that both the core game loop in the browser is starting, meaning
 * that it is ready to recieve commands from the back-end.
 * 
 * { 
 *   messageType = “initalize”
 * }
 * 
 * @author Dr. Richard McCreadie
 * 
 */

public class Initalize implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		
		Player humanPlayer = createHumanPlayer(out);

		GameState.setTotalUnits(); 

		gameState.setHumanPlayer(humanPlayer);
		gameState.setCurrentPlayer(humanPlayer);
		gameState.gameInitalised = true;
		gameState.emptyPreviousAction();

		
		gameState.something = true;
		
		/*
		 *  Create the board of Tile Objects
		 */
		for (int i = 0; i < gameState.board.length; i++) {
			for(int j = 0; j < gameState.board[0].length; j++) {
				gameState.board[i][j] = BasicObjectBuilders.loadTile(i, j);
				BasicCommands.drawTile(out, gameState.board[i][j], 0);
				try {Thread.sleep(10);} catch (InterruptedException e) {e.printStackTrace();}
			}
		}
		
		/*
		 * Place the Human Avatar on the board
		 */
		Unit unit = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, -1, Unit.class);
		unit.setPositionByTile(gameState.board[3][2]); 
		gameState.board[3][2].setOccupier(unit);
		BasicCommands.drawUnit(out, unit, gameState.board[3][2]);

		GameState.getHumanPlayer().setUnit(unit);
		GameState.getHumanPlayer().createAvatar(unit);
		for (Unit u : GameState.getHumanPlayer().getUnits()){
			System.out.printf("Unit x %d and y  %d \n", unit.getPosition().getTilex(), unit.getPosition().getTiley());
		}

		GameState.modifiyTotalUnits(1);
		
		Gui.setUnitStats(unit, gameState.getHumanPlayer().getHealth(), 2);
		unit.setHealth(gameState.getHumanPlayer().getHealth());
		unit.setAttack(2);
		
		
		/*
		 * TEST
		 */
//		
//		Windshrike unitTwo = (Windshrike)BasicObjectBuilders.loadUnit(StaticConfFiles.u_windshrike, 6, Windshrike.class);
//		unitTwo.setPositionByTile(gameState.board[1][2]); 
//		
//		gameState.board[1][2].setOccupier(unitTwo);
//		BasicCommands.drawUnit(out, unitTwo, gameState.board[1][2]);
//		gameState.getHumanPlayer().setUnit(unitTwo);
//		
//		Gui.setUnitStats(unitTwo, 4, 3);
//		
//		unitTwo.setHealth(4);
//		unitTwo.setAttack(3);
//		
//		GameState.modifiyTotalUnits(1);
//		
		/*
		 * Enemy avatar stuff
		 */

		/*
		 *  ISSUE 27, Airdrop testing
		 */

		Card ironcliff = BasicObjectBuilders.loadCard(StaticConfFiles.c_ironcliff_guardian, 6, Card.class);
		ironcliff.setManacost(1);
		GameState.getHumanPlayer().testcard(ironcliff);

		
		GameState.enemy = new AIPlayer();

		
		Unit enemyUnit = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, -2, Unit.class);

		
		enemyUnit.setPositionByTile(gameState.board[5][2]); 
		gameState.board[5][2].setOccupier(enemyUnit);
		
		BasicCommands.drawUnit(out, enemyUnit, gameState.board[5][2]);
		
		Gui.setUnitStats(enemyUnit, 20, 2);
		
		enemyUnit.setHealth(20);
		enemyUnit.setAttack(2);
		GameState.modifiyTotalUnits(1);

		GameState.enemy.setUnit(enemyUnit);

		try {Thread.sleep(100);}catch (InterruptedException e){e.printStackTrace();}
		
		unit.setHealth(humanPlayer.getHealth());
		enemyUnit.setHealth(GameState.enemy.getHealth());
		GameState.enemy.createAvatar(enemyUnit);

		/*
		 * ISSUE 17, SpellThief testing
		 */

		Pureblade pureblade = (Pureblade)BasicObjectBuilders.loadUnit(StaticConfFiles.u_pureblade_enforcer, 1, Pureblade.class);
		pureblade.setPositionByTile(GameState.board[5][1]); 
		GameState.board[5][1].setOccupier(pureblade);
		
		BasicCommands.drawUnit(out, pureblade, GameState.board[5][1]);
		
		Gui.setUnitStats(pureblade, 1, 2);
		
		pureblade.setHealth(1);
		pureblade.setAttack(2);
		GameState.modifiyTotalUnits(1);

		GameState.enemy.setUnit(pureblade);

		/*
		 * Windshrike testing
		 */

		Windshrike windshrike = (Windshrike)BasicObjectBuilders.loadUnit(StaticConfFiles.u_windshrike, 34, Windshrike.class);
		windshrike.setPositionByTile(GameState.board[5][4]); 
		GameState.board[5][4].setOccupier(windshrike);
		
		BasicCommands.drawUnit(out, windshrike, GameState.board[5][4]);
		
		Gui.setUnitStats(windshrike, 1, 2);
		
		windshrike.setHealth(1);
		windshrike.setAttack(2);
		GameState.modifiyTotalUnits(1);

		GameState.enemy.setUnit(windshrike);

		/*
		Azure hearld testing 
		*/

		Card azure = BasicObjectBuilders.loadCard(StaticConfFiles.c_azure_herald, 5, Card.class);
		GameState.getHumanPlayer().testcard(azure);

	}
	
	
	/*
	 * Create the Human Player
	 */
	
	public Player createHumanPlayer(ActorRef out) {
		Player player1 = new Player();
		for (int i = 0; i < 3; i++) {
			player1.drawCard();
		}

		return player1;
	}
	
	
}


