package structures.basic.spellcards;
import akka.actor.ActorRef;
import structures.basic.*;

public abstract class SpellCard implements Playable{

    public abstract boolean castSpell(Unit target, Tile targetTile); // perform ability. Report back if successful.

    public abstract void highlightTargets(ActorRef out); // highlight valid targets for a particular spellcard.

}
