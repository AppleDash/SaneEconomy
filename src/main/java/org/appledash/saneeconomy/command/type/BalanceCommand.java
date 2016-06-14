package org.appledash.saneeconomy.command.type;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.command.SaneEconomyCommand;
import org.appledash.saneeconomy.command.exception.CommandException;
import org.appledash.saneeconomy.command.exception.type.usage.NeedPlayerException;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class BalanceCommand extends SaneEconomyCommand {
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
        String playerName;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                throw new NeedPlayerException();
            }

            playerName = sender.getName();
        } else {
            playerName = args[0];
        }

        Player player = Bukkit.getServer().getPlayer(playerName);

        if (player == null) {
            MessageUtils.sendMessage(sender, "That player is not online.");
            return;
        }

        MessageUtils.sendMessage(sender, "Balance for %s is %s.", playerName, SaneEconomy.getInstance().getEconomyManager().getFormattedBalance(player));
    }
}
