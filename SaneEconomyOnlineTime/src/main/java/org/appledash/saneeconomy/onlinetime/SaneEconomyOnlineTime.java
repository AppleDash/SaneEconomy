package org.appledash.saneeconomy.onlinetime;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionReason;
import org.appledash.sanelib.SanePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * Created by appledash on 7/13/17.
 * Blackjack is best pony.
 */
public class SaneEconomyOnlineTime extends SanePlugin implements Listener {
    private Map<UUID, Long> onlineSeconds = new HashMap<>();
    private Map<UUID, Double> reportingAmounts = new HashMap<>();
    private List<Payout> payouts = new ArrayList<>();
    private SaneEconomy saneEconomy;

    @Override
    public void onEnable() {
        super.onEnable();
        this.saneEconomy = (SaneEconomy) this.getServer().getPluginManager().getPlugin("SaneEconomy");
        this.saveDefaultConfig();

        this.getConfig().getMapList("payouts").forEach(map -> {
            this.payouts.add(Payout.fromConfigMap(map));
        });

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : this.getServer().getOnlinePlayers()) {
                long onlineSeconds = this.onlineSeconds.getOrDefault(player.getUniqueId(), 0L);

                onlineSeconds++;

                this.onlineSeconds.put(player.getUniqueId(), onlineSeconds);

                for (Payout payout : payouts) {
                    if (payout.getPermission() != null && !player.hasPermission(payout.getPermission())) {
                        continue;
                    }

                    if ((onlineSeconds % payout.getSecondsInterval()) == 0) {
                        if (this.reportingAmounts.containsKey(player.getUniqueId())) {
                            this.reportingAmounts.put(player.getUniqueId(), this.reportingAmounts.get(player.getUniqueId()) + payout.getAmount());
                        } else {
                            this.reportingAmounts.put(player.getUniqueId(), payout.getAmount());
                        }

                        this.saneEconomy.getEconomyManager().transact(new Transaction(this.saneEconomy.getEconomyManager().getCurrency(), Economable.PLUGIN, Economable.wrap(player), payout.getAmount(), TransactionReason.PLUGIN_GIVE));
                    }

                    if ((onlineSeconds % payout.getReportInterval()) == 0) {
                        this.getMessenger().sendMessage(player, payout.getMessage(), this.saneEconomy.getEconomyManager().getCurrency().formatAmount(this.reportingAmounts.getOrDefault(player.getUniqueId(), 0D)), payout.getReportInterval());
                        this.reportingAmounts.put(player.getUniqueId(), 0D);
                    }
                }

            }
        },0, 20);

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {
        if (this.onlineSeconds.containsKey(evt.getPlayer().getUniqueId())) {
            this.onlineSeconds.remove(evt.getPlayer().getUniqueId());
        }

        if (this.reportingAmounts.containsKey(evt.getPlayer().getUniqueId())) {
            this.reportingAmounts.remove(evt.getPlayer().getUniqueId());
        }
    }
}
