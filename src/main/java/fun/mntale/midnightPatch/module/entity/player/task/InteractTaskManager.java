package fun.mntale.midnightPatch.module.entity.player.task;

import fun.mntale.midnightPatch.MidnightPatch;
import fun.mntale.midnightPatch.module.entity.player.task.packet.ItemInteractUtil;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import io.github.retrooper.packetevents.util.folia.TaskWrapper;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class InteractTaskManager {
    private static final Map<Player, TaskWrapper> interactTasks = new ConcurrentHashMap<>();

    public static boolean isInteractTaskRunning(Player player) {
        return interactTasks.containsKey(player);
    }

    public static void startInteractTask(Player player, int interval) {
        if (interactTasks.containsKey(player)) return;
        
        TaskWrapper task = FoliaScheduler.getEntityScheduler().runAtFixedRate(player, MidnightPatch.instance, (ignored) -> {
            player.swingMainHand();
            ItemInteractUtil.sendNMSUseItem(player, true);
        }, null, 0L, interval);
        
        interactTasks.put(player, task);
    }

    public static void stopInteractTask(Player player) {
        TaskWrapper task = interactTasks.remove(player);
        if (task != null) {
            task.cancel();
        }
    }
} 