package org.appledash.saneeconomy.command;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.utils.PlayerUtils;
import org.appledash.sanelib.command.SaneCommand;
import org.appledash.sanelib.command.exception.CommandException;
import org.appledash.sanelib.command.exception.type.usage.NeedPlayerException;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class BalanceCommand extends SaneCommand {
    private final SaneEconomy saneEconomy;

    public BalanceCommand(SaneEconomy saneEconomy) {
        super(saneEconomy);
        this.saneEconomy = saneEconomy;
    }

    @Override
    public String getPermission() {
        return "saneeconomy.balance";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                   "/<command> [player]"
               };
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) throws CommandException {
        String playerIdentifier;
        String playerName;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                throw new NeedPlayerException();
            }

            Player player = (Player) sender;

            playerIdentifier = player.getUniqueId().toString();
            playerName = player.getDisplayName();
        } else {
            playerIdentifier = args[0];
            playerName = args[0];

            if (!sender.hasPermission("saneeconomy.balance.other")) {
                this.saneEconomy.getMessenger().sendMessage(sender, "You don't have permission to check the balance of {1}.", playerIdentifier);
                return;
            }
        }

        OfflinePlayer player = PlayerUtils.getOfflinePlayer(playerIdentifier);

        if (player == null) {
            this.saneEconomy.getMessenger().sendMessage(sender, "That player does not exist.");
            return;
        }

        if (sender == player) {
            this.saneEconomy.getMessenger().sendMessage(sender, "Your balance is {1}.", saneEconomy.getEconomyManager().getFormattedBalance(Economable.wrap(player)));
        } else {
            this.saneEconomy.getMessenger().sendMessage(sender, "Balance for {1} is {2}.", playerName, saneEconomy.getEconomyManager().getFormattedBalance(Economable.wrap(player)));
        }
    }
}
