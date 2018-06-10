package org.appledash.saneeconomysignshop.listeners;

import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionResult;
import org.appledash.saneeconomysignshop.SaneEconomySignShop;
import org.appledash.saneeconomysignshop.signshop.ShopTransaction;
import org.appledash.saneeconomysignshop.signshop.ShopTransaction.TransactionDirection;
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
import java.util.logging.Logger;

/**
 * Created by appledash on 10/3/16.
 * Blackjack is best pony.
 */
public class InteractListener implements Listener {
    private static final Logger LOGGER = Logger.getLogger("SignShop");
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
                this.plugin.getMessenger().sendMessage(evt.getPlayer(), "This shop does not permit buying.");
                return;
            }

            doBuy(shop, evt.getPlayer());
        }

        // Sell
        if (evt.getAction() == Action.LEFT_CLICK_BLOCK) {
            evt.setCancelled(true);
            if (!shop.canSell()) {
                this.plugin.getMessenger().sendMessage(evt.getPlayer(), "This shop does not permit selling.");
                return;
            }

            doSell(shop, evt.getPlayer());
        }
    }

    private void doBuy(SignShop shop, Player player) {
        EconomyManager ecoMan = plugin.getSaneEconomy().getEconomyManager();
        int quantity = player.isSneaking() ? 1 : shop.getQuantity();

        ShopTransaction shopTransaction = shop.makeTransaction(ecoMan.getCurrency(), player, TransactionDirection.BUY, quantity);

        /* No buy limits for now!
        if (!plugin.getLimitManager().shouldAllowTransaction(shopTransaction)) {
            MessageUtils.sendMessage(player, "You have reached your buying limit for the time being. Try back in an hour or so.");
            return;
        }

        plugin.getLimitManager().setRemainingLimit(player, ShopTransaction.TransactionDirection.BUY, shop.getItem(), plugin.getLimitManager().getRemainingLimit(player, ShopTransaction.TransactionDirection.BUY, shop.getItem()) - quantity);
        */

        Transaction ecoTransaction = shopTransaction.makeEconomyTransaction();
        TransactionResult result = ecoMan.transact(ecoTransaction);

        if (result.getStatus() != TransactionResult.Status.SUCCESS) {
            this.plugin.getMessenger().sendMessage(player, "An error occurred attempting to perform that transaction: {1}", result.getStatus());
            return;
        }

        ItemStack stack = shop.getItemStack().clone();
        stack.setAmount(quantity);
        player.getInventory().addItem(stack);

        this.plugin.getMessenger().sendMessage(player, "You have bought {1} {2} for {3}.", quantity, shop.getItemStack().getType().name(), ecoMan.getCurrency().formatAmount(shopTransaction.getPrice()));
        LOGGER.info(String.format("%s just bought %d %s for %s.", player.getName(), quantity, shop.getItemStack().getType().name(), ecoMan.getCurrency().formatAmount(shopTransaction.getPrice())));
    }

    private void doSell(SignShop shop, Player player) { // TODO: Selling enchanted items
        EconomyManager ecoMan = plugin.getSaneEconomy().getEconomyManager();
        int quantity = player.isSneaking() ? 1 : shop.getQuantity();
        double price = shop.getSellPrice(quantity);

        if (!player.getInventory().containsAtLeast(new ItemStack(shop.getItemStack()), quantity)) {
            this.plugin.getMessenger().sendMessage(player, "You do not have {1} {2}!", quantity, shop.getItemStack().getType().name());
            return;
        }

        ShopTransaction shopTransaction = shop.makeTransaction(ecoMan.getCurrency(), player, TransactionDirection.SELL, quantity);

        if (!plugin.getLimitManager().shouldAllowTransaction(shopTransaction)) {
            this.plugin.getMessenger().sendMessage(player, "You have reached your selling limit for the time being. Try back in an hour or so.");
            return;
        }

        plugin.getLimitManager().setRemainingLimit(player, TransactionDirection.SELL, shop.getItem(), plugin.getLimitManager().getRemainingLimit(player, TransactionDirection.SELL, shop.getItem()) - quantity);


        ItemStack stack = shop.getItemStack().clone();
        stack.setAmount(quantity);
        player.getInventory().removeItem(stack); // FIXME: This does not remove items with damage values that were detected by contains()

        ecoMan.transact(shopTransaction.makeEconomyTransaction());

        this.plugin.getMessenger().sendMessage(player, "You have sold {1} {2} for {3}.", quantity, shop.getItemStack().getType().name(), ecoMan.getCurrency().formatAmount(price));
        LOGGER.info(String.format("%s just sold %d %s for %s.", player.getName(), quantity, shop.getItemStack().getType().name(), ecoMan.getCurrency().formatAmount(price)));
    }

}
