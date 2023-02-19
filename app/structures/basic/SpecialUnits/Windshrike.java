package structures.basic.SpecialUnits;

import structures.GameState;
import structures.basic.*;

import java.util.HashSet;
import java.util.Set;

public class Windshrike extends Unit {

    private final String name = "Windshrike";

    public static Set<Tile> specialAbility(Tile[][] board) {
        System.out.println("SPECIAL ABILITY IS CALLED");

        Set<Tile> validTiles = new HashSet<>();

        for (Tile[] row : board) {
            for (Tile tile : row) {
                if (tile.getOccupier() == null) {
                    validTiles.add(tile);
                }
            }
        }
        return validTiles;
    }

    //    public Windshrike(int id, UnitAnimationType animation, Position position, UnitAnimationSet animations,
//                      ImageCorrection correction) {
//        super(id, animation, position, animations, correction);
//        name = "Windshrike";
//    }

}
