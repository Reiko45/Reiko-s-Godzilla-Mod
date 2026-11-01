package com.reiko45.godzilla.entity.showa;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.entity.projectile.SlicerDiscEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Gigan, the cyborg space monster of the Nebula M Space Hunters.
 * <ul>
 *     <li>Showa (Godzilla vs. Gigan, 1972): hammer hands, the belly buzzsaw, a laser from the visor and Mach 3 flight.
 *     He only follows orders while the Nebulans' signal from the Godzilla Tower keeps coming.</li>
 *     <li>Final Wars (2004): twice the size. Bloody Trigger scythes on grappling cables reel prey into the buzzsaw and
 *     the Gigarium Cluster beam bursts into explosive bolts. Beaten down to half health he is rebuilt on the spot as
 *     Modified Gigan, with chainsaw arms and homing Bladed Slicer discs. A Xilien weapon (M-Base).</li>
 * </ul>
 */
public class GiganEntity extends FlyingKaijuEntity {
    public static final int SHOWA = 0;
    public static final int FINAL_WARS = 1;
    private static final EntityDataAccessor<Boolean> DATA_MODIFIED = SynchedEntityData.defineId(GiganEntity.class, EntityDataSerializers.BOOLEAN);

    public GiganEntity(EntityType<? extends GiganEntity> type, Level level) {
        super(type, level);
        this.xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(800, 22, 10, 26, 0.24).add(Attributes.FLYING_SPEED, 0.75);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    public boolean hasMBase() {
        return true;
    }

    @Override
    public boolean controllableBy(AlienSignal signal) {
        return signal == AlienSignal.NEBULA_M || super.controllableBy(signal);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new HoldAbility("buzzsaw", "saw", HoldAbility.Mode.FRONT, 50, 5, 0.4f, 0f, 220)
                .damageType(ModDamageTypes.BUZZSAW).sound(ModSounds.GIGAN_SCREECH)
                .onHit((k, e) -> {
                    if (k.level() instanceof ServerLevel s) s.sendParticles(ParticleTypes.CRIT, e.getX(), e.getY(0.6), e.getZ(), 10, 0.4, 0.4, 0.4, 0.4);
                }));
        list.add(new BeamAbility("visor_beam", BeamStyle.VISOR, 1, 0.25f, ModDamageTypes.GIGARIUM_CLUSTER, 15, 30, 160)
                .damage(7f, 4).blocks(1.0f, true).tracking(0.2f, 0f).chargeParticle(ParticleTypes.FLAME)
                .range(4, 40).when(k -> k.getVariant() == SHOWA));
        list.add(new BeamAbility("gigarium_cluster", BeamStyle.CLUSTER, 1, 0.45f, ModDamageTypes.GIGARIUM_CLUSTER, 20, 40, 200)
                .damage(9f, 4).blocks(1.0f, false).tracking(0.15f, 0f).chargeParticle(ParticleTypes.CRIMSON_SPORE)
                .terrain((level, k, hit, radius) -> {
                    // The beam bursts into a spread of small explosive bolts where it lands.
                    if (k.tickCount % 6 != 0) return;
                    boolean grief = com.reiko45.godzilla.util.KaijuDestruction.canGrief(level, k);
                    for (int i = 0; i < 3; i++) {
                        Vec3 p = hit.add((k.getRandom().nextDouble() - 0.5) * 8, k.getRandom().nextDouble() * 2, (k.getRandom().nextDouble() - 0.5) * 8);
                        level.explode(k, p.x, p.y, p.z, 1.5f, false, grief ? ExplosionInteraction.MOB : ExplosionInteraction.NONE);
                    }
                })
                .range(4, 48).when(k -> k.getVariant() == FINAL_WARS));
        list.add(new VineGrabAbility("bloody_trigger").when(k -> k.getVariant() == FINAL_WARS));
        list.add(new SprayAbility("bladed_slicer", k -> new SlicerDiscEntity(k.level(), k), ModSounds.GIGAN_SCREECH, 1.1f, 3f, 30, 260)
                .interval(12).when(k -> ((GiganEntity) k).isModified()));
        list.add(new DiveAbility(2.2f));
        list.add(new TailSwipeAbility().when(k -> k.getVariant() == FINAL_WARS));
        list.add(new RoarAbility(ModSounds.GIGAN_SCREECH));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 1.8f, 1.8f, new Vec3(0, 8.4, 1.8), 1.4f),
                new KaijuPart(this, "tail", 2.0f, 2.0f, new Vec3(0, 1.6, -4.0), 0.8f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 9.4, 2.6);
    }

    @Override
    protected double variantScale() {
        return getVariant() == FINAL_WARS ? 2.5 : 1.35;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == FINAL_WARS ? 1300 : 800);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue((getVariant() == FINAL_WARS ? 32 : 26) * (isModified() ? 1.4 : 1.0));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MODIFIED, false);
    }

    public boolean isModified() {
        return entityData.get(DATA_MODIFIED);
    }

    @Override
    public Component getTypeName() {
        if (isModified()) return Component.translatable("entity.reikogodzilla.gigan_modified");
        return Component.translatable(getVariant() == FINAL_WARS ? "entity.reikogodzilla.gigan_final_wars" : "entity.reikogodzilla.gigan");
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide && getVariant() == FINAL_WARS && !isModified() && isAlive() && getHealth() < getMaxHealth() * 0.5f) {
            // Rebuilt in the field: chainsaws for hands and a pair of disc launchers in the chest.
            entityData.set(DATA_MODIFIED, true);
            applyVariantAttributes();
            heal(getMaxHealth() * 0.2f);
            if (isAbilityActive()) stopAbility();
            triggerAnim("action", "roar");
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(0.6), getZ(), 80, getBbWidth() * 0.6, getBbHeight() * 0.4, getBbWidth() * 0.6, 0.3);
            }
            playSound(ModSounds.GIGAN_SCREECH.get(), 8f, 0.7f);
        }
        return hurt;
    }

    @Override
    protected boolean attacksPlayersOnSight() {
        return true;
    }

    @Override
    public boolean prefersFlightInCombat() {
        return true;
    }

    @Override
    public int landChance() {
        return 400;
    }

    @Override
    protected float regenPerSecond() {
        return 0.5f;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Modified", isModified());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DATA_MODIFIED, tag.getBoolean("Modified"));
        super.readAdditionalSaveData(tag);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.GIGAN_SCREECH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GIGAN_SCREECH.get();
    }
}
