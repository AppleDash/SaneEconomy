package org.appledash.saneeconomy.command.exception.type.usage;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class TooManyArgumentsException extends UsageException {
    @Override
    public String getMessage() {
        return "Too many arguments for that command!";
    }
}
