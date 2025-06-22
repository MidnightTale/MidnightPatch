package fun.mntale.midnightPatch.module.world.reacharound;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import fun.mntale.midnightPatch.command.ToggleReachAroundCommand;

public class ReachAroundBlockListener implements Listener {
    private final ReachAroundPreviewManager previewManager = new ReachAroundPreviewManager();
    private final ReachAroundPlayerTaskManager playerTaskManager = new ReachAroundPlayerTaskManager(previewManager);
    private final ReachAroundBlockPlacer blockPlacer = new ReachAroundBlockPlacer();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerTaskManager.startPlayerTask(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerTaskManager.stopPlayerTask(event.getPlayer());
        previewManager.removePreview(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item == null || !item.getType().isBlock()) {
            previewManager.removePreview(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick() || event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }
        Player player = event.getPlayer();
        if (!ToggleReachAroundCommand.isReachAroundEnabled(player)) {
            return;
        }
        if (ReachAroundUtil.isBedrockPlayer(player)) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || !item.getType().isBlock()) {
            return;
        }
        Location reachAroundLocation = ReachAroundUtil.getPlayerReachAroundTarget(player);
        if (reachAroundLocation != null) {
            blockPlacer.placeBlock(player, item, reachAroundLocation);
        }
    }
} 