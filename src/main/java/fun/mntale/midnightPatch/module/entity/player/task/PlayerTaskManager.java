package fun.mntale.midnightPatch.module.entity.player.task;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fun.mntale.midnightPatch.module.entity.player.task.effect.BlockBreakingManager;
import fun.mntale.midnightPatch.module.entity.player.task.packet.PacketUtil;

public class PlayerTaskManager implements Listener {

    public static boolean isAttackTaskRunning(Player player) {
        return AttackTaskManager.isAttackTaskRunning(player);
    }

    public static void startAttackTask(Player player, int interval) {
        AttackTaskManager.startAttackTask(player, interval);
    }

    public static void stopAttackTask(Player player) {
        AttackTaskManager.stopAttackTask(player);
    }

    public static boolean isInteractTaskRunning(Player player) {
        return InteractTaskManager.isInteractTaskRunning(player);
    }

    public static void startInteractTask(Player player, int interval) {
        InteractTaskManager.startInteractTask(player, interval);
    }

    public static void stopInteractTask(Player player) {
        InteractTaskManager.stopInteractTask(player);
    }

    public static boolean isUseTaskRunning(Player player) {
        return UseTaskManager.isUseTaskRunning(player);
    }

    public static void startUseTask(Player player, int interval) {
        UseTaskManager.startUseTask(player, interval);
    }

    public static void stopUseTask(Player player) {
        UseTaskManager.stopUseTask(player);
    }

    public static boolean isPlaceTaskRunning(Player player) {
        return BlockPlacingManager.isPlaceTaskRunning(player);
    }

    public static void startPlaceTask(Player player, int interval) {
        BlockPlacingManager.startPlaceTask(player, interval);
    }

    public static void stopPlaceTask(Player player) {
        BlockPlacingManager.stopPlaceTask(player);
    }

    public static boolean isBreakTaskRunning(Player player) {
        return BlockBreakingManager.isBreakTaskRunning(player);
    }

    public static void startBreakTask(Player player, int interval) {
        BlockBreakingManager.startBreakTask(player, interval);
    }

    public static void stopBreakTask(Player player) {
        BlockBreakingManager.stopBreakTask(player);
    }

    public static void stopAllTasks(Player player) {
        stopAttackTask(player);
        stopInteractTask(player);
        stopUseTask(player);
        stopPlaceTask(player);
        stopBreakTask(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopAllTasks(event.getPlayer());
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        stopAllTasks(event.getPlayer());
    }

    public static void sendSwingArmPacket(Player player, int hand) {
        PacketUtil.sendSwingArmPacket(player, hand);
    }
} 