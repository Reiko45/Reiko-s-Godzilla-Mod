package com.reiko45.godzilla.entity.millennium;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.ShrineGuardian;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * Mothra, God of the Sea (GMK, 2001), one of the three Guardian Monsters. Sleeps under the lake at her shrine; fires
 * stinger darts from her abdomen, scatters poison scales and beats up gales. When she falls, her life energy passes
 * to King Ghidorah, waking him as the Thousand-Year-Old Dragon King.
 */
public class GmkMothraEntity extends FlyingKaijuEntity implements ShrineGuardian {
    private final Vengeance vengeance = new Vengeance();

    public GmkMothraEntity(EntityType<? extends GmkMothraEntity> type, Level level) {
        super(type, level);
        this.xpReward = 400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(500, 10, 4, 14, 0.22).add(Attributes.FLYING_SPEED, 0.7);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.GUARDIAN;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new MissileBarrageAbility("stinger_darts", new Vec3(-0.3, 0.8, -2.4), new Vec3(0.3, 0.8, -2.4),
                KaijuMissileEntity.Payload.PARALYZER, 180).interval(4));
        list.add(new PoisonScalesAbility());
        list.add(new WindGustAbility(2.4f));
    }

    @Override
    public boolean prefersFlightInCombat() {
        return true;
    }

    @Override
    public boolean wantsToAttack(LivingEntity other) {
        return vengeance.isTarget(other) || super.wantsToAttack(other);
    }

    @Override
    public void awaken(@Nullable Player vandal) {
        if (vandal != null) vengeance.set(vandal, 2400);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        vengeance.tick(this);
    }

    /** Her spirit passes to King Ghidorah, and on to the other Guardians. */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!(level() instanceof ServerLevel server)) return;
        GmkGhidorahEntity ghidorah = server.getEntitiesOfClass(GmkGhidorahEntity.class, getBoundingBox().inflate(160)).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if (ghidorah != null) ghidorah.receiveMothraEnergy();
        passOn(this, server);
    }

    /** A fallen Guardian's spirit strengthens the others still fighting. */
    public static void passOn(KaijuEntity fallen, ServerLevel server) {
        for (KaijuEntity k : server.getEntitiesOfClass(KaijuEntity.class, fallen.getBoundingBox().inflate(160),
                k -> k != fallen && k.isAlive() && k instanceof ShrineGuardian)) {
            k.heal(k.getMaxHealth() * 0.3f);
            k.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1));
            server.sendParticles(ParticleTypes.END_ROD, k.getX(), k.getY(0.6), k.getZ(), 60, k.getBbWidth() * 0.5, k.getBbHeight() * 0.4, k.getBbWidth() * 0.5, 0.1);
        }
        for (var p : server.players()) {
            if (p.distanceToSqr(fallen) < 200 * 200) p.displayClientMessage(Component.translatable("message.reikogodzilla.guardian_spirit", fallen.getDisplayName()), true);
        }
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
        return ModSounds.MOTHRA_CRY.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MOTHRA_CRY.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MOTHRA_CRY.get();
    }
}
