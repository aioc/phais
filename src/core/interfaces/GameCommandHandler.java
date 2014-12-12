package core.interfaces;

/**
 * This interface needs to be implemented by the game maker.
 * <p />
 * Used to handle commands inputed, that are game specific.
 */

public interface GameCommandHandler {

	/**
	 * This method is given any command that has not been processed, and should handle it if able.
	 * It should return whether the command was handled or not.
	 * @param command The command name
	 * @param args A list of additional arguments with the command
	 * 
	 * @return True iff the command was handled by the game.
	 */
	public boolean handleCommand(String command, String[] args);
	
}
