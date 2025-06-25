package fun.mntale.midnightPatch.module.world.reacharound;

import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import fun.mntale.midnightPatch.MidnightPatch;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReachAroundPreviewManager {
    private final Map<UUID, BlockDisplay> previewDisplays = new ConcurrentHashMap<>();
    private final Map<UUID, Location> lastPreviewLocation = new ConcurrentHashMap<>();

    public void updatePreview(Player player, Material blockType) {
        if (!blockType.isBlock() || !blockType.isSolid()) {
            removePreview(player);
            return;
        }
        Location previewLocation = ReachAroundUtil.getPlayerReachAroundTarget(player);
        if (previewLocation == null) {
            removePreview(player);
            return;
        }
        Location lastLocation = lastPreviewLocation.get(player.getUniqueId());
        if (lastLocation != null && lastLocation.equals(previewLocation)) {
            return;
        }
        removePreview(player);
        final Location finalPreviewLocation = previewLocation;
        final Material finalBlockType = blockType;
        final Player finalPlayer = player;
        FoliaScheduler.getRegionScheduler().execute(MidnightPatch.instance, previewLocation, () -> {
            BlockDisplay display = finalPlayer.getWorld().spawn(finalPreviewLocation, BlockDisplay.class);
            display.setVisibleByDefault(false);
            finalPlayer.showEntity(MidnightPatch.instance, display);
            org.bukkit.block.data.BlockData blockData = finalBlockType.createBlockData();
            if (blockData instanceof org.bukkit.block.data.Directional directional) {
                BlockFace facing = ReachAroundUtil.getPlayerFacingDirection(finalPlayer);
                directional.setFacing(facing);
                blockData = directional;
            }
            display.setBlock(blockData);
            long time = finalPlayer.getWorld().getTime();
            int brightness = (time >= 0 && time < 12000) ? 5 : 15;
            display.setBrightness(new Display.Brightness(brightness, brightness));
            display.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new AxisAngle4f(0, 0, 0, 1),
                new Vector3f(1, 1, 1),
                new AxisAngle4f(0, 0, 0, 1)
            ));
            display.setInterpolationDuration(0);
            display.setInterpolationDelay(0);
            previewDisplays.put(finalPlayer.getUniqueId(), display);
            lastPreviewLocation.put(finalPlayer.getUniqueId(), finalPreviewLocation);
        });
    }

    public void removePreview(Player player) {
        UUID playerId = player.getUniqueId();
        BlockDisplay display = previewDisplays.remove(playerId);
        if (display != null && !display.isDead()) {
            final BlockDisplay finalDisplay = display;
            FoliaScheduler.getRegionScheduler().execute(MidnightPatch.instance, display.getLocation(), () -> {
                if (!finalDisplay.isDead()) {
                    finalDisplay.remove();
                }
            });
        }
        lastPreviewLocation.remove(playerId);
    }
} 