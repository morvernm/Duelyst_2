package game.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

import akka.actor.ActorRef;
import commands.BasicCommands;
import events.CardClicked;
import structures.GameState;

import structures.basic.Player;
import structures.basic.SpecialUnits.Provoke;
import structures.basic.SpecialUnits.SilverguardKnight;
import structures.basic.SpecialUnits.Windshrike;
import structures.basic.Tile;
import structures.basic.Unit;

import structures.basic.UnitAnimationType;

import structures.basic.BigCard;
import structures.basic.Card;
import structures.basic.UnitAnimationSet;
import structures.basic.EffectAnimation;
import structures.basic.Playable;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

public class Utility {
	
    /*
     * This class is the utility class where methods with some main logic of the game will be provided
     *
     */
    private static ActorRef out;

    public Utility(ActorRef out) {
        Utility.out = out;
    }


    public static Set<Tile> determineTargets(Tile tile, Set<Tile> positions, Player enemy, Tile[][] board) {

        // Using Set so that the Tile Objects do not repeat for the last condition
        Set<Tile> validAttacks = new HashSet<>();

        // Has Attacked already
        if (tile.getOccupier().hasAttacked()) {
            return null;
            // Has moved but has not attacked - consider only the current position
        } else if (tile.getOccupier().hasMoved() && !tile.getOccupier().hasAttacked()) {
        	validAttacks = getValidTargets(tile, enemy, board);
        	
            // Has not moved nor attacked - consider all possible movements as well.
        } else if (!tile.getOccupier().hasMoved() && !tile.getOccupier().hasAttacked()) {
            System.out.println("has NOT moved NOR attacked");

            for (Tile position : positions) {
                validAttacks.addAll(getValidTargets(position, enemy, board));
            }
        }
        return validAttacks;
    }

    public static Set<Tile> getValidTargets(Tile tile, Player enemy, Tile[][] board) {

        Set<Tile> validAttacks = new HashSet<>();
                
        for (Unit unit : enemy.getUnits()) {
            int unitx = unit.getPosition().getTilex();
            int unity = unit.getPosition().getTiley();

            if (Math.abs(unitx - tile.getTilex()) < 2 && Math.abs(unity - tile.getTiley()) < 2) {
                validAttacks.add(board[unitx][unity]);
            }
        }
        
        /*
         * check for provoke units
         */
        for (Tile validAttack : validAttacks) {
        	if (validAttack.getOccupier() instanceof Provoke) {
        		System.out.println("PROVOKE UNIT IN THE HOOSE");
        		return ((Provoke)validAttack.getOccupier()).specialAbility(validAttack.getOccupier());        		        		
        	}
        }
        
        
        return validAttacks;
    }

    /*
     * Attacking logic
     */
	public static void adjacentAttack(Unit attacker, Unit defender) {
	
		if (!attacker.hasAttacked()) {
						
			Gui.performAttack(attacker);
			BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.idle);
			
			defender.setHealth(defender.getHealth()-attacker.getAttack());
			Gui.setUnitStats(defender, defender.getHealth(), defender.getAttack());
			
			attacker.setAttacked(); // commented out to test that unit dies
			
			checkEndGame(defender);
			counterAttack(attacker, defender);
		}
	}
	
	public static void distancedAttack(Unit attacker, Unit defender, Player enemy) {
        System.out.println("Distanced Attack Activated");
        if (!attacker.hasAttacked() && !attacker.hasMoved()) {

            // Get the valid tiles from which the unit can attack
            ArrayList<Tile> validTiles = getValidAttackTiles(defender);

            int minScore = Integer.MAX_VALUE;
            Tile closestTile = null;

            // Find the closest/optimal position to attack from by scoring each option
            for (Tile tile : validTiles) {
                int score = 0;
                score += Math.abs(tile.getTilex() - attacker.getPosition().getTilex());
                score += Math.abs(tile.getTiley() - attacker.getPosition().getTiley());
                if (score < minScore) {
                    minScore = score;
                    closestTile = tile;
                }

            }

            // move unit to the closest tile
            if (closestTile != null) {

                System.out.println("The closest tile is: x = " + closestTile.getTilex() + " and y = " + closestTile.getTiley() + " score " + minScore);

                moveUnit(attacker, closestTile);
                if (minScore < 2) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                adjacentAttack(attacker, defender);

            }
        }
    }
	
	 /*
     * Gets the valid attack positions for distanced attacks (move first and then attack)
     */

    public static ArrayList<Tile> getValidAttackTiles(Unit unit) {
        ArrayList<Tile> validTiles = new ArrayList<>();

        for (Tile tile : GameState.validMoves) {
            int unitx = unit.getPosition().getTilex();
            int unity = unit.getPosition().getTiley();
            if (Math.abs(unitx - tile.getTilex()) < 2 && Math.abs(unity - tile.getTiley()) < 2) {
                validTiles.add(tile);
            }

        }

        for (Tile tile : validTiles) {
            System.out.println("tile: x = " + tile.getTilex() + " and y = " + tile.getTiley());
        }

        return validTiles;
    }

	
	public static void placeUnit(ActorRef out, Card card, Player player, Tile tile){
		System.out.println("placeUnit Utility");
		
		/* Set unit id to number of total units on board + 1 */
        String unit_conf = StaticConfFiles.getUnitConf(card.getCardname());
        int unit_id = GameState.getTotalUnits();
        
        Unit unit = null;
        
        if (card.getCardname().equals("Silverguard Knight")) {
        	unit = (SilverguardKnight) BasicObjectBuilders.loadUnit(unit_conf, unit_id, SilverguardKnight.class);
        } else {
        	unit = BasicObjectBuilders.loadUnit(unit_conf, unit_id, Unit.class);
       }
       
        unit.setPositionByTile(tile);
        tile.setOccupier(unit);

		GameState.modifiyTotalUnits(1);
		
		//player.setUnit(unit);

		EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_summon);
        BasicCommands.playEffectAnimation(out, effect, tile);
		BasicCommands.drawUnit(out, unit, tile);
		player.setUnit(unit);

		BigCard bigCard = card.getBigCard();
		
		int attack = bigCard.getAttack();
		int health = bigCard.getHealth();
		unit.setMaxHealth(health);
		
		//Gui.setUnitStats(unit, health, attack);
        unit.setAttack(attack);
        unit.setHealth(health);

		GameState.modifiyTotalUnits(1);

		Gui.setUnitStats(unit, health, attack);

		int positionInHand = card.getPositionInHand();
		player.removeFromHand(positionInHand);
		BasicCommands.deleteCard(out, positionInHand);
		
		
        player.updateMana(-card.getManacost());
        CardClicked.currentlyHighlighted.remove(card);
        
		if (GameState.getHumanPlayer() == player){
			BasicCommands.setPlayer1Mana(out, player);
		}
		else {
			BasicCommands.setPlayer2Mana(out, player);
		}


    }

    public static Set<Tile> cardPlacements(Card card, Player player, Player enemy, Tile[][] board){
//        if (card.getManacost() > player.getMana()){
//             return null;
//        }
    	System.out.println("cardPlacement Utility");
        Set<Tile> validTiles = new HashSet<Tile>();


        Set<Tile> playerUnits = getPlayerUnitPositions(player, board);
        Set<Tile> enemyUnits = getEnemyUnitPositions(enemy, board);
        
        /* if card can be played on all squares, return the board - occupied squares */
        if (card.getMoveModifier()){
            validTiles.removeAll(playerUnits);
            validTiles.removeAll(enemyUnits);
            return validTiles;
        }

        int x, y;
        
        Set<Tile> validPlacements =  new HashSet<Tile>();

        /* Add squares around player units to set. Return this minus occupied squares */
        for (Tile tile : playerUnits){
            x = tile.getTilex();
            y = tile.getTiley();
            for (int i = -1 ; i <= 1 ; i++){
                for (int j = -1 ; j <= 1 ; j++){
                    validPlacements.add(board[x + i][y + j]);
                }
            }
        }
       
        validPlacements.removeAll(playerUnits);
        validPlacements.removeAll(enemyUnits);
        return validPlacements;
    }
    
    public static Set<Tile> getPlayerUnitPositions(Player player, Tile[][] board){
    	
        Set<Tile> s = new HashSet<Tile>();
        
        for (Unit unit : player.getUnits()){
            /* Add unit to set of player positions */
            s.add(board[unit.getPosition().getTilex()][unit.getPosition().getTiley()]);
        }
        return s;

    }
    
    public static Set<Tile> getEnemyUnitPositions(Player enemy, Tile[][] board){
        Set<Tile> s = new HashSet<Tile>();
		ArrayList<Unit> uList = enemy.getUnits();
        for (Unit unit : uList){
            /* Add unit to set of enemy positions */
            s.add(board[unit.getPosition().getTilex()][unit.getPosition().getTiley()]);
        }
        return s;
    }

    public static boolean validMove(ActorRef out, Card card, Player player, Player enemy, Tile tile, Tile[][] board){
        if (card.getManacost() > player.getMana()){
            return false;
        }
        Set<Tile> s = cardPlacements(card, player, enemy, board);
        if (s.contains(tile)){
            return true;
        }
        return false;
    }
    
    

    public static Set<Tile> showValidMoves(Card card, Player player, Player enemy, Tile[][] board){
        Set<Tile> s = cardPlacements(card, player, enemy, board);
        return s;
    }
    
    

	public static void checkEndGame(Unit defender) {
		//unit death
		if(defender.getHealth() <= 0) {
			BasicCommands.playUnitAnimation(out, defender, UnitAnimationType.death);
			try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
			GameState.board[defender.getPosition().getTilex()][defender.getPosition().getTiley()].setOccupier(null); //remove unit from tiles
			BasicCommands.deleteUnit(out, defender); //delete unit from board
			
//		AI unit
			if(GameState.getAiPlayer().getUnits().contains(defender)) {
				GameState.getAiPlayer().removeUnit(defender); 
				
				//GameState.getAiPlayer().setHealth(0); //for testing purposes <= DOES THIS NEED TO GO???
				
				if(GameState.getAiPlayer().getHealth() <= 0) {
					BasicCommands.addPlayer1Notification(out, "Player 1 wins!", 20);
					//game over:
				}
				
//		Human unit
			}else if(GameState.getHumanPlayer().getUnits().contains(defender)) {
				GameState.getHumanPlayer().removeUnit(defender);
				if(GameState.getHumanPlayer().getHealth() <= 0) {
					BasicCommands.addPlayer1Notification(out, "You lose!", 20);
				}
			}	
		}
	}
	

    public static void moveUnit(Unit unit, Tile tile) {
        GameState.board[unit.getPosition().getTilex()][unit.getPosition().getTiley()].setOccupier(null); //clear unit from tile

        BasicCommands.moveUnitToTile(out, unit, tile); //move unit to chosen tiles
        unit.setPositionByTile(tile); //change position of unit to new tiles

        tile.setOccupier(unit); //set unit as occupier of tiles

        unit.setMoved();
        
        Gui.removeHighlightTiles(out, GameState.board); //clearing board
    }


    public static Set<Tile> determineValidMoves(Tile[][] board, Unit unit) {

        Set<Tile> validTiles = new HashSet<>();

        if (unit.getClass().equals(Windshrike.class) && !unit.hasMoved() && !unit.hasAttacked()) {
            return ((Windshrike) unit).specialAbility(board);
            
            
        } else if (!unit.hasMoved() && !unit.hasAttacked()) {
            int x = unit.getPosition().getTilex();
            int y = unit.getPosition().getTiley();
            // check one behind
            int newX = x - 1;
            if (newX > -1 && newX < board.length && board[newX][y].getOccupier() == null) {
                validTiles.add(board[newX][y]);
                // if one behind empty, check two behind
                newX = x - 2;
                if (newX > -1 && newX < board.length && board[newX][y].getOccupier() == null) {
                    validTiles.add(board[newX][y]);
                }
            }
            // check one ahead
            newX = x + 1;
            if (newX > -1 && newX < board.length && board[newX][y].getOccupier() == null) {
                validTiles.add(board[newX][y]);
                // if one ahead empty, check two ahead
                newX = x + 2;
                if (newX > -1 && newX < board.length && board[newX][y].getOccupier() == null) {
                    validTiles.add(board[newX][y]);
                }
            }
            // check one up
            int newY = y - 1;
            if (newY > -1 && newY < board[0].length && board[x][newY].getOccupier() == null) {
                validTiles.add(board[x][newY]);
                // if one up empty, check two up
                newY = y - 2;
                if (newY > -1 && newY < board[0].length && board[x][newY].getOccupier() == null) {
                    validTiles.add(board[x][newY]);
                }
            }
            // check one down
            newY = y + 1;
            if (newY > -1 && newY < board[0].length && board[x][newY].getOccupier() == null) {
                validTiles.add(board[x][newY]);
                // if one up empty, check two up
                newY = y + 2;
                if (newY > -1 && newY < board[0].length && board[x][newY].getOccupier() == null) {
                    validTiles.add(board[x][newY]);
                }
            }

            // diagonal tiles
            if (x + 1 < board.length && y + 1 < board[0].length && board[x + 1][y + 1].getOccupier() == null) {
                validTiles.add(board[x + 1][y + 1]);
            }

            if (x - 1 >= 0 && y - 1 >= 0 && board[x - 1][y - 1].getOccupier() == null) {
                validTiles.add(board[x - 1][y - 1]);
            }

            if (x + 1 < board.length && y - 1 >= 0 && board[x + 1][y - 1].getOccupier() == null) {
                validTiles.add(board[x + 1][y - 1]);
            }

            if (x - 1 >= 0 && y + 1 < board[0].length && board[x - 1][y + 1].getOccupier() == null) {
                validTiles.add(board[x - 1][y + 1]);
            }

        } else {
            // cannot move, so what happens? just return empty set?
            return validTiles;
        }
        return validTiles;
    }

    public static void counterAttack(Unit attacker, Unit countAttacker) {
        int x = countAttacker.getPosition().getTilex();
        int y = countAttacker.getPosition().getTiley();

        int range = 1;

        if (countAttacker.getHealth() > 0) {
            for (int i = Math.max(0, x - range); i <= Math.min(GameState.board.length - 1, x + range); i++) {
                for (int j = Math.max(0, y - range); j <= Math.min(GameState.board[0].length - 1, y + range); j++) {
                    if (i == x && j == y) {
                        continue; // this is where the unit (countAttacker) is
                    } else if (attacker.getPosition().getTilex() == i & attacker.getPosition().getTiley() == j) {
                        //adjacentAttack(countAttacker, attacker);
                    	Gui.performAttack(countAttacker);
                    	BasicCommands.playUnitAnimation(out, countAttacker, UnitAnimationType.idle);
                        //attacker.setAttacked(); // - believe we dont need this

                        int newHealth = attacker.getHealth() - countAttacker.getAttack();
                        attacker.setHealth(newHealth);
                        Gui.setUnitStats(attacker, attacker.getHealth(), attacker.getAttack());
                        
                        checkEndGame(attacker);
                    	
                    }
                }
            }
        }
    }

	// Get positions of potential targets of a spell.
	public static Set<Tile> getSpellTargetPositions(ArrayList<Unit> targets) {
		Set<Tile> positions = new HashSet<>();

		for (Unit unit : targets) {
			int unitx = unit.getPosition().getTilex();
			int unity = unit.getPosition().getTiley();
			positions.add(GameState.getBoard()[unitx][unity]);
		}
		return positions;
	}

	public static Set<Tile> boardToSet(Tile[][] board){
		Set<Tile> s = new HashSet<Tile>();
		for (Tile[] a : board){
			s.addAll(Arrays.asList(a));
		}
		return s;
	}

}
