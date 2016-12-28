package org.appledash.saneeconomymobkills.listeners;

import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionReason;
import org.appledash.saneeconomy.utils.MessageUtils;
import org.appledash.saneeconomymobkills.SaneEconomyMobKills;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by appledash on 12/27/16.
 * Blackjack is still best pony.
 */
public class EntityDamageListener implements Listener {
    private SaneEconomyMobKills plugin;
    private Map<Integer, Map<UUID, Double>> damageDealt = new HashMap<>();

    public EntityDamageListener(SaneEconomyMobKills plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent evt) {
        if (!(evt.getDamager() instanceof Player)) {
            return;
        }

        Player damager = ((Player) evt.getDamager());
        Entity damagee = evt.getEntity();

        if (!plugin.getKillAmounts().containsKey(getEntityType(damagee))) {
            return;
        }

        Map<UUID, Double> damageDoneToThisEntity = new HashMap<>();

        if (damageDealt.containsKey(damagee.getEntityId())) {
            damageDoneToThisEntity = damageDealt.get(damagee.getEntityId());
        } else {
            damageDealt.put(damagee.getEntityId(), damageDoneToThisEntity);
        }

        double totalDamageDealt = 0;

        if (damageDoneToThisEntity.containsKey(damager.getUniqueId())) {
            totalDamageDealt += damageDoneToThisEntity.get(damager.getUniqueId());
        }

        totalDamageDealt += evt.getDamage();

        damageDoneToThisEntity.put(damager.getUniqueId(), totalDamageDealt);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent evt) {
        Entity entity = evt.getEntity();

        if (!damageDealt.containsKey(entity.getEntityId())) {
            return;
        }

        Map<UUID, Double> damageDoneToThisEntity = damageDealt.get(entity.getEntityId());
        double totalDmg = sumValues(damageDoneToThisEntity);

        for (Map.Entry<UUID, Double> entry : damageDoneToThisEntity.entrySet()) {
            double thisDmg = entry.getValue();
            double thisPercent = (thisDmg / totalDmg) * 100.0D;
            double thisAmount = plugin.getKillAmounts().get(getEntityType(entity)) * (thisPercent / 100);
            OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(entry.getKey());

            if (offlinePlayer.isOnline()) {
                Player player = Bukkit.getServer().getPlayer(offlinePlayer.getUniqueId());
                MessageUtils.sendMessage(player, "You have been awarded {0} for doing {1}% of the damage required to kill that {2}!", plugin.getSaneEconomy().getEconomyManager().getCurrency().formatAmount(thisAmount), thisPercent, entity.getName());
            }

            plugin.getSaneEconomy().getEconomyManager().transact(new Transaction(
                    Economable.PLUGIN, Economable.wrap(offlinePlayer), thisAmount, TransactionReason.PLUGIN_GIVE
            ));
        }

    }

    private String getEntityType(Entity entity) {
        EntityType entityType = entity.getType();

        if ((entityType == EntityType.SKELETON) && (((Skeleton) entity).getSkeletonType() == Skeleton.SkeletonType.WITHER)) {
            return "WITHER_SKELETON";
        }

        if ((entityType == EntityType.GUARDIAN) && ((Guardian) entity).isElder()) {
            return "ELDER_GUARDIAN";
        }

        return entityType.toString();
    }

    private double sumValues(Map<?, Double> map) {
        double sum = 0;

        for (Map.Entry<?, Double> entry : map.entrySet()) {
            sum += entry.getValue();
        }

        return sum;
    }
}
