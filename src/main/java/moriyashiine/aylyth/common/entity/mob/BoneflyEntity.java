package moriyashiine.aylyth.common.entity.mob;

import blue.endless.jankson.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import moriyashiine.aylyth.common.component.entity.RiderComponent;
import moriyashiine.aylyth.common.registry.ModComponents;
import moriyashiine.aylyth.common.registry.ModItems;
import moriyashiine.aylyth.mixin.EntityAccessor;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BoneflyEntity extends HostileEntity implements IAnimatable, TameableHostileEntity {
    private final AnimationFactory factory = new AnimationFactory(this);
    protected static final TrackedData<Boolean> DORMANT;
    public static final TrackedData<Integer> ACTION_STATE;
    private static final TrackedData<Optional<UUID>> OWNER_UUID;
    public int stabTicks = 0;

    public BoneflyEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.stepHeight = 1.5F;
        this.setPersistent();
    }

    @Override
    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        return false;
    }

    public static DefaultAttributeContainer.Builder createBoneflyAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.4)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_ARMOR, 24.0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(DORMANT, false);
        this.dataTracker.startTracking(ACTION_STATE, 0);
        this.dataTracker.startTracking(OWNER_UUID, Optional.of(UUID.fromString("1ece513b-8d36-4f04-9be2-f341aa8c9ee2")));
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void initGoals() {
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.getOwnerUuid() != null) {
            nbt.putUuid("Owner", this.getOwnerUuid());
        }

        nbt.putInt("ActionState", this.getActionState());
        nbt.putBoolean("Dormant", this.isDormant());
        nbt.putInt("stabTicks", this.stabTicks);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        UUID ownerUUID;
        if (nbt.containsUuid("Owner")) {
            ownerUUID = nbt.getUuid("Owner");
        } else {
            String string = nbt.getString("Owner");
            ownerUUID = ServerConfigHandler.getPlayerUuidByName(this.getServer(), string);
        }

        this.setActionState(nbt.getInt("ActionState"));
        if (ownerUUID != null) {
            try {
                this.setOwnerUuid(ownerUUID);
                this.setTamed(true);
            } catch (Throwable var4) {
                this.setTamed(false);
            }
        }

        this.setDormant(nbt.getBoolean("Dormant"));
        this.stabTicks = nbt.getInt("stabTicks");
    }

    public boolean isDormant() {
        return (Boolean)this.getDataTracker().get(DORMANT);
    }

    @Override
    protected void addFlapEffects() {
        this.playSound(SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 1.0F);
    }

    @Override
    protected boolean hasWings() {
        return this.isInAir();
    }

    @Override
    public EntityGroup getGroup() {
        return EntityGroup.UNDEAD;
    }

    public int getActionState() {
        return (Integer)this.dataTracker.get(ACTION_STATE);
    }

    public void setActionState(int i) {
        this.dataTracker.set(ACTION_STATE, i);
    }

    public void setDormant(boolean rest) {
        this.getDataTracker().set(DORMANT, rest);
    }

    @Override
    public void travel(Vec3d travelVector) {
        boolean flying = this.isInAir();
        float speed = (float)this.getAttributeValue(flying ? EntityAttributes.GENERIC_FLYING_SPEED : EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (!this.hasPassengers() && !this.canBeControlledByRider()) {
            this.airStrafingSpeed = 0.02F;
            super.travel(travelVector);
        } else {
            LivingEntity passenger = (LivingEntity)this.getFirstPassenger();
            if (!(passenger instanceof PlayerEntity)) {
                if (passenger != null) {
                    passenger.dismountVehicle();
                }
            }

            if (passenger instanceof PlayerEntity) {
                this.headYaw = (float)this.serverYaw;
                this.serverHeadYaw = (double)this.headYaw;
                this.serverYaw -= (double)(passenger.sidewaysSpeed * 2.0F);
                this.serverPitch = (double)(passenger.getPitch() * 0.5F);
                boolean isPlayerUpwardsMoving = ((RiderComponent)ModComponents.RIDER_COMPONENT.get(passenger)).isPressingUp();
                boolean isPlayerDownwardsMoving = ((RiderComponent)ModComponents.RIDER_COMPONENT.get(passenger)).isPressingDown();
                double getFlightDelta = isPlayerUpwardsMoving && isPlayerDownwardsMoving ? 0.0 : (isPlayerUpwardsMoving ? 0.8 : (isPlayerDownwardsMoving ? -0.6 : 0.0));
                this.setPitch((float)this.serverPitch);
                this.setYaw((float)this.serverYaw);
                this.setRotation(this.getYaw(), this.getPitch());
                this.bodyYaw = (float)this.serverYaw;
                if (!flying) {
                    this.setActionState(0);
                }

                if (this.hasPassengers() && isPlayerDownwardsMoving && isPlayerUpwardsMoving && this.isInAir()) {
                    if (this.getActionState() != 0 && this.getActionState() != 2) {
                        this.setActionState(0);
                        if (this.getPassengerList().size() > 1) {
                            ((Entity)this.getPassengerList().get(1)).dismountVehicle();
                        }
                    } else {
                        this.setActionState(1);
                    }
                }

                if (!flying && isPlayerUpwardsMoving) {
                    this.jump();
                }

                if (this.getFirstPassenger() != null) {
                    travelVector = new Vec3d(0.0, getFlightDelta, (double)(passenger.forwardSpeed * (flying ? 1.0F : 0.4F)));
                    this.setMovementSpeed(speed);
                    this.stepBobbingAmount = 0.0F;
                } else {
                    this.updateLimbs(this, false);
                    this.setVelocity(Vec3d.ZERO);
                    return;
                }
            }

            if (flying) {
                this.applyMovementInput(travelVector, speed);
                this.move(MovementType.SELF, this.getVelocity());
                this.setVelocity(this.getVelocity().multiply(0.9100000262260437));
                this.updateLimbs(this, false);
                this.updatePositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
            } else {
                super.travel(travelVector);
                this.updateLeash();
            }

        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age % 20 == 0 && this.getHealth() < this.getMaxHealth() && this.isDormant()) {
            this.heal(2.0F);
        }

        if (this.isDormant()) {
            this.setVelocity(0.0, this.getVelocity().y, 0.0);
            this.setPitch(0.0F);
        }

        if (this.getActionState() == 1) {
            ++this.stabTicks;
            if (this.stabTicks >= 10) {
                this.setActionState(2);
                if (!this.getWorld().isClient() && this.getWorld().getEntitiesByClass(LivingEntity.class, this.getBoundingBox().offset(0.0, -2.0, 0.0).expand(1.0), (entity) -> {
                    return entity != this;
                }).size() > 0 && this.getPassengerList().size() <= 1) {
                    LivingEntity livingEntity = this.getWorld().getClosestEntity(this.getWorld().getEntitiesByClass(LivingEntity.class, this.getBoundingBox().offset(0.0, -2.0, 0.0).expand(1.0), (entity) -> {
                        return entity != this;
                    }), TargetPredicate.createAttackable(), this, this.getX(), this.getY(), this.getZ());
                    if (livingEntity != null) {
                        if (!(livingEntity instanceof PlayerEntity player)) {
                            this.tryAttack(livingEntity);
                            livingEntity.startRiding(this, true);
                        } else {
                            this.tryAttack(livingEntity);
                            this.playerStartRiding(this, player);
                        }
                    }
                }

                this.stabTicks = 0;
            }
        }

    }

    private void playerStartRiding(Entity entity, PlayerEntity player) {
        if (entity != player.getVehicle()) {
            for(Entity entity2 = entity; entity2.getVehicle() != null; entity2 = entity2.getVehicle()) {
                if (entity2.getVehicle() == this) {
                    return;
                }
            }

            if (this.hasVehicle()) {
                player.stopRiding();
            }

            this.setPose(EntityPose.STANDING);
            player.vehicle = entity;
            this.boneflyAddPassenger(player, player.getVehicle());
            ((EntityAccessor)entity).mason$streamIntoPassengers().filter((passenger) -> {
                return passenger instanceof ServerPlayerEntity;
            }).forEach((playr) -> {
                Criteria.STARTED_RIDING.trigger((ServerPlayerEntity)playr);
            });
        }
    }

    protected void boneflyAddPassenger(Entity passenger, Entity adder) {
        if (passenger.getVehicle() != this) {
            throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
        } else {
            if (adder.passengerList.isEmpty()) {
                adder.passengerList = ImmutableList.of(passenger);
            } else {
                List<Entity> list = Lists.newArrayList(adder.passengerList);
                list.add(passenger);
                adder.passengerList = ImmutableList.copyOf(list);
            }

        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!this.world.isClient() && source.getAttacker() != null) {
            Entity var4 = source.getAttacker();
            if (var4 instanceof PlayerEntity player) {
                if (player.isHolding(ModItems.SOULTRAP_EFFIGY_ITEM)) {
                    this.setOwner(player);
                }
            }
        }

        return super.damage(source, amount);
    }

    public boolean isInAir() {
        return this.isHighEnough((int)this.stepHeight + 1);
    }

    public boolean isHighEnough(int altitude) {
        return this.getAltitude(altitude) >= (double)altitude;
    }

    public double getAltitude(int limit) {
        BlockPos.Mutable mutable = this.getBlockPos().mutableCopy();

        for(int i = 0; i <= limit && mutable.getY() > 0 && !this.world.getBlockState(mutable.move(Direction.DOWN)).getMaterial().blocksMovement(); ++i) {
        }

        return this.getY() - (double)mutable.getY() - 0.11;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if ((stack.getItem().equals(Items.BONE_BLOCK) || stack.isIn(ItemTags.SOUL_FIRE_BASE_BLOCKS)) && this.getHealth() < this.getMaxHealth()) {
            stack.decrement(1);
            this.heal(1.0F);
        }

        if (this.isOwner(player) && !this.isBaby() && stack.isEmpty() && this.isTamed() && !this.hasPassengers()) {
            if (player.isSneaking()) {
                this.setDormant(!this.isDormant());
            } else {
                player.startRiding(this);
                this.navigation.stop();
            }
        }

        return super.interactMob(player, hand);
    }

    @Override
    public void updatePassengerPosition(Entity passenger) {
        if (this.hasPassenger(passenger)) {
            if (this.getFirstPassenger() == passenger) {
                float f = 0.5F;
                float g = (float)((this.isRemoved() ? 0.009999999776482582 : this.getMountedHeightOffset()) + passenger.getHeightOffset());
                Vec3d vec3d = (new Vec3d((double)f, 0.0, 0.0)).rotateY(-this.getYaw() * 0.017453292F - 1.5707964F);
                passenger.setPosition(this.getX() + vec3d.x, this.getY() + (double)g, this.getZ() + vec3d.z);
                passenger.setYaw(passenger.getYaw());
                passenger.setHeadYaw(passenger.getHeadYaw());
            } else {
                passenger.setPosition(this.getX(), this.getY() - 0.5, this.getZ());
                passenger.setYaw(this.getYaw());
                passenger.setHeadYaw(this.getHeadYaw());
            }

        }
    }

    @Override
    public double getMountedHeightOffset() {
        return 2.3;
    }

    public boolean canBeControlledByRider() {
        return this.getFirstPassenger() instanceof LivingEntity;
    }

    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "wingController", 5.0F, this::wingPredicate));
        animationData.addAnimationController(new AnimationController<>(this, "idleController", 1.0F, this::idlePredicate));
        animationData.addAnimationController(new AnimationController<>(this, "stabController", 5.0F, this::stabPredicate));
    }

    private <E extends IAnimatable> PlayState wingPredicate(AnimationEvent<E> event) {
        AnimationBuilder animationBuilder = new AnimationBuilder();
        if (this.isInAir()) {
            animationBuilder.addAnimation("idleFly", true);
        } else if (event.isMoving()) {
            animationBuilder.addAnimation("walkGround", true);
        } else {
            animationBuilder.addAnimation("idleGround", true);
        }

        if (!animationBuilder.getRawAnimationList().isEmpty()) {
            event.getController().setAnimation(animationBuilder);
        }

        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState stabPredicate(AnimationEvent<E> event) {
        AnimationBuilder animationBuilder = new AnimationBuilder();
        if (this.getActionState() == 2) {
            animationBuilder.addAnimation("stabIdle", true);
        } else {
            if (this.getActionState() != 1) {
                return PlayState.STOP;
            }

            animationBuilder.addAnimation("stab", false);
        }

        if (!animationBuilder.getRawAnimationList().isEmpty()) {
            event.getController().setAnimation(animationBuilder);
        }

        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState idlePredicate(AnimationEvent<E> event) {
        AnimationBuilder animationBuilder = new AnimationBuilder();
        if (this.isDormant()) {
            animationBuilder.addAnimation("idleDormant", true);
        } else {
            if (this.hurtTime <= 0 && this.deathTime <= 0) {
                return PlayState.STOP;
            }

            animationBuilder.addAnimation("hurt", true);
        }

        if (!animationBuilder.getRawAnimationList().isEmpty()) {
            event.getController().setAnimation(animationBuilder);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public UUID getOwnerUuid() {
        return (UUID)((Optional)this.dataTracker.get(OWNER_UUID)).orElse(null);
    }

    @Override
    public void setOwnerUuid(@Nullable UUID uuid) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Override
    public void setOwner(PlayerEntity player) {
        this.setTamed(true);
        this.setOwnerUuid(player.getUuid());
    }

    @Override
    @Nullable
    public LivingEntity getOwner() {
        try {
            UUID uUID = this.getOwnerUuid();
            return uUID == null ? null : this.world.getPlayerByUuid(uUID);
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }

    @Override
    public boolean isOwner(LivingEntity entity) {
        return entity == this.getOwner();
    }

    @Override
    public boolean isTamed() {
        return true;
    }

    @Override
    public void setTamed(boolean tamed) {
    }

    static {
        DORMANT = DataTracker.registerData(BoneflyEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        ACTION_STATE = DataTracker.registerData(BoneflyEntity.class, TrackedDataHandlerRegistry.INTEGER);
        OWNER_UUID = DataTracker.registerData(BoneflyEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    }
}
