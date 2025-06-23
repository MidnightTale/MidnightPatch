package fun.mntale.midnightPatch.module.entity.armorstand;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.util.EulerAngle;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.jspecify.annotations.NullMarked;
import java.util.Map;

@NullMarked
public class PoseArmorStandListener implements Listener {
    private final Map<String, ArmorStandPose> presetPoses = ArmorStandPosePresets.getPresets();
    private static final NamespacedKey POSE_KEY = new NamespacedKey("midnightpatch", "armorstand_pose");

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof ArmorStand armorStand) {
            FoliaScheduler.getEntityScheduler().run(armorStand,
                fun.mntale.midnightPatch.MidnightPatch.instance,
                task -> {
                    if (armorStand.isDead()) return;
                    armorStand.setArms(true);
                }, null
            );
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand armorStand)) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType() != Material.AIR) {
            return;
        }
        cyclePose(armorStand);
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if (event.getNewCurrent() <= event.getOldCurrent()) {
            return;
        }
        for (Entity entity : event.getBlock().getWorld().getNearbyEntities(event.getBlock().getLocation().add(0.5, 0.5, 0.5), 1.5, 1.5, 1.5)) {
            if (entity instanceof ArmorStand armorStand) {
                cyclePose(armorStand);
            }
        }
    }

    private void cyclePose(ArmorStand armorStand) {
        String[] poseNames = presetPoses.keySet().toArray(new String[0]);
        String currentPose = getCurrentPoseName(armorStand);
        if (currentPose == null || !presetPoses.containsKey(currentPose)) {
            currentPose = "default";
        }
        int currentIndex = -1;
        for (int i = 0; i < poseNames.length; i++) {
            if (poseNames[i].equals(currentPose)) {
                currentIndex = i;
                break;
            }
        }
        String nextPose = poseNames[(currentIndex + 1) % poseNames.length];
        applyPose(armorStand, nextPose);
    }

    private void applyPose(ArmorStand armorStand, String poseName) {
        ArmorStandPose pose = presetPoses.get(poseName);
        if (pose == null) return;
        FoliaScheduler.getEntityScheduler().run(armorStand,
            fun.mntale.midnightPatch.MidnightPatch.instance,
            task -> {
                if (armorStand.isDead()) return;
                armorStand.setArms(true);
                armorStand.setHeadPose(pose.head);
                armorStand.setBodyPose(pose.body);
                armorStand.setLeftArmPose(pose.leftArm);
                armorStand.setRightArmPose(pose.rightArm);
                armorStand.setLeftLegPose(pose.leftLeg);
                armorStand.setRightLegPose(pose.rightLeg);
                armorStand.getPersistentDataContainer().set(POSE_KEY, PersistentDataType.STRING, poseName);
            }, null
        );
    }

    private String getCurrentPoseName(ArmorStand armorStand) {
        return armorStand.getPersistentDataContainer().get(POSE_KEY, PersistentDataType.STRING);
    }
} 