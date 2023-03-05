package structures.basic.spellcards;

import akka.actor.ActorRef;
import game.logic.Gui;
import game.logic.Utility;
import structures.GameState;
import structures.basic.*;
import structures.basic.SpecialUnits.Pureblade;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.Set;

public class Sundrop extends SpellCard {

    // highlight valid targets for this particular spell
    public void highlightTargets(ActorRef out) {
    	
    	System.out.println("Sundrop - HIHGLIGHT TARGETS CALLED");
        ArrayList<Unit> units = GameState.getCurrentPlayer().getUnits();
        Set<Tile> positions = Utility.getSpellTargetPositions(units);
        positions.addAll(Utility.getSpellTargetPositions(GameState.getOtherPlayer().getUnits()));
        Gui.highlightTiles(out,positions,2);
    }
    @Override
    public boolean castSpell(Unit target, Tile targetTile) {
        // Check the player owns this unit. This spell card can only be applied to friendlies
        if(!GameState.getCurrentPlayer().getUnits().contains(target)) return false;

        this.handleSpellThief();
        // Add health to the unit. Cap so the new value doesn't exceed the unit's max health
        // TODO Cap so the new value doesn't exceed the unit's max health// will become easier once unit cards implemented
        target.setHealth(Math.min(target.getHealth() + 5, target.getMaxHealth()));
        Gui.playEffectAnimation(BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff),targetTile);
        return true;
    }

    /*
     * Checks if unit has SpellThief by checcking unit id, if so, applies the affect
     */
    @Override
    //this needs to be removed post testing
    public void handleSpellThief(){
        Player enemy = GameState.getHumanPlayer();
        for (Unit unit : enemy.getUnits()){
            if (unit.getClass().equals(Pureblade.class)){
                Pureblade p = (Pureblade)unit;
                p.specialAbility();
                return;
            }
        }
        return;
    }
}
