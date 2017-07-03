package org.appledash.saneeconomy.command;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.sanelib.command.SaneCommand;
import org.appledash.sanelib.command.exception.CommandException;
import org.appledash.sanelib.command.exception.type.usage.TooManyArgumentsException;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class BalanceTopCommand extends SaneCommand {
    private final SaneEconomy saneEconomy;

    public BalanceTopCommand(SaneEconomy saneEconomy) {
        super(saneEconomy);
        this.saneEconomy = saneEconomy;
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
                this.saneEconomy.getMessenger().sendMessage(sender, "{1} is not a valid number.");
                return;
            }
        }

        int offset = (page - 1) * nPerPage;

        Map<OfflinePlayer, Double> topBalances = saneEconomy.getEconomyManager().getTopPlayerBalances(nPerPage, offset);

        if (topBalances.isEmpty()) {
            this.saneEconomy.getMessenger().sendMessage(sender, "There aren't enough players to display that page.");
            return;
        }

        AtomicInteger index = new AtomicInteger(offset + 1); /* I know it's stupid, but you can't do some_int++ from within the lambda. */

        this.saneEconomy.getMessenger().sendMessage(sender, "Top {1} players on page {2}:", topBalances.size(), page);
        topBalances.forEach((player, balance) -> this.saneEconomy.getMessenger().sendMessage(sender, "[{1:02d}] {2} - {3}", index.getAndIncrement(), player.getName(), SaneEconomy.getInstance().getEconomyManager().getCurrency().formatAmount(balance)));
    }
}
