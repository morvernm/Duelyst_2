package structures;

import java.awt.datatransfer.Clipboard;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import structures.basic.Player;
import structures.basic.Position;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.SpellCard;
import structures.basic.Card;
import structures.basic.Playable;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState {

	public static Player currentPlayer; // store who's round it currently is
	public static Player enemyPlayer;

	public static Player humanPlayer;

	private static int status;
	private static final int PreviousActionEmpty = 0;
	private static final int PreviousActionContainsUnit = 1;
	private static final int PreviousActionContainsUnitCard = 2;
	private static final int PreviousActionContainsSpellCard = 3;
	
	public static Player enemy;

	public boolean gameInitalised = false;
	public boolean something = false;
	
//	storing validMoves and valid Attacks
	public Set<Tile> validMoves = new HashSet<>();
	public Set<Tile> validAttacks = new HashSet<>();
	
//	stack of actions taken by the player
	public static Stack<Playable> previousAction = new Stack<Playable>();	
	
	public static Tile[][] board = new Tile[9][5];
	
	public static Object getPreviousAction() {
		return previousAction.pop();
	}	
	
	public static void setPreviousAction(Playable action) {
		previousAction.push(action);
		System.out.println(action.type);
		if (action instanceof Unit){
			status = PreviousActionContainsUnit;
			return;
		}
		if (action instanceof SpellCard){
			status = PreviousActionContainsSpellCard;
			return;
		}
		if (action instanceof Card){
			status = PreviousActionContainsUnitCard;
			return;
		}
		return;
	}

	public static void emptyPreviousAction(){
		previousAction.clear();
		status = PreviousActionEmpty;
		System.out.println("emptyprev");
	}

	public static int getStatus(){
		return status;
	}

	public static Playable peekPreviousAction(){
		return previousAction.peek();
	}
	
	public static Player getCurrentPlayer() {
		return currentPlayer;
	}
	public void setCurrentPlayer(Player player) {
		this.currentPlayer = player;
	}

	public void setHumanPlayer(Player player) {
		humanPlayer = player;
	}

	public static Player getHumanPlayer() {
		return humanPlayer;
	}

	public static Player getEnemyPlayer() {
		return enemyPlayer;
	}

}

