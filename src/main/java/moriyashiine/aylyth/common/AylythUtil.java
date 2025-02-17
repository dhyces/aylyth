package moriyashiine.aylyth.common;

import moriyashiine.aylyth.common.registry.ModPotions;
import moriyashiine.aylyth.common.registry.ModSoundEvents;
import moriyashiine.aylyth.common.registry.ModTags;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

public class AylythUtil {
	public static final int MAX_TRIES = 8;
	
	public static BlockPos getSafePosition(World world, BlockPos.Mutable pos, int tries) {
		if (tries >= MAX_TRIES) {
			return world.getSpawnPos();
		}
		pos.setY(world.getTopY() - 1);
		while (world.isInBuildLimit(pos) && !world.getBlockState(pos).shouldSuffocate(world, pos)) {
			pos.setY(pos.getY() - 1);
		}
		while (world.isInBuildLimit(pos) && world.getBlockState(pos).shouldSuffocate(world, pos)) {
			pos.setY(pos.getY() + 2);
		}
		if (world.getBlockState(pos).getMaterial().isReplaceable() && world.getFluidState(pos).getFluid() == Fluids.EMPTY) {
			return pos.toImmutable();
		}
		return getSafePosition(world, pos.set(MathHelper.nextInt(world.random, pos.getX() - 32, pos.getX() + 32) + 0.5, world.getTopY() - 1, MathHelper.nextInt(world.random, pos.getZ() - 32, pos.getZ() + 32) + 0.5), ++tries);
	}
	
	public static void teleportTo(RegistryKey<World> world, LivingEntity living, int tries) {
		living.world.playSoundFromEntity(living instanceof PlayerEntity player ? player : null, living, ModSoundEvents.ENTITY_GENERIC_SHUCKED, SoundCategory.PLAYERS, 1, living.getSoundPitch());
		ServerWorld toWorld = living.world.getServer().getWorld(world);
		FabricDimensions.teleport(living, toWorld, new TeleportTarget(Vec3d.of(AylythUtil.getSafePosition(toWorld, living.getBlockPos().mutableCopy(), tries)), Vec3d.ZERO, living.headYaw, living.getPitch()));
		toWorld.playSoundFromEntity(null, living, ModSoundEvents.ENTITY_GENERIC_SHUCKED, SoundCategory.PLAYERS, 1, living.getSoundPitch());
	}

	public static boolean shouldUndeadAttack(LivingEntity target, LivingEntity attacker) {
		return attacker.getAttacker() != target && target.hasStatusEffect(ModPotions.CIMMERIAN_EFFECT) && attacker.getGroup() == EntityGroup.UNDEAD;
	}

	public static boolean isNearSeep(LivingEntity livingEntity, int radius) {
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					if (livingEntity.world.getBlockState(mutable.set(livingEntity.getX() + x, livingEntity.getY() + y, livingEntity.getZ() + z)).isIn(ModTags.SEEPS)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static double distanceIfNearSeep(LivingEntity livingEntity, int radius) {
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					mutable.set(livingEntity.getX() + x, livingEntity.getY() + y, livingEntity.getZ() + z);
					if (livingEntity.world.getBlockState(mutable).isIn(ModTags.SEEPS)) {
						return Math.sqrt(mutable.getSquaredDistance(livingEntity.getBlockPos().getX(), livingEntity.getBlockPos().getY(), livingEntity.getBlockPos().getZ()));
					}
				}
			}
		}
		return -1;
	}
}
