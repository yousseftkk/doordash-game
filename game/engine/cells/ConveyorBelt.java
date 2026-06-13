package game.engine.cells;

import game.engine.monsters.Monster;

public class ConveyorBelt extends TransportCell {	
	
	public ConveyorBelt(String name, int effect) {
		super(name, effect);
	}
	public void transport(Monster monster) {
		int old = monster.getPosition();
		
		super.transport(monster);
		System.out.println("Landed on conveyor belt " + monster.getName() + " went from " + old + " to " + monster.getPosition());
	}
}
