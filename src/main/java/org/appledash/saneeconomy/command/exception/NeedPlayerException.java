package org.appledash.saneeconomy.command.exception;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class NeedPlayerException extends CommandException {
    @Override
    public String getMessage() {
        return "That command requires a player argument when not run by a player.";
    }
}
