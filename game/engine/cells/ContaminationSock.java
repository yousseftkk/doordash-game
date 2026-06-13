package game.engine.cells;

import game.engine.Constants;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class ContaminationSock extends TransportCell implements CanisterModifier {
	
	public ContaminationSock(String name, int effect) {

		super(name, effect);
	}

	@Override
    public void transport(Monster monster) {
		int old = monster.getPosition();
		
		super.transport(monster);
		System.out.println("Landed on contamination sock " + monster.getName() + " went from " + old + " to " + monster.getPosition() + 
				" and lost " + Constants.SLIP_PENALTY + " energy!");
        // Apply slip penalty
		modifyCanisterEnergy(monster, -Constants.SLIP_PENALTY);
    }

	@Override
	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
		monster.alterEnergy(canisterValue);
	}

}

