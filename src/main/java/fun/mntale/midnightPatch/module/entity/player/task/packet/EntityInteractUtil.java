package fun.mntale.midnightPatch.module.entity.player.task.packet;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

/**
 * Utility for simulating player interactions with entities using direct NMS packets.
 * Includes attack (left-click), interact (right-click), and interact at location (e.g., for armor stands).
 * All methods are safe for Paper/Folia 1.21+ and avoid reflection or ProtocolLib.
 */
public class EntityInteractUtil {
    /**
     * Simulates a left-click (attack) on a target entity as if the player attacked it.
     * This is equivalent to the player swinging at and damaging the entity.
     *
     * @param bukkitPlayer The player performing the attack
     * @param bukkitTarget The entity to be attacked
     */
    public static void sendNMSAttack(Player bukkitPlayer, org.bukkit.entity.Entity bukkitTarget) {
        try {
            ServerPlayer nmsPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
            net.minecraft.world.entity.Entity nmsTarget = ((org.bukkit.craftbukkit.entity.CraftEntity) bukkitTarget).getHandle();
            ServerboundInteractPacket attackPacket = ServerboundInteractPacket.createAttackPacket(nmsTarget, bukkitPlayer.isSneaking());
            nmsPlayer.connection.handleInteract(attackPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Simulates a right-click (interact) on a target entity as if the player interacted with it.
     * This is used for actions like opening a villager trade, mounting, or using an item on an entity.
     *
     * @param bukkitPlayer The player performing the interaction
     * @param bukkitTarget The entity to interact with
     * @param mainHand true for main hand, false for off hand
     */
    public static void sendNMSInteract(Player bukkitPlayer, org.bukkit.entity.Entity bukkitTarget, boolean mainHand) {
        try {
            ServerPlayer nmsPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
            net.minecraft.world.entity.Entity nmsTarget = ((org.bukkit.craftbukkit.entity.CraftEntity) bukkitTarget).getHandle();
            InteractionHand hand = mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ServerboundInteractPacket interactPacket = ServerboundInteractPacket.createInteractionPacket(nmsTarget, bukkitPlayer.isSneaking(), hand);
            nmsPlayer.connection.handleInteract(interactPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Simulates a right-click (interact at location) on a target entity at a specific point.
     * This is used for entities that support location-based interaction, such as armor stands (for posing arms, etc).
     *
     * @param bukkitPlayer The player performing the interaction
     * @param bukkitTarget The entity to interact with
     * @param mainHand true for main hand, false for off hand
     * @param x X offset relative to the entity's position (target point)
     * @param y Y offset relative to the entity's position (target point)
     * @param z Z offset relative to the entity's position (target point)
     */
    public static void sendNMSInteractAt(Player bukkitPlayer, org.bukkit.entity.Entity bukkitTarget, boolean mainHand, double x, double y, double z) {
        try {
            ServerPlayer nmsPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
            net.minecraft.world.entity.Entity nmsTarget = ((org.bukkit.craftbukkit.entity.CraftEntity) bukkitTarget).getHandle();
            InteractionHand hand = mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            Vec3 location = new Vec3(x, y, z);
            ServerboundInteractPacket interactAtPacket = ServerboundInteractPacket.createInteractionPacket(nmsTarget, bukkitPlayer.isSneaking(), hand, location);
            nmsPlayer.connection.handleInteract(interactAtPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 