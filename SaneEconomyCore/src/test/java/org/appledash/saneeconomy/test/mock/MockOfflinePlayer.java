package org.appledash.saneeconomy.test.mock;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
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
        return this.name;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public PlayerProfile getPlayerProfile() {
        return null;
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public BanEntry<PlayerProfile> ban(String s, Date date, String s1) {
        return null;
    }

    @Override
    public BanEntry<PlayerProfile> ban(String s, Instant instant, String s1) {
        return null;
    }

    @Override
    public BanEntry<PlayerProfile> ban(String s, Duration duration, String s1) {
        return null;
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
    public Location getRespawnLocation() {
        return null;
    }

    @Override
    public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {

    }

    @Override
    public void incrementStatistic(Statistic statistic, int i) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic, int i) throws IllegalArgumentException {

    }

    @Override
    public void setStatistic(Statistic statistic, int i) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(Statistic statistic) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {

    }

    @Override
    public void setStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {

    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType, int i) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType, int i) {

    }

    @Override
    public void setStatistic(Statistic statistic, EntityType entityType, int i) {

    }

    @Override
    public Location getLastDeathLocation() {
        return null;
    }

    @Override
    public Location getLocation() {
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
