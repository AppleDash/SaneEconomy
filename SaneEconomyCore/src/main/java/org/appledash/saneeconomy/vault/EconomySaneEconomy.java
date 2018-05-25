package org.appledash.saneeconomy.vault;

import com.google.common.collect.ImmutableList;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionReason;
import org.appledash.saneeconomy.economy.transaction.TransactionResult;
import org.appledash.saneeconomy.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;

import java.util.List;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class EconomySaneEconomy implements Economy {
    @Override
    public boolean isEnabled() {
        return SaneEconomy.getInstance().isEnabled();
    }

    @Override
    public String getName() {
        return "SaneEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return SaneEconomy.getInstance().getEconomyManager().getCurrency().getFormat().getMaximumFractionDigits();
    }

    @Override
    public String format(double v) {
        return SaneEconomy.getInstance().getEconomyManager().getCurrency().formatAmount(v);
    }

    @Override
    public String currencyNamePlural() {
        return SaneEconomy.getInstance().getEconomyManager().getCurrency().getPluralName();
    }

    @Override
    public String currencyNameSingular() {
        return SaneEconomy.getInstance().getEconomyManager().getCurrency().getSingularName();
    }

    @Override
    public boolean hasAccount(String target) {
        return SaneEconomy.getInstance().getEconomyManager().accountExists(makeEconomable(target));
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return SaneEconomy.getInstance().getEconomyManager().accountExists(Economable.wrap(offlinePlayer));
    }

    @Override
    public boolean hasAccount(String target, String worldName) {
        return hasAccount(target);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String worldName) {
        return hasAccount(offlinePlayer);
    }

    @Override
    public double getBalance(String target) {
        return SaneEconomy.getInstance().getEconomyManager().getBalance(makeEconomable(target));
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return SaneEconomy.getInstance().getEconomyManager().getBalance(Economable.wrap(offlinePlayer));
    }

    @Override
    public double getBalance(String target, String worldName) {
        return getBalance(target);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String worldName) {
        return getBalance(offlinePlayer);
    }

    @Override
    public boolean has(String target, double amount) {
        return SaneEconomy.getInstance().getEconomyManager().hasBalance(makeEconomable(target), amount);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double amount) {
        return SaneEconomy.getInstance().getEconomyManager().hasBalance(Economable.wrap(offlinePlayer), amount);
    }

    @Override
    public boolean has(String target, String worldName, double amount) {
        return has(target, amount);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String worldName, double amount) {
        return has(offlinePlayer, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String target, double amount) {
        if (amount == 0) {
            return new EconomyResponse(amount, getBalance(target), EconomyResponse.ResponseType.SUCCESS, "");
        }

        return transact(new Transaction(
                SaneEconomy.getInstance().getEconomyManager().getCurrency(), makeEconomable(target), Economable.PLUGIN, amount, TransactionReason.PLUGIN_TAKE
        ));
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        if (amount == 0) {
            return new EconomyResponse(amount, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "");
        }

        if (!has(offlinePlayer, amount)) {
            return new EconomyResponse(amount, getBalance(offlinePlayer), EconomyResponse.ResponseType.FAILURE, "Insufficient funds.");
        }

        return transact(new Transaction(
                SaneEconomy.getInstance().getEconomyManager().getCurrency(), Economable.wrap(offlinePlayer), Economable.PLUGIN, amount, TransactionReason.PLUGIN_TAKE
        ));
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double v) {
        return withdrawPlayer(playerName, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return withdrawPlayer(offlinePlayer, v);
    }

    @Override
    public EconomyResponse depositPlayer(String target, double amount) {
        if (amount == 0) {
            return new EconomyResponse(amount, getBalance(target), EconomyResponse.ResponseType.SUCCESS, "");
        }

        return transact(new Transaction(
                SaneEconomy.getInstance().getEconomyManager().getCurrency(), Economable.PLUGIN, makeEconomable(target), amount, TransactionReason.PLUGIN_GIVE
        ));
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double v) {
        if (v == 0) {
            return new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "");
        }

        return transact(new Transaction(
                SaneEconomy.getInstance().getEconomyManager().getCurrency(), Economable.PLUGIN, Economable.wrap(offlinePlayer), v, TransactionReason.PLUGIN_GIVE
        ));
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double v) {
        return depositPlayer(playerName, v);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String worldName, double v) {
        return depositPlayer(offlinePlayer, v);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }

    @Override
    public List<String> getBanks() {
        return ImmutableList.of();
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return true;
    }

    private boolean validatePlayer(String playerName) {
        return PlayerUtils.getOfflinePlayer(playerName) != null;
    }

    private Economable makeEconomable(String input) {
        if (input.equals(SaneEconomy.getInstance().getEconomyManager().getServerAccountName())) {
            return Economable.CONSOLE;
        }

        if (validatePlayer(input)) {
            return Economable.wrap(PlayerUtils.getOfflinePlayer(input));
        }

        return Economable.wrap(input);
    }

    private EconomyResponse transact(Transaction transaction) {
        TransactionResult result = SaneEconomy.getInstance().getEconomyManager().transact(transaction);

        if (result.getStatus() == TransactionResult.Status.SUCCESS) {
            return new EconomyResponse(transaction.getAmount(), result.getToBalance(), EconomyResponse.ResponseType.SUCCESS, null);
        }

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, result.getStatus().toString());
    }
}
