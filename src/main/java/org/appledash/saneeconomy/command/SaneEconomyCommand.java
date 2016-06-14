package org.appledash.saneeconomy.command;

import org.appledash.saneeconomy.command.exception.*;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public abstract class SaneEconomyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!sender.hasPermission(getPermission())) {
                throw new NoPermissionException();
            }

            return onCommand(sender, args);
        } catch (TooFewArgumentsException | NeedPlayerException | InvalidUsageException e) {
            MessageUtils.sendMessage(sender, e.getMessage());
            for (String s : getUsage()) {
                MessageUtils.sendMessage(sender, String.format("Usage: %s", s.replace("<command>", label)));
            }
            return true;
        } catch (CommandException e) {
            MessageUtils.sendMessage(sender, e.getMessage());
            return true;
        }
    }

    public abstract String getPermission();
    public abstract String[] getUsage();
    public abstract boolean onCommand(CommandSender sender, String[] args) throws CommandException;
}
