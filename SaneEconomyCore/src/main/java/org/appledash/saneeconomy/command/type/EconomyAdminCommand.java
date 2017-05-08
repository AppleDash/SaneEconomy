package org.appledash.saneeconomy.command.type;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.command.SaneEconomyCommand;
import org.appledash.saneeconomy.command.exception.CommandException;
import org.appledash.saneeconomy.command.exception.type.usage.InvalidUsageException;
import org.appledash.saneeconomy.command.exception.type.usage.NeedPlayerException;
import org.appledash.saneeconomy.command.exception.type.usage.TooFewArgumentsException;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionReason;
import org.appledash.saneeconomy.economy.transaction.TransactionResult;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.appledash.saneeconomy.utils.NumberUtils;
import org.appledash.saneeconomy.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class EconomyAdminCommand extends SaneEconomyCommand {
    public EconomyAdminCommand(SaneEconomy saneEconomy) {
        super(saneEconomy);
    }

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

        OfflinePlayer targetPlayer = PlayerUtils.getOfflinePlayer(sTargetPlayer);

        if (targetPlayer == null) {
            MessageUtils.sendMessage(sender, "That player does not exist.");
            return;
        }

        EconomyManager ecoMan = saneEconomy.getEconomyManager();
        Economable economable = Economable.wrap(targetPlayer);

        double amount = NumberUtils.parseAndFilter(ecoMan.getCurrency(), sAmount);

        if (amount <= 0) {
            MessageUtils.sendMessage(sender, "{1} is not a positive number.", ((amount == -1) ? sAmount : String.valueOf(amount)));
            return;
        }

        if (subCommand.equalsIgnoreCase("give")) {
            Transaction transaction = new Transaction(Economable.wrap(sender), Economable.wrap(targetPlayer), amount, TransactionReason.ADMIN_GIVE);
            TransactionResult result = ecoMan.transact(transaction);

            double newAmount = result.getToBalance();

            MessageUtils.sendMessage(sender, "Added {1} to {2}. Their balance is now {3}.",
                    ecoMan.getCurrency().formatAmount(amount),
                    sTargetPlayer,
                    ecoMan.getCurrency().formatAmount(newAmount)
            );
            return;
        }

        if (subCommand.equalsIgnoreCase("take")) {
            Transaction transaction = new Transaction(Economable.wrap(targetPlayer), Economable.wrap(sender), amount, TransactionReason.ADMIN_TAKE);
            TransactionResult result = ecoMan.transact(transaction);

            double newAmount = result.getFromBalance();

            MessageUtils.sendMessage(sender, "Took {1} from {2}. Their balance is now {3}.",
                    ecoMan.getCurrency().formatAmount(amount),
                    sTargetPlayer,
                    ecoMan.getCurrency().formatAmount(newAmount)
            );
            return;
        }

        if (subCommand.equalsIgnoreCase("set")) {
            double oldBal = ecoMan.getBalance(economable);
            ecoMan.setBalance(economable, amount);
            MessageUtils.sendMessage(sender, "Balance for {1} set to {2}.", sTargetPlayer, ecoMan.getCurrency().formatAmount(amount));

            saneEconomy.getTransactionLogger().ifPresent((logger) -> {
                // FIXME: This is a silly hack to get it to log.
                if (oldBal > 0.0) {
                    logger.logTransaction(new Transaction(
                            economable, Economable.CONSOLE, oldBal, TransactionReason.ADMIN_GIVE
                    ));
                }

                logger.logTransaction(new Transaction(
                        Economable.CONSOLE, economable, amount, TransactionReason.ADMIN_GIVE
                ));
            });

            return;
        }

        throw new InvalidUsageException();
    }
}
