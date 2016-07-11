package org.appledash.saneeconomy.command.exception.type;

import org.appledash.saneeconomy.command.exception.CommandException;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class NoPermissionException extends CommandException {
    @Override
    public String getMessage() {
        return "You do not have permission to do that.";
    }
}
