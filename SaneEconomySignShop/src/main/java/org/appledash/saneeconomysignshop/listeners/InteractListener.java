package org.appledash.saneeconomysignshop.listeners;

import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionReason;
import org.appledash.saneeconomy.economy.transaction.TransactionResult;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.appledash.saneeconomysignshop.SaneEconomySignShop;
import org.appledash.saneeconomysignshop.signshop.SignShop;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Created by appledash on 10/3/16.
 * Blackjack is best pony.
 */
public class InteractListener implements Listener {
    private final SaneEconomySignShop plugin;

    public InteractListener(SaneEconomySignShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {
        if (!evt.getPlayer().hasPermission("saneeconomy.signshop.use")) {
            return;
        }

        if (evt.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if ((evt.getPlayer().getInventory().getItemInMainHand() != null) && (evt.getPlayer().getInventory().getItemInMainHand().getType() == Material.DIAMOND_AXE)) {
            return;
        }

        if ((evt.getAction() != Action.RIGHT_CLICK_BLOCK) && (evt.getAction() != Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        Optional<SignShop> shopOptional = plugin.getSignShopManager().getSignShop(evt.getClickedBlock().getLocation());

        if (!shopOptional.isPresent()) {
            return;
        }

        SignShop shop = shopOptional.get();

        // Buy
        if (evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
            evt.setCancelled(true);
            if (!shop.canBuy()) {
                MessageUtils.sendMessage(evt.getPlayer(), "This shop does not permit buying.");
                return;
            }

            doBuy(shop, evt.getPlayer());
        }

        // Sell
        if (evt.getAction() == Action.LEFT_CLICK_BLOCK) {
            evt.setCancelled(true);
            if (!shop.canSell()) {
                MessageUtils.sendMessage(evt.getPlayer(), "This shop does not permit selling.");
                return;
            }

            doSell(shop, evt.getPlayer());
        }
    }

    private void doBuy(SignShop shop, Player player) {
        EconomyManager ecoMan = plugin.getSaneEconomy().getEconomyManager();
        int quantity = player.isSneaking() ? 1 : shop.getQuantity();
        double price = shop.getBuyPrice(quantity);

        if (!ecoMan.hasBalance(Economable.wrap(player), price)) {
            MessageUtils.sendMessage(player, "You do not have enough money to buy {1} {2}.", quantity, shop.getItem());
            return;
        }

        TransactionResult result = ecoMan.transact(new Transaction(Economable.wrap(player), Economable.PLUGIN, price, TransactionReason.PLUGIN_TAKE));

        if (result.getStatus() != TransactionResult.Status.SUCCESS) {
            MessageUtils.sendMessage(player, "An error occurred attempting to perform that transaction: {1}", result.getStatus());
            return;
        }

        ItemStack stack = shop.getItem().clone();
        stack.setAmount(quantity);

        player.getInventory().addItem(stack);
        MessageUtils.sendMessage(player, "You have bought {1} {2} for {3}.", quantity, shop.getItem(), ecoMan.getCurrency().formatAmount(price));
    }

    private void doSell(SignShop shop, Player player) { // TODO: Selling enchanted items
        EconomyManager ecoMan = plugin.getSaneEconomy().getEconomyManager();
        int quantity = player.isSneaking() ? 1 : shop.getQuantity();
        double price = shop.getSellPrice(quantity);

        if (!player.getInventory().containsAtLeast(new ItemStack(shop.getItem()), quantity)) {
            MessageUtils.sendMessage(player, "You do not have {1} {2}!", quantity, shop.getItem());
            return;
        }

        ItemStack stack = shop.getItem().clone();
        stack.setAmount(quantity);

        player.getInventory().removeItem(stack); // FIXME: This does not remove items with damage values that were detected by contains()
        ecoMan.transact(new Transaction(Economable.PLUGIN, Economable.wrap(player), price, TransactionReason.PLUGIN_GIVE));
        MessageUtils.sendMessage(player, "You have sold {1} {2} for {3}.", quantity, shop.getItem(), ecoMan.getCurrency().formatAmount(price));
    }
}
