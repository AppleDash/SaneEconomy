package org.appledash.saneeconomysignshop.listeners;

import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionReason;
import org.appledash.saneeconomy.economy.transaction.TransactionResult;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.appledash.saneeconomysignshop.SaneEconomySignShop;
import org.appledash.saneeconomysignshop.signshop.SignShop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
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

        Optional<SignShop> shopOptional = plugin.getSignShopManager().getSignShop(evt.getClickedBlock().getLocation());

        if (!shopOptional.isPresent()) {
            return;
        }

        SignShop shop = shopOptional.get();

        // Buy
        if (evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!shop.canBuy()) {
                MessageUtils.sendMessage(evt.getPlayer(), "This shop does not permit buying.");
                return;
            }

            doBuy(shop, evt.getPlayer());
        }

        // Sell
        if (evt.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (!shop.canSell()) {
                MessageUtils.sendMessage(evt.getPlayer(), "This shop does not permit selling.");
                return;
            }

            doSell(shop, evt.getPlayer());
        }
    }

    private void doBuy(SignShop shop, Player player) {
        EconomyManager ecoMan = plugin.getSaneEconomy().getEconomyManager();

        if (!ecoMan.hasBalance(Economable.wrap(player), shop.getBuyAmount())) {
            MessageUtils.sendMessage(player, String.format("You do not have enough money to buy %d %s.", shop.getQuantity(), shop.getItem()));
            return;
        }

        TransactionResult result = ecoMan.transact(new Transaction(Economable.wrap(player), Economable.PLUGIN, shop.getBuyAmount(), TransactionReason.PLUGIN_TAKE));

        if (result.getStatus() != TransactionResult.Status.SUCCESS) {
            MessageUtils.sendMessage(player, String.format("An error occurred attempting to perform that transaction: %s", result.getStatus()));
            return;
        }

        player.getInventory().addItem(new ItemStack(shop.getItem(), shop.getQuantity()));
        MessageUtils.sendMessage(player, String.format("You have bought %d %s for %s.", shop.getQuantity(), shop.getItem(), ecoMan.getCurrency().formatAmount(shop.getBuyAmount())));
    }

    private void doSell(SignShop shop, Player player) {
        EconomyManager ecoMan = plugin.getSaneEconomy().getEconomyManager();

        if (!player.getInventory().contains(new ItemStack(shop.getItem(), shop.getQuantity()))) {
            MessageUtils.sendMessage(player, String.format("You do not have %d %s!", shop.getQuantity(), shop.getItem()));
            return;
        }

        player.getInventory().remove(new ItemStack(shop.getItem(), shop.getQuantity()));
        ecoMan.transact(new Transaction(Economable.PLUGIN, Economable.wrap(player), shop.getSellAmount(), TransactionReason.PLUGIN_GIVE));
        MessageUtils.sendMessage(player, String.format("You have sold %d %s for %s.", shop.getQuantity(), shop.getItem(), ecoMan.getCurrency().formatAmount(shop.getSellAmount())));
    }
}
