package fun.mntale.midnightPatch.module.entity.player.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import fun.mntale.midnightPatch.MidnightPatch;
import fun.mntale.midnightPatch.module.entity.player.task.packet.ItemInteractUtil;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class UseTaskManager {
    private static final Map<Player, WrappedTask> useTasks = new ConcurrentHashMap<>();

    public static boolean isUseTaskRunning(Player player) {
        return useTasks.containsKey(player);
    }

    public static void startUseTask(Player player, int interval) {
        if (useTasks.containsKey(player)) return;
        
        WrappedTask task = MidnightPatch.instance.foliaLib.getScheduler().runAtEntityTimer(player,() -> {
            player.swingMainHand();
            ItemInteractUtil.sendNMSUseItem(player, false);
        }, null, 0L, interval);
        
        useTasks.put(player, task);
    }

    public static void stopUseTask(Player player) {
        WrappedTask task = useTasks.remove(player);
        if (task != null) {
            task.cancel();
        }
    }
} 