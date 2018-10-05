package org.appledash.saneeconomy.command;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionReason;
import org.appledash.saneeconomy.economy.transaction.TransactionResult;
import org.appledash.saneeconomy.utils.NumberUtils;
import org.appledash.saneeconomy.utils.PlayerUtils;
import org.appledash.sanelib.command.SaneCommand;
import org.appledash.sanelib.command.exception.CommandException;
import org.appledash.sanelib.command.exception.type.usage.InvalidUsageException;
import org.appledash.sanelib.command.exception.type.usage.NeedPlayerException;
import org.appledash.sanelib.command.exception.type.usage.TooFewArgumentsException;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class EconomyAdminCommand extends SaneCommand {
    private final SaneEconomy saneEconomy;

    public EconomyAdminCommand(SaneEconomy saneEconomy) {
        super(saneEconomy);
        this.saneEconomy = saneEconomy;
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
            this.saneEconomy.getMessenger().sendMessage(sender, "That player does not exist.");
            return;
        }

        EconomyManager ecoMan = saneEconomy.getEconomyManager();
        Economable economable = Economable.wrap(targetPlayer);

        double amount = NumberUtils.parseAndFilter(ecoMan.getCurrency(), sAmount);

        if (!(subCommand.equalsIgnoreCase("set") && amount == 0) && amount <= 0) { // If they're setting it to 0 it's fine, otherwise reject numbers under 1.
            this.saneEconomy.getMessenger().sendMessage(sender, "{1} is not a positive number.", ((amount == -1) ? sAmount : String.valueOf(amount)));
            return;
        }

        if (subCommand.equalsIgnoreCase("give")) {
            Transaction transaction = new Transaction(ecoMan.getCurrency(), Economable.wrap(sender), Economable.wrap(targetPlayer), amount, TransactionReason.ADMIN_GIVE);
            TransactionResult result = ecoMan.transact(transaction);

            double newAmount = result.getToBalance();

            this.saneEconomy.getMessenger().sendMessage(sender, "Added {1} to {2}. Their balance is now {3}.",
                    ecoMan.getCurrency().formatAmount(amount),
                    sTargetPlayer,
                    ecoMan.getCurrency().formatAmount(newAmount)
            );

            if (this.saneEconomy.getConfig().getBoolean("economy.notify-admin-give") && targetPlayer.isOnline()) {
                this.saneEconomy.getMessenger().sendMessage((Player) targetPlayer, "{1} has given you {2}. Your balance is now {3}.",
                        sender.getName(),
                        ecoMan.getCurrency().formatAmount(amount),
                        ecoMan.getCurrency().formatAmount(newAmount)

                );
            }
            return;
        }

        if (subCommand.equalsIgnoreCase("take")) {
            Transaction transaction = new Transaction(ecoMan.getCurrency(), Economable.wrap(targetPlayer), Economable.wrap(sender), amount, TransactionReason.ADMIN_TAKE);
            TransactionResult result = ecoMan.transact(transaction);

            double newAmount = result.getFromBalance();

            this.saneEconomy.getMessenger().sendMessage(sender, "Took {1} from {2}. Their balance is now {3}.",
                    ecoMan.getCurrency().formatAmount(amount),
                    sTargetPlayer,
                    ecoMan.getCurrency().formatAmount(newAmount)
            );

            if (this.saneEconomy.getConfig().getBoolean("economy.notify-admin-take") && targetPlayer.isOnline()) {
                this.saneEconomy.getMessenger().sendMessage((Player) targetPlayer, "{1} has taken {2} from you. Your balance is now {3}.",
                        sender.getName(),
                        ecoMan.getCurrency().formatAmount(amount),
                        ecoMan.getCurrency().formatAmount(newAmount)

                );
            }
            return;
        }

        if (subCommand.equalsIgnoreCase("set")) {
            double oldBal = ecoMan.getBalance(economable);
            ecoMan.setBalance(economable, amount);
            this.saneEconomy.getMessenger().sendMessage(sender, "Balance for {1} set to {2}.", sTargetPlayer, ecoMan.getCurrency().formatAmount(amount));

            saneEconomy.getTransactionLogger().ifPresent((logger) -> {
                // FIXME: This is a silly hack to get it to log.
                if (oldBal > 0.0) {
                    logger.logTransaction(new Transaction(
                            ecoMan.getCurrency(), economable, Economable.CONSOLE, oldBal, TransactionReason.ADMIN_TAKE
                    ));
                }

                logger.logTransaction(new Transaction(
                        ecoMan.getCurrency(), Economable.CONSOLE, economable, amount, TransactionReason.ADMIN_GIVE
                ));
            });

            if (this.saneEconomy.getConfig().getBoolean("economy.notify-admin-set") && targetPlayer.isOnline()) {
                this.saneEconomy.getMessenger().sendMessage((Player) targetPlayer, "{1} has set your balance to {2}.",
                        sender.getName(),
                        ecoMan.getCurrency().formatAmount(amount)

                );
            }

            return;
        }

        throw new InvalidUsageException();
    }
}
