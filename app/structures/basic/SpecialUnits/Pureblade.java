package structures.basic.SpecialUnits;
import game.logic.Gui;

import structures.GameState;
import structures.basic.*;

import java.util.HashSet;
import java.util.Set;

public class Pureblade extends Unit {

    private final String name = "Pureblade Enforcer";

    public void specialAbility(){
        this.modAttack(1);
        this.modHealth(1);
        Gui.setUnitStats(this, this.getHealth(), this.getAttack());
    }
}