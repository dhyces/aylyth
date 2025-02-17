package moriyashiine.aylyth.common.registry;

import moriyashiine.aylyth.common.Aylyth;
import moriyashiine.aylyth.common.entity.mob.*;
import moriyashiine.aylyth.common.entity.passive.PilotLightEntity;
import moriyashiine.aylyth.common.entity.projectile.YmpeLanceEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;

public class ModEntityTypes {
	public static final EntityType<PilotLightEntity> PILOT_LIGHT = FabricEntityTypeBuilder.create(SpawnGroup.AMBIENT, PilotLightEntity::new).dimensions(EntityDimensions.fixed(0.5F, 0.5F)).build();
	public static final EntityType<AylythianEntity> AYLYTHIAN = FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, AylythianEntity::new).dimensions(EntityDimensions.fixed(0.6F, 2.3F)).build();
	public static final EntityType<ElderAylythianEntity> ELDER_AYLYTHIAN = FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ElderAylythianEntity::new).dimensions(EntityDimensions.fixed(1.4F, 2.3F)).build();
	public static final EntityType<YmpeLanceEntity> YMPE_LANCE = FabricEntityTypeBuilder.<YmpeLanceEntity>create(SpawnGroup.MISC, YmpeLanceEntity::new).dimensions(EntityDimensions.fixed(0.5F, 0.5F)).build();

	public static final EntityType<SoulmouldEntity> SOULMOULD = FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, SoulmouldEntity::new).dimensions(EntityDimensions.fixed(0.85F, 2.7F)).fireImmune().build();
	public static final EntityType<BoneflyEntity> BONEFLY = FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, BoneflyEntity::new).dimensions(EntityDimensions.fixed(1.4F, 2.1F)).fireImmune().build();

	public static final EntityType<ScionEntity> SCION = FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ScionEntity::new).dimensions(EntityDimensions.fixed(1.4F, 2.1F)).fireImmune().build();

	public static void init() {
		FabricDefaultAttributeRegistry.register(PILOT_LIGHT, PilotLightEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(AYLYTHIAN, AylythianEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ELDER_AYLYTHIAN, ElderAylythianEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(SOULMOULD, SoulmouldEntity.createSoulmouldAttributes());
		FabricDefaultAttributeRegistry.register(BONEFLY, BoneflyEntity.createBoneflyAttributes());
		FabricDefaultAttributeRegistry.register(SCION, ScionEntity.createAttributes());
		Registry.register(Registry.ENTITY_TYPE, new Identifier(Aylyth.MOD_ID, "pilot_light"), PILOT_LIGHT);
		Registry.register(Registry.ENTITY_TYPE, new Identifier(Aylyth.MOD_ID, "aylythian"), AYLYTHIAN);
		Registry.register(Registry.ENTITY_TYPE, new Identifier(Aylyth.MOD_ID, "elder_aylythian"), ELDER_AYLYTHIAN);
		Registry.register(Registry.ENTITY_TYPE, new Identifier(Aylyth.MOD_ID, "ympe_lance"), YMPE_LANCE);
		Registry.register(Registry.ENTITY_TYPE, new Identifier(Aylyth.MOD_ID, "soulmould"), SOULMOULD);
		Registry.register(Registry.ENTITY_TYPE, new Identifier(Aylyth.MOD_ID, "bonefly"), BONEFLY);
		Registry.register(Registry.ENTITY_TYPE, new Identifier(Aylyth.MOD_ID, "scion"), SCION);
		
		SpawnRestriction.register(PILOT_LIGHT, SpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING, PilotLightEntity::canSpawn);
		SpawnRestriction.register(AYLYTHIAN, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AylythianEntity::canSpawn);
		SpawnRestriction.register(ELDER_AYLYTHIAN, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AylythianEntity::canSpawn);
	}
}
