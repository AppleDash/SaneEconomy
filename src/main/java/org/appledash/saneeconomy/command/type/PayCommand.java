package org.appledash.saneeconomy.command.type;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.command.SaneEconomyCommand;
import org.appledash.saneeconomy.command.exception.CommandException;
import org.appledash.saneeconomy.command.exception.type.usage.NeedPlayerException;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.appledash.saneeconomy.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 *
 * TODO: Support for paying offline players.
 */
public class PayCommand extends SaneEconomyCommand {
    @Override
    public String getPermission() {
        return "saneeconomy.pay";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "/pay <player> <amount>"
        };
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw CommandException.makeArgumentException(2, args.length);
        }

        /* Doesn't make sense for console to pay a player, and admins paying a player is best done with /ecoadmin give */
        if (!(sender instanceof Player)) {
            throw new NeedPlayerException();
        }

        Player fromPlayer = (Player) sender;

        String sToPlayer = args[0];
        Player toPlayer = Bukkit.getServer().getPlayer(sToPlayer);

        if (toPlayer == null) {
            MessageUtils.sendMessage(sender, "That player is not online.");
            return;
        }

        String sAmount = args[1];
        double amount = NumberUtils.parsePositiveDouble(sAmount);

        if (amount == -1) {
            MessageUtils.sendMessage(sender, "%s is not a positive number.", sAmount);
            return;
        }

        EconomyManager ecoMan = SaneEconomy.getInstance().getEconomyManager();

        /* Perform the actual transfer. False == They didn't have enough money */
        boolean result = ecoMan.transfer(fromPlayer, toPlayer, amount);

        if (!result) {
            MessageUtils.sendMessage(sender, "You do not have enough money to transfer %s to %s.",
                    ecoMan.getCurrency().formatAmount(amount),
                    sToPlayer
            );

            return;
        }

        /* Inform the relevant parties. */

        MessageUtils.sendMessage(sender, "You have transferred %s to %s.",
                ecoMan.getCurrency().formatAmount(amount),
                sToPlayer
        );

        MessageUtils.sendMessage(toPlayer, "You have received %s from %s.",
                ecoMan.getCurrency().formatAmount(amount),
                fromPlayer.getDisplayName()
        );
    }
}
