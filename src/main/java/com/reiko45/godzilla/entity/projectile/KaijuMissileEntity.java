package com.reiko45.godzilla.entity.projectile;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.entity.PartEntity;

/**
 * A rocket fired by a kaiju or mecha. The payload decides what it does:
 * <ul>
 *     <li>EXPLOSIVE: a small explosion (Showa Mechagodzilla's finger missiles).</li>
 *     <li>PARALYZER: stuns and slows (Heisei Mechagodzilla).</li>
 *     <li>CADMIUM: shuts down regeneration and nuclear attacks (Super X).</li>
 *     <li>FREEZER: fills the freeze meter (Super X3).</li>
 *     <li>D03: the Satsuma's D-03 drill missile. Devastating fired into an open wound (GMK Godzilla).</li>
 *     <li>RAILGUN: Kiryu's Type-0 railgun rounds: no explosion, just a heavy hit.</li>
 *     <li>LASER: a burning laser bolt (the Godzilla Tower, Xilien saucers).</li>
 *     <li>MOP: a bunker-busting bomb (Shin Godzilla): pierces armour and can break dorsal plates.</li>
 *     <li>COAGULANT: a blood-coagulant shell: no blast, but it feeds a blood-cooled kaiju's freeze.</li>
 *     <li>EMP_PROBE: an EMP probe that sticks into Godzilla Earth's dorsal fins.</li>
 * </ul>
 */
public class KaijuMissileEntity extends ThrowableItemProjectile {
    public enum Payload { EXPLOSIVE, PARALYZER, CADMIUM, FREEZER, D03, RAILGUN, LASER, MOP, COAGULANT, EMP_PROBE }

    private static final EntityDataAccessor<Integer> DATA_PAYLOAD = SynchedEntityData.defineId(KaijuMissileEntity.class, EntityDataSerializers.INT);

    public KaijuMissileEntity(EntityType<? extends KaijuMissileEntity> type, Level level) {
        super(type, level);
    }

    public KaijuMissileEntity(Level level, LivingEntity shooter) {
        this(level, shooter, Payload.EXPLOSIVE);
    }

    public KaijuMissileEntity(Level level, LivingEntity shooter, Payload payload) {
        super(ModEntities.KAIJU_MISSILE.get(), shooter, level);
        entityData.set(DATA_PAYLOAD, payload.ordinal());
    }

    /** A missile with no shooter (the Godzilla Tower's laser). */
    public KaijuMissileEntity(Level level, double x, double y, double z, Payload payload) {
        super(ModEntities.KAIJU_MISSILE.get(), x, y, z, level);
        entityData.set(DATA_PAYLOAD, payload.ordinal());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PAYLOAD, 0);
    }

    public Payload getPayload() {
        return Payload.values()[Math.floorMod(entityData.get(DATA_PAYLOAD), Payload.values().length)];
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            ParticleOptions trail = switch (getPayload()) {
                case FREEZER -> ParticleTypes.SNOWFLAKE;
                case CADMIUM -> ParticleTypes.HAPPY_VILLAGER;
                case PARALYZER, RAILGUN -> ParticleTypes.ELECTRIC_SPARK;
                case LASER -> ParticleTypes.CRIMSON_SPORE;
                case COAGULANT -> ParticleTypes.FALLING_DRIPSTONE_LAVA;
                case EMP_PROBE -> ParticleTypes.ELECTRIC_SPARK;
                default -> ParticleTypes.FLAME;
            };
            level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0, 0);
            level().addParticle(trail, getX(), getY(), getZ(), 0, 0, 0);
        } else if (tickCount > 140) {
            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target instanceof PartEntity<?> part && part.getParent() == getOwner()) return false;
        if (getOwner() != null && target.isPassengerOfSameVehicle(getOwner())) return false;
        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Payload payload = getPayload();
        float damage = switch (payload) {
            case EXPLOSIVE -> 6.0f;
            case D03 -> 14.0f;
            case RAILGUN -> 10.0f;
            case LASER -> 5.0f;
            case MOP -> 40.0f;
            case COAGULANT, EMP_PROBE -> 1.0f;
            default -> 4.0f;
        };
        net.minecraft.world.entity.Entity hit = result.getEntity();
        String part = hit instanceof com.reiko45.godzilla.entity.KaijuPart p ? p.name : null;
        KaijuEntity kaiju = hit instanceof KaijuEntity k ? k : hit instanceof com.reiko45.godzilla.entity.KaijuPart p ? p.getParent() : null;
        if (kaiju != null) damage *= kaiju.missileDamageMultiplier(payload, part);
        if (payload == Payload.LASER) hit.igniteForSeconds(4);
        if (kaiju != null) kaiju.onSpecialPayload(payload, part, getOwner());
        hit.hurt(payload == Payload.RAILGUN || payload == Payload.LASER ? damageSources().mobProjectile(this, getOwner() instanceof LivingEntity l ? l : null)
                : damageSources().explosion(this, getOwner()), damage);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(level() instanceof ServerLevel server)) return;
        Payload payload = getPayload();
        boolean grief = payload == Payload.EXPLOSIVE && getOwner() != null && KaijuDestruction.canGrief(server, getOwner());
        if (payload == Payload.D03 || payload == Payload.MOP) grief = getOwner() != null && KaijuDestruction.canGrief(server, getOwner());
        if (payload == Payload.COAGULANT || payload == Payload.EMP_PROBE) {
            server.sendParticles(payload == Payload.COAGULANT ? ParticleTypes.FALLING_DRIPSTONE_LAVA : ParticleTypes.ELECTRIC_SPARK,
                    getX(), getY(), getZ(), 20, 0.6, 0.6, 0.6, 0.1);
            discard();
            return;
        }
        if (payload != Payload.FREEZER && payload != Payload.RAILGUN && payload != Payload.LASER) {
            float power = payload == Payload.MOP ? 5.0f : payload == Payload.D03 ? 3.0f : payload == Payload.EXPLOSIVE ? 2.0f : 1.0f;
            server.explode(this, getX(), getY(), getZ(), power, false, grief ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
        }
        if (payload == Payload.RAILGUN || payload == Payload.LASER) {
            server.sendParticles(payload == Payload.LASER ? ParticleTypes.FLAME : ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 12, 0.3, 0.3, 0.3, 0.1);
            discard();
            return;
        }
        for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.0), e -> e != getOwner())) {
            KaijuEntity kaiju = e instanceof KaijuEntity k ? k : null;
            switch (payload) {
                case PARALYZER -> {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                    if (kaiju != null && kaiju.isAbilityActive() && kaiju.getRandom().nextInt(3) == 0) kaiju.stopAbility();
                }
                case CADMIUM -> {
                    if (kaiju != null) kaiju.applyCadmium(1200);
                }
                case FREEZER -> {
                    if (kaiju != null) kaiju.addFreeze(18f);
                    else e.setTicksFrozen(Math.min(e.getTicksFrozen() + 160, 400));
                }
                default -> {}
            }
        }
        if (payload == Payload.FREEZER) server.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY(), getZ(), 40, 1.5, 1.5, 1.5, 0.05);
        if (payload == Payload.CADMIUM) server.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY(), getZ(), 25, 1.5, 1.5, 1.5, 0.05);
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Payload", entityData.get(DATA_PAYLOAD));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_PAYLOAD, tag.getInt("Payload"));
    }
}
