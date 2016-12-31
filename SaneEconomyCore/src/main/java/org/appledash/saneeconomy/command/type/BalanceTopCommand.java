package org.appledash.saneeconomy.command.type;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.command.SaneEconomyCommand;
import org.appledash.saneeconomy.command.exception.CommandException;
import org.appledash.saneeconomy.command.exception.type.usage.TooManyArgumentsException;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class BalanceTopCommand extends SaneEconomyCommand {
    public BalanceTopCommand(SaneEconomy saneEconomy) {
        super(saneEconomy);
    }

    @Override
    public String getPermission() {
        return "saneeconomy.balancetop";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "/<command>",
                "/<command> <page>"
        };
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) throws CommandException {
        if (args.length > 1) {
            throw new TooManyArgumentsException();
        }

        int nPerPage = 10;
        int page = 1;

        if (args.length == 1) {
            try {
                page = Math.abs(Integer.parseInt(args[0]));
            } catch (NumberFormatException e) {
                MessageUtils.sendMessage(sender, "{1} is not a valid number.");
                return;
            }
        }

        int offset = (page - 1) * nPerPage;

        Map<OfflinePlayer, Double> topBalances = saneEconomy.getEconomyManager().getTopPlayerBalances(nPerPage, offset);

        if (topBalances.isEmpty()) {
            MessageUtils.sendMessage(sender, "There aren't enough players to display that page.");
            return;
        }

        AtomicInteger index = new AtomicInteger(offset + 1); /* I know it's stupid, but you can't do some_int++ from within the lambda. */

        MessageUtils.sendMessage(sender, "Top {1} players on page {2}:", topBalances.size(), page);
        topBalances.forEach((player, balance) -> MessageUtils.sendMessage(sender, "[{1:02d}] {2} - {3}", index.getAndIncrement(), player.getName(), SaneEconomy.getInstance().getEconomyManager().getCurrency().formatAmount(balance)));
    }
}
