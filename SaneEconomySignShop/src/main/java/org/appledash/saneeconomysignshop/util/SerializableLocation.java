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
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final UUID worldUuid;

    public SerializableLocation(Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.worldUuid = location.getWorld().getUID();
    }

    public Location getBukkitLocation() {
        return new Location(Bukkit.getServer().getWorld(this.worldUuid), this.x, this.y, this.z, this.yaw, this.pitch);
    }
}
