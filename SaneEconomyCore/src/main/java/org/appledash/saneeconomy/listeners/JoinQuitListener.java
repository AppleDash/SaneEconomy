package org.appledash.saneeconomy.listeners;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionReason;
import org.appledash.saneeconomy.updates.GithubVersionChecker;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class JoinQuitListener implements Listener {
    private final SaneEconomy plugin;

    public JoinQuitListener(SaneEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player player = evt.getPlayer();
        Economable economable = Economable.wrap((OfflinePlayer) player);
        double startBalance = plugin.getConfig().getDouble("economy.start-balance", 0.0D);

        /* A starting balance is configured AND they haven't been given it yet. */
        if ((startBalance > 0) && !plugin.getEconomyManager().accountExists(economable)) {
            plugin.getEconomyManager().transact(new Transaction(
                    Economable.CONSOLE, economable, startBalance, TransactionReason.STARTING_BALANCE
            ));
            MessageUtils.sendMessage(player, "You've been issued a starting balance of %s!", plugin.getEconomyManager().getCurrency().formatAmount(startBalance));
        }

        /* Update notification */
        if (player.hasPermission("saneeconomy.update-notify") && plugin.getVersionChecker().isUpdateAvailable()) {
            MessageUtils.sendMessage(player, "An update is available! The currently-installed version is %s, but the newest available is %s. Please go to %s to update!", plugin.getDescription().getVersion(), plugin.getVersionChecker().getNewestVersion(), GithubVersionChecker.DOWNLOAD_URL);
        }
    }

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent evt) {
        plugin.getEconomyManager().getBackend().reloadDatabase(); // TODO: If servers start to lag when lots of people join, this is why.
    }
}
