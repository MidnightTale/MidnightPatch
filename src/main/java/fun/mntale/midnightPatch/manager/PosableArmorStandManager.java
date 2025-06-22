package fun.mntale.midnightPatch.entity;

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

import java.util.HashMap;
import java.util.Map;

@NullMarked
public class PosableArmorStandManager implements Listener {
    
    // Preset poses like Bedrock
    private final Map<String, ArmorStandPose> presetPoses = new HashMap<>();
    private static final NamespacedKey POSE_KEY = new NamespacedKey("midnightpatch", "armorstand_pose");
    
    public PosableArmorStandManager() {
        initializePresetPoses();
    }
    
    private void initializePresetPoses() {
        // Default: Perfectly straight, neutral pose.
        presetPoses.put("default", new ArmorStandPose(
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0), new EulerAngle(Math.toRadians(-10), 0, 0),
            new EulerAngle(Math.toRadians(-10), 0, 0), new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0)
        ));

        // Sitting: Legs bent as if on a chair, arms resting.
        presetPoses.put("sitting", new ArmorStandPose(
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-15), 0, Math.toRadians(5)), // Left arm resting
            new EulerAngle(Math.toRadians(-15), 0, Math.toRadians(-5)), // Right arm resting
            new EulerAngle(Math.toRadians(90), 0, 0),  // Left leg bent
            new EulerAngle(Math.toRadians(90), 0, 0)   // Right leg bent
        ));

        // Waving: A friendly wave with the right hand.
        presetPoses.put("wave", new ArmorStandPose(
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-120), Math.toRadians(0), Math.toRadians(-45)), // Right arm waving
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0)
        ));

        // Victory: Both arms up in a celebratory 'V' shape.
        presetPoses.put("victory", new ArmorStandPose(
            new EulerAngle(Math.toRadians(-5), 0, 0), new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-150), Math.toRadians(0), Math.toRadians(-35)), // Left arm up
            new EulerAngle(Math.toRadians(-150), Math.toRadians(0), Math.toRadians(35)), // Right arm up
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0)
        ));

        // Bowing: A respectful bow, body and head forward.
        presetPoses.put("bow", new ArmorStandPose(
            new EulerAngle(Math.toRadians(35), 0, 0), // Head bowed
            new EulerAngle(Math.toRadians(25), 0, 0), // Body bowed
            new EulerAngle(Math.toRadians(-10), 0, 0), new EulerAngle(Math.toRadians(-10), 0, 0),
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0)
        ));

        // Superhero Flight: A dynamic flying pose.
        presetPoses.put("superhero", new ArmorStandPose(
            new EulerAngle(0, 0, 0), new EulerAngle(Math.toRadians(20), 0, 0), // Body leaning forward
            new EulerAngle(Math.toRadians(-70), 0, 0), // Left arm back
            new EulerAngle(Math.toRadians(-90), 0, 0), // Right arm forward (punching)
            new EulerAngle(Math.toRadians(15), 0, 0),  // Left leg trailing
            new EulerAngle(Math.toRadians(-15), 0, 0)   // Right leg forward
        ));

        // Thinking: Hand to chin, legs crossed, deep in thought.
        presetPoses.put("thinking", new ArmorStandPose(
            new EulerAngle(Math.toRadians(-10), Math.toRadians(0), Math.toRadians(20)), // Head tilted
            new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-100), Math.toRadians(-20), 0), // Left hand on chin
            new EulerAngle(Math.toRadians(-15), 0, Math.toRadians(-5)), // Right arm resting
            new EulerAngle(Math.toRadians(0), Math.toRadians(0), Math.toRadians(15)), // Legs crossed
            new EulerAngle(Math.toRadians(0), Math.toRadians(0), Math.toRadians(-15))
        ));
        
        // Facepalm: Head down, hand to face in disappointment.
        presetPoses.put("facepalm", new ArmorStandPose(
            new EulerAngle(Math.toRadians(25), 0, 0), // Head down
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-160), Math.toRadians(25), 0), // Right hand on face
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0)
        ));

        // Shrug: A classic "I don't know" gesture.
        presetPoses.put("shrug", new ArmorStandPose(
            new EulerAngle(Math.toRadians(5), 0, 0), // Head slightly up
            new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-20), Math.toRadians(0), Math.toRadians(25)), // Left arm shrug
            new EulerAngle(Math.toRadians(-20), Math.toRadians(0), Math.toRadians(-25)),// Right arm shrug
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0)
        ));

        // T-Pose: Asserting dominance.
        presetPoses.put("t-pose", new ArmorStandPose(
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, Math.toRadians(90)), // Left arm out
            new EulerAngle(0, 0, Math.toRadians(-90)), // Right arm out
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0)
        ));
    }
    
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
        
        event.setCancelled(true);
        cyclePose(armorStand);
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        // Only trigger on a rising edge (i.e., when power is applied or increased)
        if (event.getNewCurrent() <= event.getOldCurrent()) {
            return;
        }

        // Check for nearby armor stands and cycle their pose
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
    
    // Helper class to store pose data
    private static class ArmorStandPose {
        final EulerAngle head, body, leftArm, rightArm, leftLeg, rightLeg;
        
        ArmorStandPose(EulerAngle head, EulerAngle body, EulerAngle leftArm, 
                      EulerAngle rightArm, EulerAngle leftLeg, EulerAngle rightLeg) {
            this.head = head;
            this.body = body;
            this.leftArm = leftArm;
            this.rightArm = rightArm;
            this.leftLeg = leftLeg;
            this.rightLeg = rightLeg;
        }
    }
} 