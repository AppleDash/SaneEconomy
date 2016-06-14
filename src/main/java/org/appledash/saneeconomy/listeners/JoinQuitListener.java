package org.appledash.saneeconomy.listeners;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class JoinQuitListener implements Listener {
    private SaneEconomy plugin;

    public JoinQuitListener(SaneEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player player = evt.getPlayer();
        double startBalance = plugin.getConfig().getDouble("economy.start-balance", 0.0D);
        /* A starting balance is configured AND they haven't been given it yet. */
        if (startBalance > 0 && !plugin.getEconomyManager().accountExists(player)) {
            plugin.getEconomyManager().setBalance(player, startBalance);
            MessageUtils.sendMessage(player, "You've been issued a starting balance of %s!", plugin.getEconomyManager().getCurrency().formatAmount(startBalance));
        }
    }
}
