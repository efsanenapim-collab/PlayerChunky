package com.example.logoutitemcleanup;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LogoutItemCleanup extends JavaPlugin implements Listener {

    private double radius;
    private double removePercentage;
    private boolean randomize;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("LogoutItemCleanup enabled.");
    }

    private void loadSettings() {
        radius = Math.max(0.0, getConfig().getDouble("radius", 16.0));
        removePercentage = Math.max(0.0, Math.min(100.0,
                getConfig().getDouble("remove-percentage", 50.0)));
        randomize = getConfig().getBoolean("randomize", true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Location logoutLocation = event.getPlayer().getLocation().clone();
        World world = logoutLocation.getWorld();
        if (world == null || removePercentage <= 0.0) {
            return;
        }

        // PlayerQuitEvent runs while the player is still present, but cleanup
        // is performed from the saved logout position so it remains tied to
        // the exact place they left.
        double radiusSquared = radius * radius;

        List<Item> nearbyItems = new ArrayList<>();
        for (Entity entity : world.getNearbyEntities(logoutLocation, radius, radius, radius)) {
            if (entity instanceof Item item
                    && entity.getLocation().distanceSquared(logoutLocation) <= radiusSquared) {
                nearbyItems.add(item);
            }
        }

        if (nearbyItems.isEmpty()) {
            return;
        }

        if (randomize) {
            Collections.shuffle(nearbyItems);
        }

        int amountToRemove = (int) Math.round(
                nearbyItems.size() * (removePercentage / 100.0)
        );

        for (int i = 0; i < amountToRemove; i++) {
            Item item = nearbyItems.get(i);
            if (!item.isDead() && item.isValid()) {
                item.remove();
            }
        }
    }
}
