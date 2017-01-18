package org.appledash.saneeconomysignshop.listeners;

import org.appledash.saneeconomy.utils.MessageUtils;
import org.appledash.saneeconomysignshop.SaneEconomySignShop;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Created by appledash on 10/16/16.
 * Blackjack is best pony.
 */
public class BreakListener implements Listener {
    private final SaneEconomySignShop plugin;

    public BreakListener(SaneEconomySignShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent evt) {
        plugin.getSignShopManager().getSignShop(evt.getBlock().getLocation()).ifPresent((shop) -> {
            if (!evt.getPlayer().hasPermission("saneeconomy.signshop.destroy.admin")) {
                MessageUtils.sendMessage(evt.getPlayer(), "You may not destroy that!");
                evt.setCancelled(true);
                return;
            }

            plugin.getSignShopManager().removeSignShop(shop);
            MessageUtils.sendMessage(evt.getPlayer(), "Sign shop destroyed!");
        });
    }
}
