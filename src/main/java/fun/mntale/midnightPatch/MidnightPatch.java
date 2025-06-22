package fun.mntale.midnightPatch;

import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.command.brigadier.BasicCommand;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import fun.mntale.midnightPatch.command.KillCommand;
import fun.mntale.midnightPatch.command.ToggleReachAroundCommand;
import fun.mntale.midnightPatch.module.world.reacharound.ReachAroundBlockListener;
import fun.mntale.midnightPatch.module.entity.minecart.MinecartChunkLoadListener;
import fun.mntale.midnightPatch.module.world.fertilizer.FertilizerListener;
import fun.mntale.midnightPatch.module.entity.loot.LootMobTargetListener;
import fun.mntale.midnightPatch.module.entity.babymob.BabyMobListener;
import fun.mntale.midnightPatch.module.world.desirepath.DesirePathListener;
import fun.mntale.midnightPatch.module.world.fertilizer.MossBlockFertilizerListener;
import fun.mntale.midnightPatch.module.world.loot.FrostbiteLoot;
import fun.mntale.midnightPatch.module.world.loot.HarvestingLoot;
import fun.mntale.midnightPatch.module.world.loot.RecastingLoot;
import fun.mntale.midnightPatch.module.world.loot.UpdraftLoot;
import fun.mntale.midnightPatch.module.entity.armorstand.PoseArmorStandListener;
import fun.mntale.midnightPatch.module.entity.projectile.ProjectileDamageListener;
import fun.mntale.midnightPatch.module.world.enchantment.FrostbiteEnchantment;
import fun.mntale.midnightPatch.module.world.enchantment.UpdraftEnchantment;
import fun.mntale.midnightPatch.module.world.enchantment.HarvestingEnchantment;
import fun.mntale.midnightPatch.module.world.enchantment.RecastingEnchantment;


public final class MidnightPatch extends JavaPlugin {
    public static MidnightPatch instance;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new ReachAroundBlockListener(), this);
        BasicCommand ToggleReachAroundCommand = new ToggleReachAroundCommand();
        registerCommand("togglereacharound", ToggleReachAroundCommand);

        getServer().getPluginManager().registerEvents(new MinecartChunkLoadListener(), this);
        getServer().getPluginManager().registerEvents(new FertilizerListener(), this);
        getServer().getPluginManager().registerEvents(new LootMobTargetListener(), this);
        getServer().getPluginManager().registerEvents(new BabyMobListener(), this);
        getServer().getPluginManager().registerEvents(new DesirePathListener(), this);
        getServer().getPluginManager().registerEvents(new MossBlockFertilizerListener(), this);
        getServer().getPluginManager().registerEvents(new PoseArmorStandListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileDamageListener(), this);

        getServer().getPluginManager().registerEvents(new FrostbiteEnchantment(), this);
        getServer().getPluginManager().registerEvents(new FrostbiteLoot(), this);

        getServer().getPluginManager().registerEvents(new UpdraftEnchantment(), this);
        getServer().getPluginManager().registerEvents(new RecastingLoot(), this);

        getServer().getPluginManager().registerEvents(new HarvestingEnchantment(), this);
        getServer().getPluginManager().registerEvents(new HarvestingLoot(), this);

        getServer().getPluginManager().registerEvents(new RecastingEnchantment(), this);
        getServer().getPluginManager().registerEvents(new UpdraftLoot(), this);
        
        BasicCommand killCommand = new KillCommand();
        registerCommand("kill", killCommand);
    }

    @Override
    public void onDisable() {
    }
}
