package com.sandevistanmod.robot;

import com.sandevistanmod.SandevistanMod;
import com.sandevistanmod.registry.ModRegistries;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;

@Mod.EventBusSubscriber(modid = SandevistanMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RobotEvents {
    private RobotEvents() {}

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent e) {
        e.put(ModRegistries.ROBO_ZUMBI.get(), RoboZombie.attrs().build());
        e.put(ModRegistries.ROBO_CREEPER.get(), RoboCreeper.attrs().build());
        e.put(ModRegistries.ROBO_ESQUELETO.get(), RoboSkeleton.attrs().build());
        e.put(ModRegistries.ROBO_CAO.get(), RoboDog.attrs().build());
        e.put(ModRegistries.ROBO_SEGURANCA.get(), RoboSecurity.attrs().build());
        e.put(ModRegistries.ROBO_DRONE.get(), RoboDrone.attrs().build());
    }

    @SubscribeEvent
    public static void placements(SpawnPlacementRegisterEvent e) {
        e.register(ModRegistries.ROBO_ZUMBI.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        e.register(ModRegistries.ROBO_CREEPER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        e.register(ModRegistries.ROBO_ESQUELETO.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        e.register(ModRegistries.ROBO_CAO.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        e.register(ModRegistries.ROBO_SEGURANCA.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        e.register(ModRegistries.ROBO_DRONE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
    }
}
