package org.appledash.saneeconomysignshop.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by appledash on 10/17/16.
 * Blackjack is best pony.
 */
public class SerializableLocation implements Serializable {
    private double x, y, z;
    private float yaw, pitch;
    private UUID worldUuid;

    public SerializableLocation(Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.worldUuid = location.getWorld().getUID();
    }

    public Location getBukkitLocation() {
        return new Location(Bukkit.getServer().getWorld(worldUuid), x, y, z, yaw, pitch);
    }
}
