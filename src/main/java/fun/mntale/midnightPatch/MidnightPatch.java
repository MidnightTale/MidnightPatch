package fun.mntale.midnightPatch;

import fun.mntale.midnightPatch.chunk.block.MossBlockManager;
import fun.mntale.midnightPatch.entity.LootMobTargetManager;
import org.bukkit.plugin.java.JavaPlugin;
import fun.mntale.midnightPatch.chunk.EnderPearlChunkManager;
import fun.mntale.midnightPatch.skin.SkinManager;
import fun.mntale.midnightPatch.skin.SkinCommand;
import io.papermc.paper.command.brigadier.BasicCommand;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import fun.mntale.midnightPatch.chunk.block.DesirePathManager;
import fun.mntale.midnightPatch.chunk.block.ReachAroundBlockManager;
import fun.mntale.midnightPatch.command.KillCommand;
import fun.mntale.midnightPatch.command.ToggleReachAroundCommand;
import fun.mntale.midnightPatch.entity.ProjectileDamageManager;
import fun.mntale.midnightPatch.stats.ServerStatsManager;

public final class MidnightPatch extends JavaPlugin {
    public static MidnightPatch instance;
    public SkinManager skinManager;
    public EnderPearlChunkManager enderPearlChunkManager;
    public MossBlockManager mossBlockManager;
    public LootMobTargetManager lootMobTargetManager;
    public DesirePathManager desirePathManager;
    public ProjectileDamageManager projectileDamageManager;
    public ServerStatsManager serverStatsManager;
    public ReachAroundBlockManager reachAroundBlockManager;

    // Configuration
    boolean enableSkinManager = false;
    boolean enableEnderPearlChunkManager = false;
    boolean enableMossBlockManager = true;
    boolean enableLootMobTargetManager = true;
    boolean enableDesirePathManager = true;
    boolean enableProjectileDamageManager = true;
    boolean enableServerStatsManager = false;
    boolean enableReachAroundBlockManager = true;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers first
        if (enableSkinManager) {
            skinManager = new SkinManager();
            BasicCommand skinCommand = new SkinCommand(skinManager);
            registerCommand("midnightpatch", skinCommand);
            this.getServer().getPluginManager().registerEvents(skinManager, this);
            ComponentLogger.logger().info("enableSkinManager = true");
        } else {
            ComponentLogger.logger().info("enableSkinManager = false");
        }

        if (enableEnderPearlChunkManager) {
            enderPearlChunkManager = new EnderPearlChunkManager();
            this.getServer().getPluginManager().registerEvents(enderPearlChunkManager, this);
            ComponentLogger.logger().info("enderPearlChunkManager = true");
        } else {
            ComponentLogger.logger().info("enderPearlChunkManager = false");
        }

        if (enableMossBlockManager) {
            mossBlockManager = new MossBlockManager();
            this.getServer().getPluginManager().registerEvents(mossBlockManager, this);
            ComponentLogger.logger().info("enableMossBlockManager = true");
        } else {
            ComponentLogger.logger().info("enableMossBlockManager = false");
        }

        if (enableLootMobTargetManager) {
            lootMobTargetManager = new LootMobTargetManager();
            this.getServer().getPluginManager().registerEvents(lootMobTargetManager, this);
            ComponentLogger.logger().info("enableLootMobTargetManager = true");
        } else {
            ComponentLogger.logger().info("enableLootMobTargetManager = false");
        }

        if (enableDesirePathManager) {
            desirePathManager = new DesirePathManager();
            this.getServer().getPluginManager().registerEvents(desirePathManager, this);
            ComponentLogger.logger().info("enableDesirePathManager = true");
        } else {
            ComponentLogger.logger().info("enableDesirePathManager = false");
        }

        if (enableProjectileDamageManager) {
            projectileDamageManager = new ProjectileDamageManager();
            this.getServer().getPluginManager().registerEvents(projectileDamageManager, this);
            ComponentLogger.logger().info("enableProjectileDamageManager = true");
        } else {
            ComponentLogger.logger().info("enableProjectileDamageManager = false");
        }

        if (enableServerStatsManager) {
            serverStatsManager = new ServerStatsManager();
            serverStatsManager.enable();
            ComponentLogger.logger().info("enableServerStatsManager = true");
        } else {
            ComponentLogger.logger().info("enableServerStatsManager = false");
        }

        if (enableReachAroundBlockManager) {
            reachAroundBlockManager = new ReachAroundBlockManager();
            this.getServer().getPluginManager().registerEvents(reachAroundBlockManager, this);
            ComponentLogger.logger().info("enableReachAroundBlockManager = true");
        } else {
            ComponentLogger.logger().info("enableReachAroundBlockManager = false");
        }

        // Register /kill command
        BasicCommand killCommand = new KillCommand();
        registerCommand("kill", killCommand);
        
        // Register /togglereacharound command
        BasicCommand toggleReachAroundCommand = new ToggleReachAroundCommand();
        registerCommand("togglereacharound", toggleReachAroundCommand);
    }

    @Override
    public void onDisable() {
        if (enderPearlChunkManager != null) {
            enderPearlChunkManager.shutdown();
        }
        if (serverStatsManager != null) {
            serverStatsManager.disable();
        }
    }
}
