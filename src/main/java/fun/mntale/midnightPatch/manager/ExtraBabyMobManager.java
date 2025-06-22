package fun.mntale.midnightPatch.entity;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.ThreadLocalRandom;

@NullMarked
public class ExtraBabyMobManager implements Listener {
    
    // Configuration
    private final double babySquidChance = 0.15; // 15% chance for baby squid
    private final double babyDolphinChance = 0.20; // 20% chance for baby dolphin
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return; // Only handle natural spawns
        }
        
        Entity entity = event.getEntity();
        
        // Handle baby squid spawning
        if (entity instanceof Squid squid) {
            handleBabySquidSpawn(squid);
        }
        
        // Handle baby dolphin spawning
        if (entity instanceof Dolphin dolphin) {
            handleBabyDolphinSpawn(dolphin);
        }
    }
    
    private void handleBabySquidSpawn(Squid squid) {
        // Check if we should make it a baby squid
        if (ThreadLocalRandom.current().nextDouble() < babySquidChance) {
   
            // Schedule task to modify the entity after spawn
            FoliaScheduler.getEntityScheduler().run(squid,
                fun.mntale.midnightPatch.MidnightPatch.instance,
                task -> {                    
                                        double scale = 0.3 + (ThreadLocalRandom.current().nextDouble() * 0.4); // Random scale between 0.3 and 0.7
                    AttributeInstance scaleAttr = squid.getAttribute(Attribute.SCALE);
                    if (scaleAttr != null) {
                        scaleAttr.setBaseValue(scale);
                    }
                },null
            );
        }
    }
    
    private void handleBabyDolphinSpawn(Dolphin dolphin) {
        // Check if we should make it a baby dolphin
        if (ThreadLocalRandom.current().nextDouble() < babyDolphinChance) {
            // Schedule task to modify the entity after spawn
            FoliaScheduler.getEntityScheduler().run(dolphin,
                fun.mntale.midnightPatch.MidnightPatch.instance,
                task -> {
                    double scale = 0.4 + (ThreadLocalRandom.current().nextDouble() * 0.4); // Random scale between 0.4 and 0.8
                    AttributeInstance scaleAttr = dolphin.getAttribute(Attribute.SCALE);
                    if (scaleAttr != null) {
                        scaleAttr.setBaseValue(scale);
                    }
                },null
            );
        }
    }
    
} 