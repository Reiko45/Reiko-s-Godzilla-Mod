package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.TameableKaijuEntity;
import com.reiko45.godzilla.entity.ability.GrabThrowAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.LeapAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Kong, last of his kind on Skull Island and later King of the Hollow Earth.
 * <ul>
 *     <li>Young (Kong: Skull Island, 1973) or adult (Godzilla vs. Kong onward). A young Kong grows up in time.</li>
 *     <li>Climbs anything he can't smash through, leaps on his enemies, grabs them and throws them.</li>
 *     <li>Give him his <b>Battle Axe</b>: it soaks up atomic breath and beams aimed at him, then lets the energy out
 *     in one explosive blow (twice as strong against Mechagodzilla).</li>
 *     <li>Shimo's ice gives him frostbite: weaker blows and the axe frozen useless. The <b>B.E.A.S.T. Glove</b> heals
 *     the arm, protects against cold and puts lightning behind his punches.</li>
 *     <li>Feed him fruit to win his trust; a friendly Kong follows and defends you.</li>
 * </ul>
 */
public class KongEntity extends TameableKaijuEntity {
    public static final int YOUNG = 0;
    public static final int ADULT = 1;
    private static final float AXE_FULL = 100f;

    private static final EntityDataAccessor<Boolean> DATA_AXE = SynchedEntityData.defineId(KongEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_AXE_CHARGE = SynchedEntityData.defineId(KongEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_GLOVE = SynchedEntityData.defineId(KongEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_FROSTBITE = SynchedEntityData.defineId(KongEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CLIMBING = SynchedEntityData.defineId(KongEntity.class, EntityDataSerializers.BOOLEAN);

    private int frostbiteTicks;
    private int growth;

    public KongEntity(EntityType<? extends KongEntity> type, Level level) {
        super(type, level);
        this.xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(900, 16, 8, 30, 0.32);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.TITAN;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new RoarAbility(ModSounds.KONG_ROAR));
        list.add(new LeapAbility("leap", 1.5, 1.6f, 160));
        list.add(new GrabThrowAbility());
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{new KaijuPart(this, "head", 1.8f, 1.8f, new Vec3(0, 8.4, 1.0), 1.4f)};
    }

    @Override
    protected double variantScale() {
        return getVariant() == YOUNG ? 0.65 : 2.05;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == YOUNG ? 300 : 950);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == YOUNG ? 14 : 32);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData data) {
        if (type != MobSpawnType.STRUCTURE) setVariant(ADULT);
        return super.finalizeSpawn(level, difficulty, type, data);
    }

    @Override
    protected float regenPerSecond() {
        return 1.5f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_AXE, false);
        builder.define(DATA_AXE_CHARGE, 0f);
        builder.define(DATA_GLOVE, false);
        builder.define(DATA_FROSTBITE, false);
        builder.define(DATA_CLIMBING, false);
    }

    public boolean hasAxe() {
        return entityData.get(DATA_AXE);
    }

    public float getAxeCharge() {
        return entityData.get(DATA_AXE_CHARGE);
    }

    public boolean hasGlove() {
        return entityData.get(DATA_GLOVE);
    }

    public boolean isFrostbitten() {
        return entityData.get(DATA_FROSTBITE);
    }

    // ------------------------------------------------------------------ climbing

    /** Kong scales buildings and cliffs instead of always flattening them. */
    @Override
    public boolean onClimbable() {
        return entityData.get(DATA_CLIMBING);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) entityData.set(DATA_CLIMBING, horizontalCollision && !isSitting());
    }

    @Override
    public boolean canCrushBlocks() {
        return super.canCrushBlocks() && getVariant() == ADULT;
    }

    // ------------------------------------------------------------------ axe, glove, frostbite

    /** The axe drinks in beams fired at him from the front and stores the energy. */
    @Override
    protected float absorbBeam(DamageSource source, float amount) {
        if (!hasAxe() || isFrostbitten() || source.getEntity() == null) return amount;
        Vec3 to = source.getEntity().position().subtract(position()).multiply(1, 0, 1);
        if (to.lengthSqr() > 1.0E-4 && Vec3.directionFromRotation(0, yBodyRot).dot(to.normalize()) < 0.2) return amount;
        float before = getAxeCharge();
        float after = Math.min(AXE_FULL, before + amount * 2.5f);
        entityData.set(DATA_AXE_CHARGE, after);
        if (level() instanceof ServerLevel server) {
            Vec3 hand = handPosition();
            server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, hand.x, hand.y, hand.z, 8, 0.5, 0.5, 0.5, 0.05);
        }
        if (before < AXE_FULL && after >= AXE_FULL && getOwner() instanceof Player owner) {
            owner.displayClientMessage(Component.translatable("message.reikogodzilla.kong_axe_charged").withStyle(ChatFormatting.AQUA), true);
        }
        return amount * 0.25f;
    }

    private Vec3 handPosition() {
        return position().add(new Vec3(-1.9, 5.0, 1.2).scale(getScale()).yRot(-yBodyRot * net.minecraft.util.Mth.DEG_TO_RAD));
    }

    @Override
    public float freezeMultiplier() {
        return hasGlove() ? 0.35f : 1.0f;
    }

    @Override
    public void addFreeze(float amount) {
        super.addFreeze(amount);
        if (!hasGlove() && (isFrozenSolid() || getFreeze() > 60f)) frostbite();
    }

    /** Godzilla x Kong: The New Empire - Shimo's breath froze his arm and his axe together. */
    public void frostbite() {
        if (level().isClientSide || hasGlove()) return;
        if (!isFrostbitten() && getOwner() instanceof Player owner) {
            owner.displayClientMessage(Component.translatable("message.reikogodzilla.kong_frostbite").withStyle(ChatFormatting.AQUA), true);
        }
        frostbiteTicks = 3600;
        entityData.set(DATA_FROSTBITE, true);
    }

    public void equipAxe() {
        entityData.set(DATA_AXE, true);
    }

    public void equipGlove() {
        entityData.set(DATA_GLOVE, true);
        frostbiteTicks = 0;
        entityData.set(DATA_FROSTBITE, false);
        heal(getMaxHealth() * 0.2f);
    }

    @Override
    public void performMelee(LivingEntity target) {
        super.performMelee(target);
        if (!(level() instanceof ServerLevel server)) return;
        float base = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (isFrostbitten()) return;
        if (hasAxe()) {
            if (getAxeCharge() >= AXE_FULL) {
                chargedStrike(server, target);
            } else {
                // A heavy cleave catches everything in front of him.
                Vec3 front = position().add(Vec3.directionFromRotation(0, yBodyRot).scale(getBbWidth()));
                for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(front, front).inflate(3 * getScale()),
                        e -> e != this && e != target && wantsToAttack(e))) {
                    e.hurt(damageSources().mobAttack(this), base * 0.6f);
                }
                target.hurt(damageSources().mobAttack(this), base * 0.6f);
                server.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 3f, 0.5f);
            }
        }
        if (hasGlove()) {
            // B.E.A.S.T. Glove: a jolt behind every punch.
            target.hurt(damageSources().lightningBolt(), base * 0.4f);
            Vec3 c = target.getBoundingBox().getCenter();
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y, c.z, 30, target.getBbWidth() * 0.5, target.getBbHeight() * 0.4, target.getBbWidth() * 0.5, 0.4);
            server.playSound(null, c.x, c.y, c.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 2f, 1.4f);
            Vec3 away = target.position().subtract(position()).multiply(1, 0, 1).normalize();
            target.push(away.x * 1.2, 0.4, away.z * 1.2);
        }
    }

    /** Godzilla vs. Kong: the axe lets out the stored atomic energy in one explosive blow. */
    private void chargedStrike(ServerLevel server, LivingEntity target) {
        entityData.set(DATA_AXE_CHARGE, 0f);
        float damage = 70f * (getVariant() == ADULT ? 1f : 0.4f);
        if (target instanceof ApexMechagodzillaEntity) damage *= 2f;
        DamageSource atomic = ModDamageTypes.source(server, ModDamageTypes.ATOMIC_BREATH, this);
        Vec3 c = target.getBoundingBox().getCenter();
        KaijuDestruction.hurtIgnoringCooldown(target, atomic, damage);
        for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4 * getScale()), e -> e != this && e != target && wantsToAttack(e))) {
            KaijuDestruction.hurtIgnoringCooldown(e, atomic, damage * 0.4f);
        }
        server.sendParticles(ParticleTypes.SONIC_BOOM, c.x, c.y, c.z, 3, 1, 1, 1, 0);
        server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, c.x, c.y, c.z, 80, 2, 2, 2, 0.3);
        server.playSound(null, c.x, c.y, c.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 6f, 0.6f);
        // The blast throws Kong back too.
        Vec3 back = position().subtract(target.position()).multiply(1, 0, 1).normalize();
        push(back.x * 1.5, 0.5, back.z * 1.5);
    }

    // ------------------------------------------------------------------ friendship, growth

    private static boolean isFruit(ItemStack stack) {
        return stack.is(Items.MELON_SLICE) || stack.is(Items.APPLE) || stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES)
                || stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE) || stack.is(Items.MELON);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean friendly = isTame() && isOwnedBy(player) || !isTame() && getTarget() != player;
        if (stack.is(ModItems.KONG_AXE.get()) && !hasAxe() && friendly) {
            if (!level().isClientSide) {
                equipAxe();
                stack.consume(1, player);
                playSound(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 2f, 0.6f);
                player.displayClientMessage(Component.translatable("message.reikogodzilla.kong_axe"), true);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (stack.is(ModItems.BEAST_GLOVE.get()) && !hasGlove() && friendly) {
            if (!level().isClientSide) {
                equipGlove();
                stack.consume(1, player);
                playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 2f, 0.6f);
                player.displayClientMessage(Component.translatable("message.reikogodzilla.kong_glove"), true);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (isFruit(stack)) {
            if (!level().isClientSide) {
                stack.consume(1, player);
                heal(getMaxHealth() * 0.08f);
                growth += 2400;
                if (!isTame()) {
                    boolean trusted = stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE) || random.nextInt(3) == 0;
                    if (trusted) {
                        setOwner(player);
                        setTarget(null);
                        player.displayClientMessage(Component.translatable("message.reikogodzilla.kong_trust"), true);
                    }
                    showTamingResult(trusted);
                } else {
                    showTamingResult(true);
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (isTame() && isOwnedBy(player) && player.isSecondaryUseActive()) {
            toggleSitting(player);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (frostbiteTicks > 0) {
            frostbiteTicks--;
            if (frostbiteTicks <= 0 || isOnFire() || isInLava()) {
                frostbiteTicks = 0;
                entityData.set(DATA_FROSTBITE, false);
            } else if (tickCount % 10 == 0 && level() instanceof ServerLevel server) {
                Vec3 hand = handPosition();
                server.sendParticles(ParticleTypes.SNOWFLAKE, hand.x, hand.y, hand.z, 4, 0.4, 0.4, 0.4, 0.01);
            }
        }
        if (hasAxe() && getAxeCharge() > 0 && tickCount % 4 == 0 && level() instanceof ServerLevel server) {
            Vec3 hand = handPosition();
            server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, hand.x, hand.y, hand.z, 1 + (int) (getAxeCharge() / 34), 0.3, 0.6, 0.3, 0.01);
        }
        if (getVariant() == YOUNG && ++growth >= GodzillaConfig.get(GodzillaConfig.KONG_GROWTH_TICKS, 48000)) {
            setVariant(ADULT);
            setHealth(getMaxHealth());
            triggerAnim("action", "roar");
            playSound(ModSounds.KONG_ROAR.get(), 8f, 0.8f);
            if (level() instanceof ServerLevel server) {
                for (var p : server.players()) {
                    if (p.distanceToSqr(this) < 160 * 160) p.sendSystemMessage(Component.translatable("message.reikogodzilla.kong_grown").withStyle(ChatFormatting.GOLD));
                }
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (hasAxe()) spawnAtLocation(new ItemStack(ModItems.KONG_AXE.get()));
        if (hasGlove()) spawnAtLocation(new ItemStack(ModItems.BEAST_GLOVE.get()));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Axe", hasAxe());
        tag.putFloat("AxeCharge", getAxeCharge());
        tag.putBoolean("Glove", hasGlove());
        tag.putInt("Frostbite", frostbiteTicks);
        tag.putInt("Growth", growth);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_AXE, tag.getBoolean("Axe"));
        entityData.set(DATA_AXE_CHARGE, tag.getFloat("AxeCharge"));
        entityData.set(DATA_GLOVE, tag.getBoolean("Glove"));
        frostbiteTicks = tag.getInt("Frostbite");
        entityData.set(DATA_FROSTBITE, frostbiteTicks > 0);
        growth = tag.getInt("Growth");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.KONG_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return getVariant() == YOUNG ? 0.9f : 0.55f;
    }
}
