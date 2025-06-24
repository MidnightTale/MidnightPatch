package fun.mntale.midnightPatch.module.entity.player.task;

import fun.mntale.midnightPatch.MidnightPatch;
import fun.mntale.midnightPatch.module.entity.player.task.packet.ItemInteractUtil;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import io.github.retrooper.packetevents.util.folia.TaskWrapper;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class UseTaskManager {
    private static final Map<Player, TaskWrapper> useTasks = new ConcurrentHashMap<>();

    public static boolean isUseTaskRunning(Player player) {
        return useTasks.containsKey(player);
    }

    public static void startUseTask(Player player, int interval) {
        if (useTasks.containsKey(player)) return;
        
        TaskWrapper task = FoliaScheduler.getEntityScheduler().runAtFixedRate(player, MidnightPatch.instance, (ignored) -> {
            player.swingMainHand();
            ItemInteractUtil.sendNMSUseItem(player, false);
        }, null, 0L, interval);
        
        useTasks.put(player, task);
    }

    public static void stopUseTask(Player player) {
        TaskWrapper task = useTasks.remove(player);
        if (task != null) {
            task.cancel();
        }
    }
} 