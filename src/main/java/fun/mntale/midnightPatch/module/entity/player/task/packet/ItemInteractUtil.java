package fun.mntale.midnightPatch.module.entity.player.task.packet;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;

/**
 * Utility for simulating use item in air (right-click, not on entity) via NMS.
 */
public class ItemInteractUtil {
    /**
     * Simulates a right-click in air (use item) for the player using NMS.
     * @param bukkitPlayer The player
     * @param mainHand true for main hand, false for off hand
     */
    public static void sendNMSUseItem(Player bukkitPlayer, boolean mainHand) {
        try {
            ServerPlayer nmsPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
            InteractionHand hand = mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            int sequence = 0; // Plugins can use 0 for sequence
            float yRot = bukkitPlayer.getLocation().getYaw();
            float xRot = bukkitPlayer.getLocation().getPitch();
            ServerboundUseItemPacket useItemPacket = new ServerboundUseItemPacket(hand, sequence, yRot, xRot);
            nmsPlayer.connection.handleUseItem(useItemPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Simulates a right-click on a block (place block or use item on block) for the player using NMS.
     * @param bukkitPlayer The player
     * @param block The block being targeted
     * @param face The face being targeted
     * @param mainHand true for main hand, false for off hand
     */
    public static void sendNMSUseItemOn(Player bukkitPlayer, Block block, BlockFace face, boolean mainHand) {
        try {
            ServerPlayer nmsPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
            InteractionHand hand = mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());
            float cursorX = 0.5f;
            float cursorY = 0.5f;
            float cursorZ = 0.5f;
            net.minecraft.core.Direction nmsFace = switch (face) {
                case DOWN -> net.minecraft.core.Direction.DOWN;
                case UP -> net.minecraft.core.Direction.UP;
                case NORTH -> net.minecraft.core.Direction.NORTH;
                case SOUTH -> net.minecraft.core.Direction.SOUTH;
                case WEST -> net.minecraft.core.Direction.WEST;
                case EAST -> net.minecraft.core.Direction.EAST;
                default -> net.minecraft.core.Direction.UP;
            };
            BlockHitResult hitResult = new BlockHitResult(
                new Vec3(block.getX() + cursorX, block.getY() + cursorY, block.getZ() + cursorZ),
                nmsFace, pos, false
            );
            int sequence = 0;
            ServerboundUseItemOnPacket packet = new ServerboundUseItemOnPacket(
                hand, hitResult, sequence
            );
            nmsPlayer.connection.handleUseItemOn(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Simulates starting to break a block (left-click and hold) for the player using NMS.
     * @param bukkitPlayer The player
     * @param block The block being targeted
     * @param face The face being targeted
     */
    public static void sendNMSStartBreak(Player bukkitPlayer, Block block, BlockFace face) {
        try {
            ServerPlayer nmsPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
            BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());
            net.minecraft.core.Direction nmsFace = switch (face) {
                case DOWN -> net.minecraft.core.Direction.DOWN;
                case UP -> net.minecraft.core.Direction.UP;
                case NORTH -> net.minecraft.core.Direction.NORTH;
                case SOUTH -> net.minecraft.core.Direction.SOUTH;
                case WEST -> net.minecraft.core.Direction.WEST;
                case EAST -> net.minecraft.core.Direction.EAST;
                default -> net.minecraft.core.Direction.UP;
            };
            int sequence = 0;
            ServerboundPlayerActionPacket packet = new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, nmsFace, sequence
            );
            nmsPlayer.connection.handlePlayerAction(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Simulates stopping to break a block (release left-click) for the player using NMS.
     * @param bukkitPlayer The player
     * @param block The block being targeted
     * @param face The face being targeted
     */
    public static void sendNMSStopBreak(Player bukkitPlayer, Block block, BlockFace face) {
        try {
            ServerPlayer nmsPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
            BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());
            net.minecraft.core.Direction nmsFace = switch (face) {
                case DOWN -> net.minecraft.core.Direction.DOWN;
                case UP -> net.minecraft.core.Direction.UP;
                case NORTH -> net.minecraft.core.Direction.NORTH;
                case SOUTH -> net.minecraft.core.Direction.SOUTH;
                case WEST -> net.minecraft.core.Direction.WEST;
                case EAST -> net.minecraft.core.Direction.EAST;
                default -> net.minecraft.core.Direction.UP;
            };
            int sequence = 0;
            ServerboundPlayerActionPacket packet = new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, nmsFace, sequence
            );
            nmsPlayer.connection.handlePlayerAction(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Simulates aborting breaking a block (release left-click before breaking) for the player using NMS.
     * @param bukkitPlayer The player
     * @param block The block being targeted
     * @param face The face being targeted
     */
    public static void sendNMSAbortBreak(Player bukkitPlayer, Block block, BlockFace face) {
        try {
            ServerPlayer nmsPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
            BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());
            net.minecraft.core.Direction nmsFace = switch (face) {
                case DOWN -> net.minecraft.core.Direction.DOWN;
                case UP -> net.minecraft.core.Direction.UP;
                case NORTH -> net.minecraft.core.Direction.NORTH;
                case SOUTH -> net.minecraft.core.Direction.SOUTH;
                case WEST -> net.minecraft.core.Direction.WEST;
                case EAST -> net.minecraft.core.Direction.EAST;
                default -> net.minecraft.core.Direction.UP;
            };
            int sequence = 0;
            ServerboundPlayerActionPacket packet = new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, pos, nmsFace, sequence
            );
            nmsPlayer.connection.handlePlayerAction(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 