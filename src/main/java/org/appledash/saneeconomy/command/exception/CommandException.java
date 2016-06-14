package org.appledash.saneeconomy.command.exception;

import org.appledash.saneeconomy.command.exception.type.usage.TooFewArgumentsException;
import org.appledash.saneeconomy.command.exception.type.usage.TooManyArgumentsException;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class CommandException extends Exception {

    /**
     * Construct the proper CommandException for the given number of expected and actual arguments.
     * A TooManyArgumentsException is returned if actual > expected, otherwise a TooFewArguemntsException is returned.
     * @param expectedCount Expected number of arguments
     * @param actualCount Actual number of arguments
     * @return The right exception
     */
    public static CommandException makeArgumentException(int expectedCount, int actualCount) {
        return actualCount > expectedCount ? new TooManyArgumentsException() : new TooFewArgumentsException();
    }
}
