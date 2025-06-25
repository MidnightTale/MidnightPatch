package fun.mntale.midnightPatch.module.entity.player.task;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fun.mntale.midnightPatch.module.entity.player.task.effect.BlockBreakingManager;
import fun.mntale.midnightPatch.module.entity.player.task.packet.PacketUtil;

public class PlayerTaskManager implements Listener {

    public static boolean isAttackTaskRunning(Player player) {
        if (isCreativeOrSpectator(player)) return false;
        return AttackTaskManager.isAttackTaskRunning(player);
    }

    public static void startAttackTask(Player player, int interval) {
        if (isCreativeOrSpectator(player)) return;
        AttackTaskManager.startAttackTask(player, interval);
    }

    public static void stopAttackTask(Player player) {
        if (isCreativeOrSpectator(player)) return;
        AttackTaskManager.stopAttackTask(player);
    }

    public static boolean isInteractTaskRunning(Player player) {
        if (isCreativeOrSpectator(player)) return false;
        return InteractTaskManager.isInteractTaskRunning(player);
    }

    public static void startInteractTask(Player player, int interval) {
        if (isCreativeOrSpectator(player)) return;
        InteractTaskManager.startInteractTask(player, interval);
    }

    public static void stopInteractTask(Player player) {
        if (isCreativeOrSpectator(player)) return;
        InteractTaskManager.stopInteractTask(player);
    }

    public static boolean isUseTaskRunning(Player player) {
        if (isCreativeOrSpectator(player)) return false;
        return UseTaskManager.isUseTaskRunning(player);
    }

    public static void startUseTask(Player player, int interval) {
        if (isCreativeOrSpectator(player)) return;
        UseTaskManager.startUseTask(player, interval);
    }

    public static void stopUseTask(Player player) {
        if (isCreativeOrSpectator(player)) return;
        UseTaskManager.stopUseTask(player);
    }

    public static boolean isPlaceTaskRunning(Player player) {
        if (isCreativeOrSpectator(player)) return false;
        return BlockPlacingManager.isPlaceTaskRunning(player);
    }

    public static void startPlaceTask(Player player, int interval) {
        if (isCreativeOrSpectator(player)) return;
        BlockPlacingManager.startPlaceTask(player, interval);
    }

    public static void stopPlaceTask(Player player) {
        if (isCreativeOrSpectator(player)) return;
        BlockPlacingManager.stopPlaceTask(player);
    }

    public static boolean isBreakTaskRunning(Player player) {
        if (isCreativeOrSpectator(player)) return false;
        return BlockBreakingManager.isBreakTaskRunning(player);
    }

    public static void startBreakTask(Player player, int interval) {
        if (isCreativeOrSpectator(player)) return;
        BlockBreakingManager.startBreakTask(player, interval);
    }

    public static void stopBreakTask(Player player) {
        if (isCreativeOrSpectator(player)) return;
        BlockBreakingManager.stopBreakTask(player);
    }

    public static void stopAllTasks(Player player) {
        if (isCreativeOrSpectator(player)) return;
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
        if (isCreativeOrSpectator(player)) return;
        PacketUtil.sendSwingArmPacket(player, hand);
    }

    /**
     * Checks if the player is in Creative or Spectator mode.
     */
    private static boolean isCreativeOrSpectator(Player player) {
        GameMode mode = player.getGameMode();
        return mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR;
    }
} 