package org.appledash.saneeconomy.command.type;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.command.SaneEconomyCommand;
import org.appledash.saneeconomy.command.exception.CommandException;
import org.appledash.saneeconomy.command.exception.type.usage.InvalidUsageException;
import org.appledash.saneeconomy.utils.I18n;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.bukkit.command.CommandSender;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class SaneEcoCommand extends SaneEconomyCommand {
    public SaneEcoCommand(SaneEconomy saneEconomy) {
        super(saneEconomy);
    }

    @Override
    public String getPermission() {
        return "saneeconomy.admin";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                I18n._("/<command> reload-database")
        };
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) {
            throw new InvalidUsageException();
        }

        String subCommand = args[0];

        if (subCommand.equalsIgnoreCase("reload-database")) {
            MessageUtils.sendMessage(sender, "Reloading database...");
            saneEconomy.getEconomyManager().getBackend().reloadDatabase();
            MessageUtils.sendMessage(sender, "Database reloaded.");
        } else {
            throw new InvalidUsageException();
        }
    }
}
