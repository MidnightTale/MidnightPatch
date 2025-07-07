package fun.mntale.midnightPatch.module.entity.player.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import fun.mntale.midnightPatch.MidnightPatch;
import fun.mntale.midnightPatch.module.entity.player.task.packet.EntityInteractUtil;
import fun.mntale.midnightPatch.module.entity.player.task.tool.EntityTargetingUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class AttackTaskManager {
    private static final Map<Player, WrappedTask> attackTasks = new ConcurrentHashMap<>();

    public static boolean isAttackTaskRunning(Player player) {
        return attackTasks.containsKey(player);
    }

    public static void startAttackTask(Player player, int interval) {
        if (attackTasks.containsKey(player)) return;
        
        WrappedTask task = MidnightPatch.instance.foliaLib.getScheduler().runAtEntityTimer(player, () -> {
            player.swingMainHand();
            double range = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).getValue();
            Entity target = EntityTargetingUtil.getTargetEntityDoubleRange(player, range);
            if (target != null) {
                EntityInteractUtil.sendNMSAttack(player, target);
            }
        }, null, 0L, interval);
        
        attackTasks.put(player, task);
    }

    public static void stopAttackTask(Player player) {
        WrappedTask task = attackTasks.remove(player);
        if (task != null) {
            task.cancel();
        }
    }
} 