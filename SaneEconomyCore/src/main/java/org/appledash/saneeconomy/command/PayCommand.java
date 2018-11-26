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
import org.appledash.sanelib.command.exception.type.usage.NeedPlayerException;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class PayCommand extends SaneCommand {
    private final SaneEconomy saneEconomy;

    public PayCommand(SaneEconomy saneEconomy) {
        super(saneEconomy.getPlugin());
        this.saneEconomy = saneEconomy;
    }

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

        EconomyManager ecoMan = saneEconomy.getEconomyManager();
        Player fromPlayer = (Player) sender;

        String sToPlayer = args[0];
        OfflinePlayer toPlayer = PlayerUtils.getOfflinePlayer(sToPlayer);

        if (toPlayer == null) {
            this.saneEconomy.getPlugin().getMessenger().sendMessage(sender, "That player does not exist or has never played before.");
            return;
        }

        if (toPlayer.getUniqueId().equals(fromPlayer.getUniqueId())) {
            this.saneEconomy.getPlugin().getMessenger().sendMessage(sender, "You cannot pay yourself.");
            return;
        }

        String sAmount = args[1];
        double amount = NumberUtils.parseAndFilter(ecoMan.getCurrency(), sAmount);

        if (amount <= 0) {
            this.saneEconomy.getPlugin().getMessenger().sendMessage(sender, "{1} is not a positive number.", ((amount == -1) ? sAmount : String.valueOf(amount)));
            return;
        }

        /* Perform the actual transfer. False == They didn't have enough money */
        Transaction transaction = new Transaction(ecoMan.getCurrency(), Economable.wrap(fromPlayer), Economable.wrap(toPlayer), amount, TransactionReason.PLAYER_PAY);
        TransactionResult result = ecoMan.transact(transaction);

        if (result.getStatus() != TransactionResult.Status.SUCCESS) {
            this.saneEconomy.getPlugin().getMessenger().sendMessage(sender, "You do not have enough money to transfer {1} to {2}.",
                    ecoMan.getCurrency().formatAmount(amount),
                    sToPlayer
            );

            return;
        }

        /* Inform the relevant parties. */

        this.saneEconomy.getPlugin().getMessenger().sendMessage(sender, "You have transferred {1} to {2}.",
                ecoMan.getCurrency().formatAmount(amount),
                sToPlayer
        );

        if (toPlayer.isOnline()) {
            this.saneEconomy.getPlugin().getMessenger().sendMessage(((CommandSender) toPlayer), "You have received {1} from {2}.",
                    ecoMan.getCurrency().formatAmount(amount),
                    fromPlayer.getDisplayName()
            );
        }
    }
}
