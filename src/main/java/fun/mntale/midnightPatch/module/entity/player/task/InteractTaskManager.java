package fun.mntale.midnightPatch.module.entity.player.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import fun.mntale.midnightPatch.MidnightPatch;
import fun.mntale.midnightPatch.module.entity.player.task.packet.ItemInteractUtil;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class InteractTaskManager {
    private static final Map<Player, WrappedTask> interactTasks = new ConcurrentHashMap<>();

    public static boolean isInteractTaskRunning(Player player) {
        return interactTasks.containsKey(player);
    }

    public static void startInteractTask(Player player, int interval) {
        if (interactTasks.containsKey(player)) return;
        
        WrappedTask task = MidnightPatch.instance.foliaLib.getScheduler().runAtEntityTimer(player, () -> {
            player.swingMainHand();
            ItemInteractUtil.sendNMSUseItem(player, true);
        }, null, 0L, interval);
        
        interactTasks.put(player, task);
    }

    public static void stopInteractTask(Player player) {
        WrappedTask task = interactTasks.remove(player);
        if (task != null) {
            task.cancel();
        }
    }
} 