package org.appledash.saneeconomy.command.type;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.command.SaneEconomyCommand;
import org.appledash.saneeconomy.command.exception.CommandException;
import org.appledash.saneeconomy.command.exception.type.usage.InvalidUsageException;
import org.appledash.saneeconomy.command.exception.type.usage.NeedPlayerException;
import org.appledash.saneeconomy.command.exception.type.usage.TooFewArgumentsException;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.appledash.saneeconomy.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class EconomyAdminCommand extends SaneEconomyCommand {
    @Override
    public String getPermission() {
        return "saneeconomy.ecoadmin";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "/<command> <give/take/set> [player] <amount>"
        };
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new TooFewArgumentsException();
        }

        String subCommand = args[0];
        String sTargetPlayer;
        String sAmount;

        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                throw new NeedPlayerException();
            }

            sTargetPlayer = sender.getName();
            sAmount = args[1];
        } else {
            sTargetPlayer = args[1];
            sAmount = args[2];
        }

        Player targetPlayer = Bukkit.getServer().getPlayer(sTargetPlayer);

        if (targetPlayer == null) {
            MessageUtils.sendMessage(sender, "That player is not online.");
            return;
        }

        double amount = NumberUtils.parseAndFilter(sAmount);

        if (amount <= 0) {
            MessageUtils.sendMessage(sender, "%s is not a positive number.", (amount == -1 ? sAmount : amount + ""));
            return;
        }

        EconomyManager ecoMan = SaneEconomy.getInstance().getEconomyManager();

        if (subCommand.equalsIgnoreCase("give")) {
            double newAmount = ecoMan.addBalance(targetPlayer, amount);

            MessageUtils.sendMessage(sender, "Added %s to %s. Their balance is now %s.",
                    ecoMan.getCurrency().formatAmount(amount),
                    sTargetPlayer,
                    ecoMan.getCurrency().formatAmount(newAmount)
            );
            return;
        } else if (subCommand.equalsIgnoreCase("take")) {
            double newAmount = ecoMan.subtractBalance(targetPlayer, amount);

            MessageUtils.sendMessage(sender, "Took %s from %s. Their balance is now %s.",
                    ecoMan.getCurrency().formatAmount(amount),
                    sTargetPlayer,
                    ecoMan.getCurrency().formatAmount(newAmount)
            );
            return;
        } else if (subCommand.equalsIgnoreCase("set")) {
            ecoMan.setBalance(targetPlayer, amount);
            MessageUtils.sendMessage(sender, "Balance for %s set to %s", sTargetPlayer, ecoMan.getCurrency().formatAmount(amount));
            return;
        }

        throw new InvalidUsageException();
    }
}
