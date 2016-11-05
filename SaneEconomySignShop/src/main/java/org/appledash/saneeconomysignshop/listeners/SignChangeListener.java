package org.appledash.saneeconomysignshop.listeners;

import com.google.common.base.Strings;
import net.md_5.bungee.api.ChatColor;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.appledash.saneeconomysignshop.SaneEconomySignShop;
import org.appledash.saneeconomysignshop.signshop.SignShop;
import org.appledash.saneeconomysignshop.util.ItemDatabase;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by appledash on 10/2/16.
 * Blackjack is still best pony.
 */
public class SignChangeListener implements Listener {
    private SaneEconomySignShop plugin;

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
            MessageUtils.sendMessage(evt.getPlayer(), String.format("Cannot create shop: %s", pss.error));
            return;
        }

        if (pss.shop == null) {
            return;
        }

        SignShop signShop = pss.shop;
        plugin.getSignShopManager().addSignShop(signShop);
        evt.setLine(0, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("admin-shop-title")));
        MessageUtils.sendMessage(evt.getPlayer(), "Sign shop created!");
        MessageUtils.sendMessage(evt.getPlayer(), String.format("Item: %d x %s", signShop.getQuantity(), signShop.getItem()));

        if (signShop.canBuy()) { // The player be buying from the shop, not the other way around.
            MessageUtils.sendMessage(evt.getPlayer(), String.format("Will sell too players for %s.",
                    plugin.getSaneEconomy().getEconomyManager().getCurrency().formatAmount(signShop.getBuyPrice())
            ));
        }

        if (signShop.canSell()) { // The player be selling to the shop, not the other way around.
            MessageUtils.sendMessage(evt.getPlayer(), String.format("Will buy from players for %s.",
                    plugin.getSaneEconomy().getEconomyManager().getCurrency().formatAmount(signShop.getSellPrice())
            ));
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
            itemStack = parseGive(itemName);
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

    private ItemStack parseGive(String rawItemName) throws InvalidItemException {
        String itemName;
        short damage;

        if (rawItemName.contains(":")) {
            String[] splitItemName = rawItemName.split(":");
            itemName = splitItemName[0];
            if (splitItemName.length == 1) { // They just typed 'tnt:'
                damage = 0;
            } else { // They typed 'tnt:something'
                try {
                    damage = Short.valueOf(splitItemName[1]);
                } catch (NumberFormatException e) {
                    throw new InvalidItemException("Damage value must be a number.");
                }
            }
        } else { // No damage value
            itemName = rawItemName;
            damage = 0;
        }

        Optional<Material> materialOptional = parseMaterialFromName(itemName);

        if (!materialOptional.isPresent()) {
            Optional<ItemDatabase.Pair<Integer, Short>> parsedItem = ItemDatabase.getIDAndDamageForName(normalizeItemName(itemName));
            if (!parsedItem.isPresent()) {
                throw new InvalidItemException("Item by that name does not exist.");
            }
            return new ItemStack(parsedItem.get().getLeft(), 1, parsedItem.get().getRight());
        }

        return new ItemStack(materialOptional.get(), 1, damage);

    }

    private Optional<Material> parseMaterialFromName(String materialName) {
        // Try to parse an integral item ID first, for legacy reasons.
        try {
            Material mat = Material.getMaterial(Integer.valueOf(materialName));
            return Optional.ofNullable(mat);
        } catch (NumberFormatException ignored) { }

        for (Material mat : Material.values()) {
            if (normalizeItemName(mat.name()).equals(normalizeItemName(materialName))) {
                return Optional.of(mat);
            }
        }

        return Optional.empty();
    }

    private String normalizeItemName(String itemName) {
        return itemName.toLowerCase().replace("_", "").replace(" ", "");
    }

    private class InvalidItemException extends Exception {
        public InvalidItemException(String message) {
            super(message);
        }
    }




}
