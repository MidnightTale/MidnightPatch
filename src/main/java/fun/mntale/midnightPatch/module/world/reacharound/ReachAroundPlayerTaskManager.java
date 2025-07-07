package fun.mntale.midnightPatch.module.world.reacharound;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import fun.mntale.midnightPatch.MidnightPatch;
import fun.mntale.midnightPatch.command.ToggleReachAroundCommand;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReachAroundPlayerTaskManager {
    private final Map<UUID, WrappedTask> playerTasks = new ConcurrentHashMap<>();
    private final ReachAroundPreviewManager previewManager;

    public ReachAroundPlayerTaskManager(ReachAroundPreviewManager previewManager) {
        this.previewManager = previewManager;
    }

    public void startPlayerTask(Player player) {
        UUID playerId = player.getUniqueId();
        if (playerTasks.containsKey(playerId)) {
            return;
        }

        WrappedTask task = MidnightPatch.instance.foliaLib.getScheduler().runAtEntityTimer(
            player,
                () -> {
                if (!player.isOnline()) {
                    stopPlayerTask(player);
                    return;
                }
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item == null || !item.getType().isBlock()) {
                    previewManager.removePreview(player);
                    return;
                }
                if (!ToggleReachAroundCommand.isReachAroundEnabled(player)) {
                    previewManager.removePreview(player);
                    return;
                }
                previewManager.updatePreview(player, item.getType());
            },
            60L,
            2L
        );
        playerTasks.put(playerId, task);
    }

    public void stopPlayerTask(Player player) {
        UUID playerId = player.getUniqueId();
        WrappedTask task = playerTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }
} 