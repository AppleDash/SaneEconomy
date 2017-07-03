package org.appledash.saneeconomy.command;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.sanelib.command.SaneCommand;
import org.appledash.sanelib.command.exception.CommandException;
import org.appledash.sanelib.command.exception.type.usage.InvalidUsageException;
import org.bukkit.command.CommandSender;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class SaneEcoCommand extends SaneCommand {
    private final SaneEconomy saneEconomy;

    public SaneEcoCommand(SaneEconomy saneEconomy) {
        super(saneEconomy);
        this.saneEconomy = saneEconomy;
    }

    @Override
    public String getPermission() {
        return "saneeconomy.admin";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "/<command> reload-database"
        };
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) {
            throw new InvalidUsageException();
        }

        String subCommand = args[0];

        if (subCommand.equalsIgnoreCase("reload-database")) {
            this.saneEconomy.getMessenger().sendMessage(sender, "Reloading database...");
            saneEconomy.getEconomyManager().getBackend().reloadDatabase();
            this.saneEconomy.getMessenger().sendMessage(sender, "Database reloaded.");
        } else {
            throw new InvalidUsageException();
        }
    }
}
