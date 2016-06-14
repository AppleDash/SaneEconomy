package org.appledash.saneeconomy.command.exception.type.usage;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class InvalidUsageException extends UsageException {
    @Override
    public String getMessage() {
        return "Invalid syntax for that command!";
    }
}
