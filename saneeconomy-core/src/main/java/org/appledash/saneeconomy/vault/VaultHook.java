package org.appledash.saneeconomy.vault;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.appledash.saneeconomy.SaneEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class VaultHook {
    private final SaneEconomy plugin;
    private final Economy provider = new EconomySaneEconomy();

    public VaultHook(SaneEconomy plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        Bukkit.getServicesManager().register(Economy.class, provider, plugin, ServicePriority.Normal);
    }

    public void unhook() {
        Bukkit.getServicesManager().unregister(Economy.class, provider);
    }

    public boolean hasPermission(OfflinePlayer offlinePlayer, String permNode) {
        RegisteredServiceProvider<Permission> rsp = this.plugin.getServer().getServicesManager().getRegistration(Permission.class);

        return rsp != null && rsp.getProvider().playerHas(null, offlinePlayer, permNode);
    }
}
