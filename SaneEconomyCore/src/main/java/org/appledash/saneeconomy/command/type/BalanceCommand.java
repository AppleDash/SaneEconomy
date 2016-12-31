package org.appledash.saneeconomy.command.type;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.command.SaneEconomyCommand;
import org.appledash.saneeconomy.command.exception.CommandException;
import org.appledash.saneeconomy.command.exception.type.usage.NeedPlayerException;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.appledash.saneeconomy.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class BalanceCommand extends SaneEconomyCommand {
    public BalanceCommand(SaneEconomy saneEconomy) {
        super(saneEconomy);
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
                MessageUtils.sendMessage(sender, "You don't have permission to check the balance of {1}.", playerIdentifier);
                return;
            }
        }

        OfflinePlayer player = PlayerUtils.getOfflinePlayer(playerIdentifier);

        if (player == null) {
            MessageUtils.sendMessage(sender, "That player does not exist.");
            return;
        }

        MessageUtils.sendMessage(sender, "Balance for {1} is {2}.", playerName, saneEconomy.getEconomyManager().getFormattedBalance(Economable.wrap(player)));
    }
}
