package org.appledash.saneeconomy.command.type;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.command.SaneEconomyCommand;
import org.appledash.saneeconomy.command.exception.CommandException;
import org.appledash.saneeconomy.command.exception.InvalidUsageException;
import org.appledash.saneeconomy.command.exception.NeedPlayerException;
import org.appledash.saneeconomy.command.exception.TooFewArgumentsException;
import org.appledash.saneeconomy.utils.MessageUtils;
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
        return "saneeconomy.admin";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "/<command> <give/take/set> [player] <amount>"
        };
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) throws CommandException {
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
            MessageUtils.sendMessage(sender, "That player is not online!");
            return true;
        }

        double amount;

        try {
            amount = Double.valueOf(sAmount);
        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(sender, "%s is not a number!", sAmount);
            return true;
        }

        if (amount < 0) {
            MessageUtils.sendMessage(sender, "%s is not a positive number.", sAmount);
            return true;
        }

        if (subCommand.equalsIgnoreCase("give")) {
            double newAmount = SaneEconomy.getInstance().getEconomyManager().addBalance(targetPlayer, amount);

            MessageUtils.sendMessage(sender, "New balance for %s is %s", sTargetPlayer, SaneEconomy.getInstance().getEconomyManager().getCurrency().formatAmount(newAmount));
            return true;
        } else if (subCommand.equalsIgnoreCase("take")) {
            double newAmount = SaneEconomy.getInstance().getEconomyManager().subtractBalance(targetPlayer, amount);

            MessageUtils.sendMessage(sender, "New balance for %s is %s", sTargetPlayer, SaneEconomy.getInstance().getEconomyManager().getCurrency().formatAmount(newAmount));
            return true;
        } else if (subCommand.equalsIgnoreCase("set")) {
            SaneEconomy.getInstance().getEconomyManager().setBalance(targetPlayer, amount);
            MessageUtils.sendMessage(sender, "Balance for %s set to %s", sTargetPlayer, SaneEconomy.getInstance().getEconomyManager().getCurrency().formatAmount(amount));
            return true;
        }

        throw new InvalidUsageException();
    }
}
