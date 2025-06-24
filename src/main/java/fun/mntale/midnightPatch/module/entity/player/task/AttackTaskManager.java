package fun.mntale.midnightPatch.module.entity.player.task;

import fun.mntale.midnightPatch.MidnightPatch;
import fun.mntale.midnightPatch.module.entity.player.task.packet.EntityInteractUtil;
import fun.mntale.midnightPatch.module.entity.player.task.tool.EntityTargetingUtil;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import io.github.retrooper.packetevents.util.folia.TaskWrapper;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AttackTaskManager {
    private static final Map<Player, TaskWrapper> attackTasks = new HashMap<>();

    public static boolean isAttackTaskRunning(Player player) {
        return attackTasks.containsKey(player);
    }

    public static void startAttackTask(Player player, int interval) {
        if (attackTasks.containsKey(player)) return;
        
        TaskWrapper task = FoliaScheduler.getEntityScheduler().runAtFixedRate(player, MidnightPatch.instance, (ignored) -> {
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
        TaskWrapper task = attackTasks.remove(player);
        if (task != null) {
            task.cancel();
        }
    }
} 