package com.reiko45.godzilla.entity.reiwa;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.*;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import com.reiko45.godzilla.world.InstantBeam;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shin Godzilla (2016). It comes ashore as a gilled, crawling 2nd form, stands up as the 3rd form, and becomes the
 * towering 4th form: a walking nuclear reactor that evolves in hours.
 * <ul>
 *     <li><b>Evolution:</b> time spent on land and damage taken fill an evolution meter; when it's full the creature
 *     stops, steams and molts into its next form.</li>
 *     <li><b>Reactor energy:</b> its breath (black smoke, then a fire spray, then a focused purple beam), its dorsal
 *     beams (which shoot down anything in the air, drones included) and its tail beam all burn energy. Run it dry and
 *     it shuts down into dormancy, standing still for a long time.</li>
 *     <li><b>Operation Yashiori:</b> conventional weapons barely scratch it. Knock it down (train bombs, demolition
 *     charges) or catch it dormant, then pump blood coagulant into its mouth. At 100% it freezes solid for good.</li>
 *     <li>It adapts: after soaking up enough of one kind of damage it resists it. MOP bunker busters on its back can
 *     break dorsal plates, and breaking the tail tip silences the tail beam.</li>
 * </ul>
 */
public class ShinGodzillaEntity extends KaijuEntity {
    public static final int SECOND = 0, THIRD = 1, FOURTH = 2;
    public static final int NORMAL = 0, MOLTING = 1, DORMANT = 2, KNOCKED_DOWN = 3, FROZEN = 4;
    private static final EntityDimensions[] DIMENSIONS = {EntityDimensions.scalable(5.0f, 4.2f), EntityDimensions.scalable(3.0f, 10.0f),
            EntityDimensions.scalable(3.6f, 11.0f)};
    private static final EntityDataAccessor<Float> DATA_ENERGY = SynchedEntityData.defineId(ShinGodzillaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_COAGULANT = SynchedEntityData.defineId(ShinGodzillaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(ShinGodzillaEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation DORMANT_ANIM = RawAnimation.begin().thenLoop("dormant");
    private static final RawAnimation MOLT_ANIM = RawAnimation.begin().thenLoop("charge");

    private float evolution;
    private int stateTicks;
    private float knockdown;
    private int platesBroken;
    private boolean tailBroken;
    private float tailDamage;
    private final Map<String, Float> damageTaken = new HashMap<>();
    private final Set<String> adapted = new HashSet<>();
    private int lastMop = -100;

    public ShinGodzillaEntity(EntityType<? extends ShinGodzillaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 900;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1400, 20, 12, 30, 0.18);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    /** It never hunts people: it walks where it walks and destroys whatever gets in the way, or shoots at it. */
    @Override
    protected boolean attacksPlayersOnSight() {
        return false;
    }

    @Override
    public boolean isGodzilla() {
        return true;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("atomic_breath", BeamStyle.SHIN, 1, 0.9f, ModDamageTypes.SHIN_BREATH, 90, 70, 1200) {
            @Override
            public void tickCharge(KaijuEntity k, @Nullable LivingEntity target, int tick) {
                super.tickCharge(k, target, tick);
                ((ShinGodzillaEntity) k).breathCharge(target, tick);
            }

            @Override
            public void activate(KaijuEntity k, @Nullable LivingEntity target) {
                super.activate(k, target);
                ((ShinGodzillaEntity) k).spendEnergy(35);
            }
        }.damage(22f, 3).blocks(2.6f, true).sounds(ModSounds.SHIN_ROAR, ModSounds.ATOMIC_BREATH_FIRE).chargeParticle(ParticleTypes.DRAGON_BREATH)
                .nuclear().range(6, 96).when(k -> ((ShinGodzillaEntity) k).canAct() && k.getVariant() == FOURTH && ((ShinGodzillaEntity) k).getEnergy() >= 35));
        list.add(new BeamAbility("tail_beam", BeamStyle.DORSAL, 1, 0.35f, ModDamageTypes.DORSAL_BEAM, 20, 40, 400) {
            @Override
            public void activate(KaijuEntity k, @Nullable LivingEntity target) {
                super.activate(k, target);
                ((ShinGodzillaEntity) k).spendEnergy(10);
            }
        }.damage(9f, 3).blocks(1.2f, true).chargeParticle(ParticleTypes.DRAGON_BREATH).range(4, 48)
                .when(k -> ((ShinGodzillaEntity) k).canAct() && k.getVariant() == FOURTH && !((ShinGodzillaEntity) k).tailBroken
                        && ((ShinGodzillaEntity) k).getEnergy() >= 10 && ((ShinGodzillaEntity) k).getEnergy() < 60));
        list.add(new TailSwipeAbility().when(k -> ((ShinGodzillaEntity) k).canAct() && k.getVariant() >= THIRD));
        list.add(new RoarAbility(ModSounds.SHIN_ROAR).when(k -> ((ShinGodzillaEntity) k).canAct() && k.getVariant() == FOURTH));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 1.6f, 1.6f, new Vec3(0, 9.6, 1.2), 1.3f),
                new KaijuPart(this, "back", 2.6f, 3.0f, new Vec3(0, 6.0, -1.4), 1.0f),
                new KaijuPart(this, "tail", 2.0f, 1.8f, new Vec3(0, 1.4, -6.0), 0.7f),
                new KaijuPart(this, "tail_tip", 1.4f, 1.4f, new Vec3(0, 1.6, -10.0), 0.8f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        if (getActiveAbility() != null && getActiveAbility().name.equals("tail_beam")) return new Vec3(0, 2.2, -10.6);
        return getVariant() == SECOND ? new Vec3(0, 2.4, 3.2) : new Vec3(0, 10.0, 1.8);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return DIMENSIONS[Mth.clamp(getVariant(), 0, 2)];
    }

    @Override
    protected double variantScale() {
        return switch (getVariant()) {
            case SECOND -> 1.3;
            case THIRD -> 1.15;
            default -> 2.15;
        };
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(switch (getVariant()) {
            case SECOND -> 300;
            case THIRD -> 650;
            default -> 1400;
        });
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == SECOND ? 12 : getVariant() == THIRD ? 20 : 30);
        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(getVariant() == SECOND ? 0.24 : getVariant() == THIRD ? 0.2 : 0.16);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ENERGY, 100f);
        builder.define(DATA_COAGULANT, 0f);
        builder.define(DATA_STATE, NORMAL);
    }

    public float getEnergy() {
        return entityData.get(DATA_ENERGY);
    }

    public float getCoagulant() {
        return entityData.get(DATA_COAGULANT);
    }

    public int getState() {
        return entityData.get(DATA_STATE);
    }

    private void setState(int state, int ticks) {
        entityData.set(DATA_STATE, state);
        stateTicks = ticks;
        if (state != NORMAL && isAbilityActive()) stopAbility();
    }

    public boolean isFrozenForever() {
        return getState() == FROZEN;
    }

    private boolean canAct() {
        return getState() == NORMAL;
    }

    private void spendEnergy(float amount) {
        entityData.set(DATA_ENERGY, Math.max(0f, getEnergy() - amount));
    }

    @Override
    public Component getTypeName() {
        if (isFrozenForever()) return Component.translatable("entity.reikogodzilla.shin_godzilla_frozen");
        return Component.translatable(switch (getVariant()) {
            case SECOND -> "entity.reikogodzilla.shin_godzilla_2";
            case THIRD -> "entity.reikogodzilla.shin_godzilla_3";
            default -> "entity.reikogodzilla.shin_godzilla";
        });
    }

    // ---------------------------------------------------------------- the breath's three stages

    private void breathCharge(@Nullable LivingEntity target, int tick) {
        if (!(level() instanceof ServerLevel server)) return;
        Vec3 mouth = getBeamOrigin(0, 1f);
        Vec3 aim = target != null ? target.getBoundingBox().getCenter() : mouth.add(Vec3.directionFromRotation(getXRot(), yBodyRot).scale(20));
        Vec3 dir = aim.subtract(mouth).normalize();
        double reach = Math.min(aim.distanceTo(mouth), 24 * Math.max(1, getScale() * 0.6));
        if (tick < 30) {
            // A black cloud of smoke and ash pours from the jaw.
            for (int i = 0; i < 6; i++) {
                Vec3 p = mouth.add(dir.scale(random.nextDouble() * reach));
                server.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, p.y, p.z, 2, 1.2, 1.2, 1.2, 0.02);
                server.sendParticles(ParticleTypes.SQUID_INK, p.x, p.y, p.z, 1, 0.8, 0.8, 0.8, 0.02);
            }
            if (tick % 10 == 0) coneVictims(mouth, dir, reach).forEach(e -> {
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                e.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
            });
        } else if (tick < 70) {
            // Then it ignites into a spray of fire.
            for (int i = 0; i < 8; i++) {
                Vec3 p = mouth.add(dir.scale(random.nextDouble() * reach * 1.2));
                server.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 3, 1.0, 1.0, 1.0, 0.05);
            }
            if (tick % 5 == 0) {
                coneVictims(mouth, dir, reach * 1.2).forEach(e -> {
                    e.igniteForSeconds(6);
                    KaijuDestruction.hurtIgnoringCooldown(e, ModDamageTypes.source(server, ModDamageTypes.SHIN_BREATH, this), e instanceof KaijuEntity ? 8f : 5f);
                });
                if (KaijuDestruction.canGrief(server, this)) {
                    BlockPos at = BlockPos.containing(mouth.add(dir.scale(reach * (0.5 + random.nextDouble() * 0.6))));
                    for (int i = 0; i < 4; i++) {
                        BlockPos f = at.offset(random.nextInt(5) - 2, random.nextInt(3) - 1, random.nextInt(5) - 2);
                        if (server.getBlockState(f).isAir() && net.minecraft.world.level.block.BaseFireBlock.canBePlacedAt(server, f, net.minecraft.core.Direction.UP)) {
                            server.setBlock(f, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    private List<LivingEntity> coneVictims(Vec3 from, Vec3 dir, double reach) {
        return level().getEntitiesOfClass(LivingEntity.class, new AABB(from, from.add(dir.scale(reach))).inflate(4), e -> {
            if (e == this || isAlliedTo(e) || e instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
            Vec3 to = e.getBoundingBox().getCenter().subtract(from);
            double along = to.dot(dir);
            return along > 0 && along < reach && to.subtract(dir.scale(along)).length() < 2.5 + along * 0.25;
        });
    }

    // ---------------------------------------------------------------- reactor, evolution, dorsal beams

    /** States that make it immobile (molting, dormant, knocked down) tick here: Minecraft skips the AI step while a mob is immobile. */
    @Override
    public void aiStep() {
        if (!level().isClientSide && isAlive() && level() instanceof ServerLevel server) tickState(server);
        super.aiStep();
    }

    private void tickState(ServerLevel server) {
        int state = getState();
        if (state == FROZEN) {
            setTarget(null);
            getNavigation().stop();
            setDeltaMovement(0, Math.min(0, getDeltaMovement().y), 0);
            return;
        }
        if (stateTicks > 0 && --stateTicks == 0) {
            switch (state) {
                case MOLTING -> finishMolt(server);
                case DORMANT, KNOCKED_DOWN -> {
                    setState(NORMAL, 0);
                    if (state == DORMANT) entityData.set(DATA_ENERGY, 60f);
                    triggerAnim("action", "roar");
                    playSound(ModSounds.SHIN_ROAR.get(), 10f, 0.9f);
                }
                default -> {}
            }
        }
        if (state == MOLTING && tickCount % 4 == 0) {
            server.sendParticles(ParticleTypes.CLOUD, getX(), getY(0.6), getZ(), 12, getBbWidth() * 0.6, getBbHeight() * 0.4, getBbWidth() * 0.6, 0.05);
            server.sendParticles(ParticleTypes.DRIPPING_LAVA, getX(), getY(0.5), getZ(), 6, getBbWidth() * 0.5, getBbHeight() * 0.3, getBbWidth() * 0.5, 0);
        }
        if (state != NORMAL) {
            if (state == DORMANT || state == KNOCKED_DOWN) setTarget(null);
            getNavigation().stop();
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        int state = getState();
        if (state != NORMAL) return;
        // Energy recovers while it isn't spending it.
        if (state == NORMAL && !isAbilityActive() && tickCount % 20 == 0) entityData.set(DATA_ENERGY, Math.min(100f, getEnergy() + 0.5f));
        if (state == NORMAL && getVariant() == FOURTH && getEnergy() <= 0.01f) {
            setState(DORMANT, GodzillaConfig.get(GodzillaConfig.SHIN_DORMANCY_TICKS, 6000));
            announce("message.reikogodzilla.shin_dormant");
        }
        if (tickCount % 20 == 0) {
            knockdown = Math.max(0f, knockdown - 0.01f);
            float coag = getCoagulant();
            if (coag > 0 && state == NORMAL) entityData.set(DATA_COAGULANT, Math.max(0f, coag - 0.1f));
            if (state == NORMAL && getVariant() < FOURTH) {
                evolution += (isInWater() ? 0f : 1f) * (float) GodzillaConfig.get(GodzillaConfig.SHIN_EVOLUTION_RATE, 1.0);
                if (evolution >= (getVariant() == SECOND ? 300 : 600)) startMolt();
            }
        }
        if (getVariant() == SECOND && tickCount % 6 == 0) {
            // The gills bleed as it drags itself along.
            server.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.6f, 0.02f, 0.02f), 1.6f),
                    getX(), getY(0.4), getZ(), 4, getBbWidth() * 0.3, 0.3, getBbWidth() * 0.3, 0);
        }
        if (state == NORMAL && getVariant() == FOURTH && tickCount % 5 == 0) fireDorsalBeams(server);
        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && getCoagulant() > 0) speed.setBaseValue((getVariant() == SECOND ? 0.24 : getVariant() == THIRD ? 0.2 : 0.16) * (1 - getCoagulant() / 150f));
    }

    private void startMolt() {
        setState(MOLTING, getVariant() == SECOND ? 160 : 240);
        playSound(ModSounds.SHIN_ROAR.get(), 8f, 1.3f);
        announce(getVariant() == SECOND ? "message.reikogodzilla.shin_evolving_3" : "message.reikogodzilla.shin_evolving_4");
    }

    private void finishMolt(ServerLevel server) {
        evolution = 0;
        setVariant(Math.min(FOURTH, getVariant() + 1));
        refreshDimensions();
        setHealth(getMaxHealth());
        setState(NORMAL, 0);
        entityData.set(DATA_ENERGY, 100f);
        server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(0.5), getZ(), 8, getBbWidth() * 0.5, getBbHeight() * 0.3, getBbWidth() * 0.5, 0);
        triggerAnim("action", "roar");
        playSound(ModSounds.SHIN_ROAR.get(), 12f, 0.8f);
    }

    /** Thin beams from the back plates that pick off anything in the air: aircraft, flyers, missiles, drones. */
    private void fireDorsalBeams(ServerLevel server) {
        if (getEnergy() < 1) return;
        int shots = Math.max(0, 2 - platesBroken / 3);
        if (shots == 0) return;
        double range = 64;
        List<Entity> targets = server.getEntities(this, getBoundingBox().inflate(range), e -> {
            if (e instanceof KaijuPart || !e.isAlive() || hasPassenger(e) || e.distanceToSqr(this) > range * range) return false;
            if (e instanceof com.reiko45.godzilla.entity.projectile.DroneEntity) return true;
            if (!InstantBeam.isAirborne(e)) return false;
            if (e instanceof net.minecraft.world.entity.projectile.Projectile p) return p.getOwner() != this && !(p.getOwner() instanceof KaijuPart);
            if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
            return e instanceof LivingEntity l && (wantsToAttack(l) || l == getLastHurtByMob() || l.getVehicle() == getLastHurtByMob()
                    || l instanceof KaijuAligned a && a.getFaction() == KaijuFaction.HUMAN);
        });
        targets.sort(java.util.Comparator.comparingDouble(e -> e.distanceToSqr(this)));
        var particle = InstantBeam.dust(0.7f, 0.3f, 1.0f, 1.5f);
        for (int i = 0; i < Math.min(shots, targets.size()); i++) {
            Entity t = targets.get(i);
            Vec3 from = position().add(new Vec3((random.nextDouble() - 0.5) * 1.2, 6.0 + random.nextDouble() * 3.0, -1.5 - random.nextDouble() * 1.5)
                    .scale(getScale()).yRot(-yBodyRot * Mth.DEG_TO_RAD));
            Vec3 to = t.getBoundingBox().getCenter();
            InstantBeam.fire(server, this, from, to, particle, ModDamageTypes.source(server, ModDamageTypes.DORSAL_BEAM, this), 8f);
            if (t instanceof net.minecraft.world.entity.projectile.Projectile || t instanceof com.reiko45.godzilla.entity.projectile.DroneEntity) {
                server.sendParticles(ParticleTypes.EXPLOSION, to.x, to.y, to.z, 1, 0, 0, 0, 0);
                t.discard();
            }
            spendEnergy(1f);
        }
        if (!targets.isEmpty()) server.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 3f, 1.8f);
    }

    // ---------------------------------------------------------------- Operation Yashiori

    /** Train bombs and collapsing buildings knock it down: a fraction of the way per hit. */
    public void addKnockdown(float amount) {
        if (getState() != NORMAL && getState() != DORMANT) return;
        knockdown += amount;
        if (knockdown >= 1f) {
            knockdown = 0;
            setState(KNOCKED_DOWN, 300);
            playSound(SoundEvents.GENERIC_EXPLODE.value(), 8f, 0.4f);
            announce("message.reikogodzilla.shin_knocked_down");
        }
    }

    /** Coagulant pumped into the mouth while it lies helpless. Returns false if it can't be pumped right now. */
    public boolean pumpCoagulant(float amount, @Nullable Entity source) {
        int state = getState();
        if (state != DORMANT && state != KNOCKED_DOWN) return false;
        float coag = Math.min(100f, getCoagulant() + amount);
        entityData.set(DATA_COAGULANT, coag);
        if (level() instanceof ServerLevel server) {
            Vec3 mouth = getBeamOrigin(0, 1f);
            server.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.85f, 0.85f, 0.95f), 2f),
                    mouth.x, mouth.y, mouth.z, 20, 0.6, 0.6, 0.6, 0);
            if (source instanceof ServerPlayer p) {
                p.displayClientMessage(Component.translatable("message.reikogodzilla.coagulant", (int) coag), true);
            }
        }
        if (coag >= 100f) freeze();
        return true;
    }

    private void freeze() {
        setState(FROZEN, 0);
        setTarget(null);
        playSound(SoundEvents.GLASS_BREAK, 6f, 0.4f);
        announce("message.reikogodzilla.shin_frozen");
        setPersistenceRequired();
    }

    private void announce(String key) {
        if (!(level() instanceof ServerLevel server)) return;
        for (ServerPlayer p : server.players()) {
            if (p.distanceToSqr(this) < 192 * 192) p.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        }
    }

    @Override
    public void onSpecialPayload(com.reiko45.godzilla.entity.projectile.KaijuMissileEntity.Payload payload, @Nullable String part, @Nullable Entity owner) {
        switch (payload) {
            case MOP -> {
                lastMop = tickCount;
                if ("back".equals(part) && platesBroken < 6) {
                    platesBroken++;
                    if (level() instanceof ServerLevel server) {
                        // Massive blood loss from the wound: a radioactive spray.
                        server.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.5f, 0.0f, 0.05f), 2.5f),
                                getX(), getY(0.7), getZ(), 80, getBbWidth() * 0.4, getBbHeight() * 0.2, getBbWidth() * 0.4, 0.1);
                        com.reiko45.godzilla.world.Fallout.blackRain(server, position(), 10f, 1200, this);
                    }
                    announce("message.reikogodzilla.shin_plate_broken");
                }
            }
            case COAGULANT -> {
                if ("head".equals(part)) pumpCoagulant(2f, owner);
            }
            default -> {}
        }
    }

    @Override
    public boolean hurtFromPart(KaijuPart part, DamageSource source, float amount) {
        if (part.name.equals("tail_tip") && !tailBroken && getVariant() == FOURTH) {
            tailDamage += amount * part.damageMultiplier;
            if (tailDamage > getMaxHealth() * 0.08f) {
                tailBroken = true;
                announce("message.reikogodzilla.shin_tail_broken");
            }
        }
        return super.hurtFromPart(part, source, amount);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isFrozenForever() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        if (level().isClientSide) return super.hurt(source, amount);
        int state = getState();
        if (state == MOLTING) amount *= 0.5f;
        if (state == DORMANT) amount *= 0.1f;
        if (source.is(DamageTypeTags.IS_EXPLOSION)) addKnockdown(amount / 220f);
        // Conventional weapons barely scratch the 4th form, until bunker busters open it up.
        boolean conventional = !(source.getEntity() instanceof KaijuEntity) && !source.is(ModDamageTypes.KAIJU_BEAM) && tickCount - lastMop > 2;
        if (getVariant() == FOURTH && conventional) amount *= 0.35f;
        String type = source.getMsgId();
        if (adapted.contains(type)) amount *= 0.75f;
        boolean hurt = super.hurt(source, amount);
        if (hurt && getVariant() == FOURTH) {
            float total = damageTaken.merge(type, amount, Float::sum);
            if (!adapted.contains(type) && total > fractionOfDesignHealth(0.3f)) {
                adapted.add(type);
                announce("message.reikogodzilla.shin_adapts");
            }
        }
        if (hurt && state == NORMAL && getVariant() < FOURTH) evolution += amount * 0.25f;
        return hurt;
    }

    @Override
    public boolean isImmobile() {
        return getState() != NORMAL || super.isImmobile();
    }

    @Override
    public boolean canWander() {
        return getState() == NORMAL;
    }

    @Override
    public boolean canCrushBlocks() {
        return getState() == NORMAL;
    }

    @Override
    public boolean showsBossBar() {
        return !isFrozenForever() && super.showsBossBar();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Nullable
    @Override
    public float[] getTint() {
        if (isFrozenForever()) return new float[]{0.62f, 0.62f, 0.66f};
        float c = getCoagulant() / 100f;
        if (c > 0.01f) return new float[]{1f - 0.3f * c, 1f - 0.3f * c, 1f - 0.2f * c};
        if (getVariant() == THIRD) return new float[]{1.0f, 0.85f, 0.85f};
        return super.getTint();
    }

    @Override
    protected PlayState mainAnimation(AnimationState<KaijuEntity> state) {
        int s = getState();
        if (s == FROZEN) return PlayState.STOP;
        if (s == DORMANT || s == KNOCKED_DOWN) return state.setAndContinue(DORMANT_ANIM);
        if (s == MOLTING) return state.setAndContinue(MOLT_ANIM);
        return super.mainAnimation(state);
    }

    @Override
    protected float regenPerSecond() {
        return getState() == NORMAL ? 1.0f : 0f;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Energy", getEnergy());
        tag.putFloat("Coagulant", getCoagulant());
        tag.putInt("State", getState());
        tag.putInt("StateTicks", stateTicks);
        tag.putFloat("Evolution", evolution);
        tag.putFloat("Knockdown", knockdown);
        tag.putInt("PlatesBroken", platesBroken);
        tag.putBoolean("TailBroken", tailBroken);
        CompoundTag adapt = new CompoundTag();
        for (String s : adapted) adapt.putBoolean(s, true);
        tag.put("Adapted", adapt);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Energy")) entityData.set(DATA_ENERGY, tag.getFloat("Energy"));
        entityData.set(DATA_COAGULANT, tag.getFloat("Coagulant"));
        entityData.set(DATA_STATE, tag.getInt("State"));
        stateTicks = tag.getInt("StateTicks");
        evolution = tag.getFloat("Evolution");
        knockdown = tag.getFloat("Knockdown");
        platesBroken = tag.getInt("PlatesBroken");
        tailBroken = tag.getBoolean("TailBroken");
        adapted.clear();
        adapted.addAll(tag.getCompound("Adapted").getAllKeys());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return getState() == NORMAL && getVariant() == FOURTH ? ModSounds.SHIN_ROAR.get() : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHIN_ROAR.get();
    }
}
