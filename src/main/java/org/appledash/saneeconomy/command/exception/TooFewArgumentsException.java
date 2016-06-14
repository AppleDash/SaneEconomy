package org.appledash.saneeconomy.command.exception;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class TooFewArgumentsException extends CommandException {
    @Override
    public String getMessage() {
        return "Wrong number of arguments for that command!";
    }
}
