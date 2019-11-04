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
                   "/<command> reload - Reload everything.",
                   "/<command> reload-database - Reload the database.",
                   "/<command> reload-config - Reload the configuration."
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
        } else if (subCommand.equalsIgnoreCase("reload-config")) {
            this.saneEconomy.getMessenger().sendMessage(sender, "Reloading configuration...");
            this.saneEconomy.loadConfig();
            this.saneEconomy.getMessenger().sendMessage(sender, "Configuration reloaded.");
        } else if (subCommand.equalsIgnoreCase("reload")) {
            this.saneEconomy.getMessenger().sendMessage(sender, "Reloading configuration and database...");
            this.saneEconomy.loadConfig();
            this.saneEconomy.getEconomyManager().getBackend().reloadDatabase();
            this.saneEconomy.getMessenger().sendMessage(sender, "Configuration and database reloaded.");
        } else {
            throw new InvalidUsageException();
        }
    }
}
