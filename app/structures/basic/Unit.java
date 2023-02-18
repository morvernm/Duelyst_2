package structures.basic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import game.logic.Gui;
import structures.GameState;

/**
 * This is a representation of a Unit on the game board.
 * A unit has a unique id (this is used by the front-end.
 * Each unit has a current UnitAnimationType, e.g. move,
 * or attack. The position is the physical position on the
 * board. UnitAnimationSet contains the underlying information
 * about the animation frames, while ImageCorrection has
 * information for centering the unit on the tile. 
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Unit {

	@JsonIgnore
	protected static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java objects from a file
	
	int id;
	UnitAnimationType animation;
	Position position;
	UnitAnimationSet animations;
	ImageCorrection correction;

	private int health;
	//TODO 5 IS A TEST VALUE. MUST BE CHANGED ONCE UNITCARDS ARE IMPLEMENTED
	private int maxHealth = 20; // This health value must never be exceeded when using healing spells
	
	private boolean attacked;
	private boolean moved;
	
	
	public Unit() {
		
		this.attacked = false;
		this.moved = false;
	}
	
	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		
		position = new Position(0,0,0,0);
		this.correction = correction;
		this.animations = animations;
		
		this.attacked = false;
		this.moved = false;
	}
	
	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		
		position = new Position(currentTile.getXpos(),currentTile.getYpos(),currentTile.getTilex(),currentTile.getTiley());
		this.correction = correction;
		this.animations = animations;
		
		this.attacked = false;
		this.moved = false;
	}
		
	public Unit(int id, UnitAnimationType animation, Position position, UnitAnimationSet animations,
			ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = animation;
		this.position = position;
		this.animations = animations;
		this.correction = correction;
		
		this.attacked = false;
		this.moved = false;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public UnitAnimationType getAnimation() {
		return animation;
	}
	public void setAnimation(UnitAnimationType animation) {
		this.animation = animation;
	}

	public ImageCorrection getCorrection() {
		return correction;
	}

	public void setCorrection(ImageCorrection correction) {
		this.correction = correction;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public UnitAnimationSet getAnimations() {
		return animations;
	}

	public void setAnimations(UnitAnimationSet animations) {
		this.animations = animations;
	}
	
	public void setMoved() {
		this.moved = true;
	}	
	public boolean hasMoved() {
		return this.moved;
	}
	public void clearMoved() {
		this.moved = false;
	}
	
	public void setAttacked() {
		this.attacked = true; 
	}
	public boolean hasAttacked() {
		return this.attacked;
	}
	public void clearAttacked() {
		this.attacked = false;
	}

	public int getHealth() {
		return this.health;
	}

	public void setHealth(int health) {
		// Check if the unit is a player avatar. Adjust health of appropriate player accordingly if so.
		if(this.getId() == 0){
			GameState.getHumanPlayer().setHealth(health);
		}
		else if(this.getId() == 1) {
			GameState.getAIPlayer().setHealth(health);
		}

		this.health = health;
		Gui.setUnitHealth(this, health);
	}

	public int getMaxHealth() {
		return this.maxHealth;
	}
	
	/**
	 * This command sets the position of the Unit to a specified
	 * tile.
	 * @param tile
	 */
	@JsonIgnore
	public void setPositionByTile(Tile tile) {
		position = new Position(tile.getXpos(),tile.getYpos(),tile.getTilex(),tile.getTiley());
	}
	
	
}
