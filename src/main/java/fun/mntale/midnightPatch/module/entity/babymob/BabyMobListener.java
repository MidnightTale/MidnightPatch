package fun.mntale.midnightPatch.module.entity.babymob;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BabyMobListener implements Listener {
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Squid squid) {
            BabyMobHandler.handleBabySquidSpawn(squid);
        }
        if (entity instanceof Dolphin dolphin) {
            BabyMobHandler.handleBabyDolphinSpawn(dolphin);
        }
    }
} 