package com.reiko45.godzilla.entity.showa;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Titanosaurus (Terror of Mechagodzilla, 1975): a gentle amphibious dinosaur from the waters near Ogasawara, turned
 * into a weapon by Dr. Mafune's control implant and the Black Hole Planet 3 aliens. His tail fan whips up gale-force
 * winds strong enough to blow Godzilla away.
 * <p>
 * The implant in his head is his weak point: it makes him helpless against supersonic waves (the Supersonic Wave
 * Oscillator stuns him), and smashing it (hits to the head) sets him free for good.
 */
public class TitanosaurusEntity extends KaijuEntity {
    private static final EntityDataAccessor<Boolean> DATA_IMPLANT = SynchedEntityData.defineId(TitanosaurusEntity.class, EntityDataSerializers.BOOLEAN);
    private float implantDamage;

    public TitanosaurusEntity(EntityType<? extends TitanosaurusEntity> type, Level level) {
        super(type, level);
        this.xpReward = 450;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(800, 18, 8, 28, 0.24);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.TITAN;
    }

    @Override
    public boolean controllableBy(AlienSignal signal) {
        return signal == AlienSignal.BLACK_HOLE && hasImplant();
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new WindGustAbility(3.5f));
        list.add(new TailSwipeAbility());
        list.add(new GrabThrowAbility());
        list.add(new RoarAbility(ModSounds.TITANOSAURUS_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 1.8f, 2.0f, new Vec3(0, 8.6, 2.0), 1.6f),
                new KaijuPart(this, "tail", 2.4f, 2.4f, new Vec3(0, 1.6, -4.4), 0.8f),
                new KaijuPart(this, "tail_fan", 2.6f, 2.6f, new Vec3(0, 1.0, -7.6), 0.6f)
        };
    }

    @Override
    protected double variantScale() {
        return 1.25;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IMPLANT, false);
    }

    public boolean hasImplant() {
        return entityData.get(DATA_IMPLANT);
    }

    /** Events and the Black Hole aliens' base bring a Titanosaurus with Mafune's implant. */
    @Override
    public void finalizeSpawnVariant(MobSpawnType type) {
        if (type == MobSpawnType.EVENT || type == MobSpawnType.STRUCTURE) entityData.set(DATA_IMPLANT, true);
    }

    @Override
    public boolean hurtFromPart(KaijuPart part, DamageSource source, float amount) {
        if (part.name.equals("head") && hasImplant()) {
            implantDamage += amount * part.damageMultiplier;
            if (implantDamage > getMaxHealth() * 0.15f) destroyImplant();
        }
        return super.hurtFromPart(part, source, amount);
    }

    private void destroyImplant() {
        entityData.set(DATA_IMPLANT, false);
        releaseControl(100);
        playSound(net.minecraft.sounds.SoundEvents.GLASS_BREAK, 5f, 0.5f);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(0.85), getZ(), 60, 1, 1, 1, 0.3);
            for (var p : server.players()) {
                if (p.distanceToSqr(this) < 160 * 160) p.displayClientMessage(Component.translatable("message.reikogodzilla.titanosaurus_free"), true);
            }
        }
    }

    /** The Supersonic Wave Oscillator: the implant shrieks in his skull and he reels helplessly. */
    public void onSupersonicWave(Player source) {
        stun(hasImplant() ? 140 : 40);
        hurt(damageSources().playerAttack(source), hasImplant() ? 10f : 2f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Implant", hasImplant());
        tag.putFloat("ImplantDamage", implantDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_IMPLANT, tag.getBoolean("Implant"));
        implantDamage = tag.getFloat("ImplantDamage");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.TITANOSAURUS_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TITANOSAURUS_ROAR.get();
    }
}
