package org.appledash.saneeconomy.test.mock;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Created by AppleDash on 7/29/2016.
 * Blackjack is still best pony.
 *
 * Implemented: Name and UUID
 */
public class MockOfflinePlayer implements OfflinePlayer {
    private final UUID uuid;
    private final String name;

    private MockOfflinePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        MockServer.getInstance().addOfflinePlayer(this);
    }

    public MockOfflinePlayer(String name) {
        this(UUID.randomUUID(), name);
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public boolean isWhitelisted() {
        return false;
    }

    @Override
    public void setWhitelisted(boolean value) {

    }

    @Override
    public Player getPlayer() {
        return null;
    }

    @Override
    public long getFirstPlayed() {
        return 0;
    }

    @Override
    public long getLastPlayed() {
        return 0;
    }

    @Override
    public boolean hasPlayedBefore() {
        return false;
    }

    @Override
    public Location getBedSpawnLocation() {
        return null;
    }

    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean value) {

    }
}
