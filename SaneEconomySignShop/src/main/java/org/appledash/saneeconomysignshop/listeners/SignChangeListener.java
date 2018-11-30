package org.appledash.saneeconomysignshop.listeners;

import com.google.common.base.Strings;
import org.appledash.saneeconomysignshop.SaneEconomySignShop;
import org.appledash.saneeconomysignshop.signshop.SignShop;
import org.appledash.saneeconomysignshop.util.ItemDatabase;
import org.appledash.saneeconomysignshop.util.ItemDatabase.InvalidItemException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by appledash on 10/2/16.
 * Blackjack is still best pony.
 */
public class SignChangeListener implements Listener {
    private final SaneEconomySignShop plugin;

    public SignChangeListener(SaneEconomySignShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent evt) {
        if (!evt.getPlayer().hasPermission("saneeconomy.signshop.create.admin")) {
            return;
        }

        ParsedSignShop pss = parseSignShop(evt);

        if (pss.error != null) {
            this.plugin.getMessenger().sendMessage(evt.getPlayer(), "Cannot create shop: {1}", pss.error);
            return;
        }

        if (pss.shop == null) {
            return;
        }

        SignShop signShop = pss.shop;
        plugin.getSignShopManager().addSignShop(signShop);
        evt.setLine(0, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("admin-shop-title")));
        this.plugin.getMessenger().sendMessage(evt.getPlayer(), "Sign shop created!");
        this.plugin.getMessenger().sendMessage(evt.getPlayer(), "Item: {1} x {2}", signShop.getQuantity(), signShop.getItemStack());

        if (signShop.canBuy()) { // The player be buying from the shop, not the other way around.
            this.plugin.getMessenger().sendMessage(evt.getPlayer(), "Will sell to players for {1}.",
                    plugin.getSaneEconomy().getEconomyManager().getCurrency().formatAmount(signShop.getBuyPrice())
            );
        }

        if (signShop.canSell()) { // The player be selling to the shop, not the other way around.
            this.plugin.getMessenger().sendMessage(evt.getPlayer(), "Will buy from players for {1}.",
                    plugin.getSaneEconomy().getEconomyManager().getCurrency().formatAmount(signShop.getSellPrice())
            );
        }
    }

    private ParsedSignShop parseSignShop(SignChangeEvent evt) {
        String[] lines = evt.getLines();
        Player player = evt.getPlayer();
        Location location = evt.getBlock().getLocation();

        if ((lines[0] == null) || !lines[0].equalsIgnoreCase(plugin.getConfig().getString("admin-shop-trigger"))) { // First line must contain the trigger
            return new ParsedSignShop();
        }

        if (Strings.isNullOrEmpty(lines[1])) { // Second line must contain an item name
            return new ParsedSignShop("No item name specified.");
        }

        if (Strings.isNullOrEmpty(lines[2])) { // Second line must contain buy/sell prices
            return new ParsedSignShop("No buy/sell price(s) specified.");
        }

        if (Strings.isNullOrEmpty(lines[3])) { // Third line must contain item amount.
            return new ParsedSignShop("No item amount specified.");
        }

        String itemName = lines[1];
        String buySellRaw = lines[2];
        String amountRaw = lines[3];

        ItemStack itemStack;
        try {
            itemStack = ItemDatabase.parseGive(itemName);
        } catch (InvalidItemException e) {
            return new ParsedSignShop("Invalid item name or ID specified.");
        }

        Matcher m = Pattern.compile("(B:(?<buy>[0-9.]+))?[ ]*(S:(?<sell>[0-9.]+))?").matcher(buySellRaw.trim());

        if (!m.matches()) {
            return new ParsedSignShop("Invalid buy/sell prices specified.");
        }

        double buy = Strings.isNullOrEmpty(m.group("buy")) ? -1.0 : Double.valueOf(m.group("buy"));
        double sell = Strings.isNullOrEmpty(m.group("sell")) ? -1.0 : Double.valueOf(m.group("sell"));

        if ((buy == -1) && (sell == -1)) {
            return new ParsedSignShop("Buy and sell amounts for this shop are both invalid.");
        }

        int itemAmount;

        try {
            itemAmount = Integer.valueOf(amountRaw);

            if (itemAmount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            return new ParsedSignShop("Item amount is not a positive integer.");
        }

        return new ParsedSignShop(new SignShop(player.getUniqueId(), location, itemStack, itemAmount, buy, sell));
    }

    private class ParsedSignShop {
        private SignShop shop;
        private String error;

        private ParsedSignShop(String error) {
            this.error = error;
        }

        private ParsedSignShop() {

        }

        private ParsedSignShop(SignShop shop) {
            this.shop = shop;
        }
    }
}
