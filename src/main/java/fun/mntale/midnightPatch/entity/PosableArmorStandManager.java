package fun.mntale.midnightPatch.entity;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.util.EulerAngle;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.util.RayTraceResult;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.ThreadLocalRandom;
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
        // Default poses
        presetPoses.put("default", new ArmorStandPose(
            new EulerAngle(0, 0, 0), // head
            new EulerAngle(0, 0, 0), // body
            new EulerAngle(0, 0, 0), // left arm
            new EulerAngle(0, 0, 0), // right arm
            new EulerAngle(0, 0, 0), // left leg
            new EulerAngle(0, 0, 0)  // right leg
        ));
        
        // Salute pose
        presetPoses.put("salute", new ArmorStandPose(
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-90), 0, Math.toRadians(45)), // right arm salute
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0)
        ));
        
        // Wave pose
        presetPoses.put("wave", new ArmorStandPose(
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, Math.toRadians(45), Math.toRadians(-90)), // right arm wave
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0)
        ));
        
        // Point pose
        presetPoses.put("point", new ArmorStandPose(
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, Math.toRadians(-45)), // right arm point
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0)
        ));
        
        // Sitting pose
        presetPoses.put("sitting", new ArmorStandPose(
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(90), 0, 0), // left leg sitting
            new EulerAngle(Math.toRadians(90), 0, 0)  // right leg sitting
        ));
        
        // Dancing pose
        presetPoses.put("dancing", new ArmorStandPose(
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(45), 0, Math.toRadians(30)), // left arm dance
            new EulerAngle(Math.toRadians(-45), 0, Math.toRadians(-30)), // right arm dance
            new EulerAngle(Math.toRadians(20), 0, 0), // left leg dance
            new EulerAngle(Math.toRadians(-20), 0, 0) // right leg dance
        ));
        
        // Thinking pose
        presetPoses.put("thinking", new ArmorStandPose(
            new EulerAngle(Math.toRadians(15), 0, 0), // head tilted
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, Math.toRadians(45)), // left arm thinking
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0)
        ));
        
        // Victory pose
        presetPoses.put("victory", new ArmorStandPose(
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-45), 0, Math.toRadians(30)), // left arm V
            new EulerAngle(Math.toRadians(-45), 0, Math.toRadians(-30)), // right arm V
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0)
        ));
        
        // Surprised pose
        presetPoses.put("surprised", new ArmorStandPose(
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-90), 0, 0), // left arm up
            new EulerAngle(Math.toRadians(-90), 0, 0), // right arm up
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0)
        ));
        
        // Bow pose
        presetPoses.put("bow", new ArmorStandPose(
            new EulerAngle(Math.toRadians(45), 0, 0), // head down
            new EulerAngle(Math.toRadians(30), 0, 0), // body forward
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0)
        ));
        
        // Flex pose
        presetPoses.put("flex", new ArmorStandPose(
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-45), 0, Math.toRadians(90)), // left arm flex
            new EulerAngle(Math.toRadians(-45), 0, Math.toRadians(-90)), // right arm flex
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0)
        ));
        
        // Sleep pose
        presetPoses.put("sleep", new ArmorStandPose(
            new EulerAngle(Math.toRadians(90), 0, 0), // head to side
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(45), 0, 0), // left leg bent
            new EulerAngle(Math.toRadians(45), 0, 0)  // right leg bent
        ));
        
        // Jump pose
        presetPoses.put("jump", new ArmorStandPose(
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-30), 0, 0), // left arm up
            new EulerAngle(Math.toRadians(-30), 0, 0), // right arm up
            new EulerAngle(Math.toRadians(-45), 0, 0), // left leg up
            new EulerAngle(Math.toRadians(-45), 0, 0)  // right leg up
        ));
        
        // Meditate pose
        presetPoses.put("meditate", new ArmorStandPose(
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, Math.toRadians(90)), // left arm meditation
            new EulerAngle(0, 0, Math.toRadians(-90)), // right arm meditation
            new EulerAngle(Math.toRadians(90), 0, 0), // cross-legged
            new EulerAngle(Math.toRadians(90), 0, 0)  // cross-legged
        ));
        
        // Run pose
        presetPoses.put("run", new ArmorStandPose(
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-45), 0, 0), // left arm back
            new EulerAngle(Math.toRadians(45), 0, 0),  // right arm forward
            new EulerAngle(Math.toRadians(45), 0, 0),  // left leg forward
            new EulerAngle(Math.toRadians(-45), 0, 0)  // right leg back
        ));
        
        // Clap pose
        presetPoses.put("clap", new ArmorStandPose(
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, Math.toRadians(45), 0), // left arm clap
            new EulerAngle(0, Math.toRadians(-45), 0), // right arm clap
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0)
        ));
        
        // Shrug pose
        presetPoses.put("shrug", new ArmorStandPose(
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-30), 0, Math.toRadians(45)), // left arm shrug
            new EulerAngle(Math.toRadians(-30), 0, Math.toRadians(-45)), // right arm shrug
            new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, 0)
        ));
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // Check if it's an armor stand
        if (!(event.getRightClicked() instanceof ArmorStand armorStand)) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Debug: Log the interaction
        player.sendMessage("§e[DEBUG] Right-click on armor stand detected. Item: " + (item != null ? item.getType().name() : "AIR"));
        
        // Only cycle pose if player has empty hand
        if (item != null && item.getType() != Material.AIR) {
            player.sendMessage("§e[DEBUG] Item in hand, skipping pose cycle");
            return;
        }
        
        player.sendMessage("§e[DEBUG] Cycling pose on armor stand!");
        event.setCancelled(true);
        
        // Cycle through poses
        cyclePose(armorStand);
        player.sendMessage("§a[DEBUG] Pose cycled successfully!");
    }
    
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof ArmorStand armorStand) {
            // Enable arms by default for new armor stands
            FoliaScheduler.getEntityScheduler().run(armorStand,
                fun.mntale.midnightPatch.MidnightPatch.instance,
                task -> {
                    if (armorStand.isDead()) return;
                    
                    // Enable arms
                    armorStand.setArms(true);
                }, null
            );
        }
    }
    
    private void cyclePose(ArmorStand armorStand) {
        String[] poseNames = presetPoses.keySet().toArray(new String[0]);
        
        // Get current pose from PDC or start with first
        String currentPose = getCurrentPoseName(armorStand);
        if (currentPose == null || !presetPoses.containsKey(currentPose)) {
            currentPose = "default";
        }
        
        // Find next pose
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
        
        // Use entity task to apply pose
        FoliaScheduler.getEntityScheduler().run(armorStand,
            fun.mntale.midnightPatch.MidnightPatch.instance,
            task -> {
                if (armorStand.isDead()) return;
                
                // Enable arms if not already enabled
                armorStand.setArms(true);
                
                // Apply pose angles
                armorStand.setHeadPose(pose.head);
                armorStand.setBodyPose(pose.body);
                armorStand.setLeftArmPose(pose.leftArm);
                armorStand.setRightArmPose(pose.rightArm);
                armorStand.setLeftLegPose(pose.leftLeg);
                armorStand.setRightLegPose(pose.rightLeg);
                
                // Store pose name in PDC
                armorStand.getPersistentDataContainer().set(POSE_KEY, PersistentDataType.STRING, poseName);
            }, null
        );
    }
    
    private void loadPoseFromPDC(ArmorStand armorStand) {
        String poseName = armorStand.getPersistentDataContainer().get(POSE_KEY, PersistentDataType.STRING);
        if (poseName != null && presetPoses.containsKey(poseName)) {
            ArmorStandPose pose = presetPoses.get(poseName);
            
            // Apply pose angles
            armorStand.setHeadPose(pose.head);
            armorStand.setBodyPose(pose.body);
            armorStand.setLeftArmPose(pose.leftArm);
            armorStand.setRightArmPose(pose.rightArm);
            armorStand.setLeftLegPose(pose.leftLeg);
            armorStand.setRightLegPose(pose.rightLeg);
        }
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