package com.reiko45.godzilla.entity.millennium;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ShrineGuardian;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * King Ghidorah, God of the Sky (GMK, 2001): smaller than Godzilla, an "undeveloped" Yamata no Orochi frozen under
 * the forest near Mount Fuji. At first he can't fly and fights with Thunder Spark bites (one of them tears open
 * GMK Godzilla's shoulder). When Mothra dies and gives him her energy he becomes the Thousand-Year-Old Dragon King:
 * he flies, fires golden gravity beams and raises a barrier.
 */
public class GmkGhidorahEntity extends FlyingKaijuEntity implements ShrineGuardian {
    public static final int GHIDORAH = 0;
    public static final int DRAGON_KING = 1;
    private final Vengeance vengeance = new Vengeance();

    public GmkGhidorahEntity(EntityType<? extends GmkGhidorahEntity> type, Level level) {
        super(type, level);
        this.xpReward = 450;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(650, 18, 8, 22, 0.24).add(Attributes.FLYING_SPEED, 0.6);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.GUARDIAN;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new HoldAbility("thunder_spark", "grab", HoldAbility.Mode.FRONT, 36, 6, 0.45f, 0f, 200)
                .damageType(ModDamageTypes.THUNDER_SPARK).sound(ModSounds.GMK_GHIDORAH_ROAR)
                .onHit((k, e) -> {
                    if (k.level() instanceof ServerLevel s) s.sendParticles(ParticleTypes.ELECTRIC_SPARK, e.getX(), e.getY(0.7), e.getZ(), 20, 0.6, 0.6, 0.6, 0.3);
                    if (e instanceof MillenniumGodzillaEntity g) g.openWound();
                }));
        list.add(new BeamAbility("gravity_beams", BeamStyle.GRAVITY, 3, 0.4f, ModDamageTypes.GRAVITY_BEAM, 25, 50, 200)
                .damage(8f, 4).blocks(1.5f, false).lift(0.25f).multiTarget()
                .sounds(ModSounds.GRAVITY_BEAM_CHARGE, ModSounds.GRAVITY_BEAM_FIRE).range(4, 52)
                .when(k -> k.getVariant() == DRAGON_KING));
        list.add(new ForceFieldAbility().when(k -> k.getVariant() == DRAGON_KING));
        list.add(new WindGustAbility(2.6f).when(k -> k.getVariant() == DRAGON_KING));
        list.add(new RoarAbility(ModSounds.GMK_GHIDORAH_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{new KaijuPart(this, "heads", 4.2f, 2.4f, new Vec3(0, 11.8, 3.0), 1.3f)};
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        double[][] heads = {{2.2, 12.2, 4.2}, {0.0, 13.0, 4.8}, {-2.2, 12.2, 4.2}};
        double[] h = heads[Math.min(index, 2)];
        return new Vec3(h[0], h[1], h[2]);
    }

    @Override
    protected double variantScale() {
        return 0.75;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == DRAGON_KING ? 950 : 650);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == DRAGON_KING ? 28 : 22);
    }

    @Override
    public boolean canFlyNow() {
        return getVariant() == DRAGON_KING;
    }

    @Override
    public boolean prefersFlightInCombat() {
        return canFlyNow();
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(getVariant() == DRAGON_KING ? "entity.reikogodzilla.king_ghidorah_dragon_king" : "entity.reikogodzilla.king_ghidorah_gmk");
    }

    /** Mothra's life energy: the Thousand-Year-Old Dragon King awakens. */
    public void receiveMothraEnergy() {
        if (!isAlive() || isDeadOrDying()) return;
        if (getVariant() == DRAGON_KING) {
            heal(getMaxHealth() * 0.5f);
            return;
        }
        setVariant(DRAGON_KING);
        setHealth(getMaxHealth());
        triggerAnim("action", "roar");
        playSound(ModSounds.GMK_GHIDORAH_ROAR.get(), 10f, 0.8f);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.6), getZ(), 120, getBbWidth(), getBbHeight() * 0.5, getBbWidth(), 0.2);
            for (var p : server.players()) {
                if (p.distanceToSqr(this) < 256 * 256) p.displayClientMessage(Component.translatable("message.reikogodzilla.dragon_king"), false);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (ForceFieldAbility.isShielding(this) && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            playSound(SoundEvents.SHIELD_BLOCK, 3f, 0.6f);
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean wantsToAttack(LivingEntity other) {
        return vengeance.isTarget(other) || super.wantsToAttack(other);
    }

    @Override
    public void awaken(@Nullable Player vandal) {
        if (vandal != null) vengeance.set(vandal, 2400);
        triggerAnim("action", "roar");
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        vengeance.tick(this);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel server) GmkMothraEntity.passOn(this, server);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        vengeance.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        vengeance.load(tag);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.GMK_GHIDORAH_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GMK_GHIDORAH_ROAR.get();
    }
}
