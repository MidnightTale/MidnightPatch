package fun.mntale.midnightPatch.module.entity.babymob;

import org.bukkit.entity.Squid;
import org.bukkit.entity.Dolphin;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import java.util.concurrent.ThreadLocalRandom;
import fun.mntale.midnightPatch.MidnightPatch;

public class BabyMobHandler {
    public static void handleBabySquidSpawn(Squid squid) {
        if (ThreadLocalRandom.current().nextDouble() < BabyMobConfig.BABY_SQUID_CHANCE) {
            FoliaScheduler.getEntityScheduler().run(squid,
                MidnightPatch.instance,
                task -> {
                    double scale = 0.3 + (ThreadLocalRandom.current().nextDouble() * 0.4); // Random scale between 0.3 and 0.7
                    AttributeInstance scaleAttr = squid.getAttribute(Attribute.SCALE);
                    if (scaleAttr != null) {
                        scaleAttr.setBaseValue(scale);
                    }
                }, null
            );
        }
    }

    public static void handleBabyDolphinSpawn(Dolphin dolphin) {
        if (ThreadLocalRandom.current().nextDouble() < BabyMobConfig.BABY_DOLPHIN_CHANCE) {
            FoliaScheduler.getEntityScheduler().run(dolphin,
                MidnightPatch.instance,
                task -> {
                    double scale = 0.4 + (ThreadLocalRandom.current().nextDouble() * 0.4); // Random scale between 0.4 and 0.8
                    AttributeInstance scaleAttr = dolphin.getAttribute(Attribute.SCALE);
                    if (scaleAttr != null) {
                        scaleAttr.setBaseValue(scale);
                    }
                }, null
            );
        }
    }
} 