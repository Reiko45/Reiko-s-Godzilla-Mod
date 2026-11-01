package com.reiko45.godzilla.entity.showa;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.List;

/**
 * King Caesar (Godzilla vs. Mechagodzilla, 1974), the lion-dog guardian of the Azumi royal family of Okinawa.
 * <p>
 * He sleeps inside the cliff at his shrine until the sacred Shisa Statue is returned and the song is sung (place the
 * statue nearby and play any music disc in a jukebox next to it). His Prism Eye catches an energy beam in one eye and
 * fires it back, twice as strong, from the other: he reflects beams for a few seconds, then needs time to recover.
 * Fast, nimble kicks and leaps. In Final Wars he's twice the size and a Xilien puppet (M-Base).
 */
public class KingCaesarEntity extends KaijuEntity {
    public static final int SHOWA = 0;
    public static final int FINAL_WARS = 1;
    private static final RawAnimation DORMANT = RawAnimation.begin().thenLoop("dormant");
    private static final EntityDataAccessor<Boolean> DATA_DORMANT = SynchedEntityData.defineId(KingCaesarEntity.class, EntityDataSerializers.BOOLEAN);
    private int prismWindow;
    private int prismCooldown;

    public KingCaesarEntity(EntityType<? extends KingCaesarEntity> type, Level level) {
        super(type, level);
        this.xpReward = 450;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(750, 18, 8, 26, 0.3);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.GUARDIAN;
    }

    @Override
    public boolean hasMBase() {
        return getVariant() == FINAL_WARS;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new LeapAbility("flying_kick", 1.6, 1.6f, 180));
        list.add(new GrabThrowAbility());
        list.add(new RoarAbility(ModSounds.KING_CAESAR_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{new KaijuPart(this, "head", 2.0f, 2.0f, new Vec3(0, 8.4, 1.6), 1.3f)};
    }

    @Override
    protected double variantScale() {
        return getVariant() == FINAL_WARS ? 2.1 : 1.05;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == FINAL_WARS ? 1200 : 750);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == FINAL_WARS ? 34 : 26);
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(getVariant() == FINAL_WARS ? "entity.reikogodzilla.king_caesar_final_wars" : "entity.reikogodzilla.king_caesar");
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_DORMANT, false);
    }

    public boolean isDormant() {
        return entityData.get(DATA_DORMANT);
    }

    @Override
    public void finalizeSpawnVariant(MobSpawnType type) {
        if (type == MobSpawnType.STRUCTURE && getVariant() == SHOWA) entityData.set(DATA_DORMANT, true);
    }

    /** The statue is back and the song is playing: he bursts out of the cliff. */
    @Override
    public void wakeUp() {
        if (!isDormant()) return;
        entityData.set(DATA_DORMANT, false);
        setHealth(getMaxHealth());
        triggerAnim("action", "roar");
        playSound(ModSounds.KING_CAESAR_ROAR.get(), 10f, 0.9f);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(0.5), getZ(), 2, getBbWidth() * 0.4, getBbHeight() * 0.3, getBbWidth() * 0.4, 0);
            for (var p : server.players()) {
                if (p.distanceToSqr(this) < 200 * 200) p.displayClientMessage(Component.translatable("message.reikogodzilla.king_caesar_awakens"), false);
            }
        }
    }

    @Override
    public boolean isImmobile() {
        return isDormant() || super.isImmobile();
    }

    @Override
    public boolean wantsToAttack(LivingEntity other) {
        return !isDormant() && super.wantsToAttack(other);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return (isDormant() && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean showsBossBar() {
        return !isDormant() && super.showsBossBar();
    }

    // ---------------------------------------------------------------- the Prism Eye

    @Override
    public boolean reflectsBeams() {
        if (isDormant() || isFrozenSolid() || isStunned()) return false;
        if (prismWindow > 0) return true;
        if (prismCooldown > 0) return false;
        prismWindow = 60;
        playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 5f, 0.6f);
        return true;
    }

    @Override
    protected float onBeamReflected(DamageSource source, float amount) {
        return 2.0f;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (prismWindow > 0) {
            if (--prismWindow <= 0) prismCooldown = 240;
            if (level() instanceof ServerLevel server && tickCount % 3 == 0) {
                Vec3 eye = getEyePosition().add(Vec3.directionFromRotation(0, yBodyRot).scale(getBbWidth() * 0.5));
                server.sendParticles(ParticleTypes.END_ROD, eye.x, eye.y, eye.z, 4, 0.2, 0.2, 0.2, 0.05);
            }
        } else if (prismCooldown > 0) {
            prismCooldown--;
        }
    }

    @Override
    public void performMelee(LivingEntity target) {
        super.performMelee(target);
        meleeCooldown = 18;
    }

    @Override
    protected PlayState mainAnimation(AnimationState<KaijuEntity> state) {
        if (isDormant()) return state.setAndContinue(DORMANT);
        return super.mainAnimation(state);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Dormant", isDormant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_DORMANT, tag.getBoolean("Dormant"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isDormant() ? null : ModSounds.KING_CAESAR_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.KING_CAESAR_ROAR.get();
    }
}
