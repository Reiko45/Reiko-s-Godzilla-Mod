package com.reiko45.godzilla.entity.mecha;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.MissileBarrageAbility;
import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * G-Force's Mechagodzilla (Godzilla vs. Mechagodzilla II, 1993).
 * <ul>
 *     <li>R: Mega-Buster mouth ray (then Garuda masers or paralyzer missiles while it recharges).</li>
 *     <li>V: Shock Anchors, which latch on and electrocute (and stun kaiju). Once it has soaked up enough beam energy,
 *     V fires the Plasma Grenade instead, sending that energy back stronger.</li>
 *     <li>G: undock Garuda (when combined).</li>
 * </ul>
 * Half of any beam that hits it is absorbed as Plasma Grenade energy. Fly Garuda close and press G to dock and form
 * Super Mechagodzilla: more armour, Garuda's twin masers and jet flight.
 */
public class GForceMechagodzillaEntity extends MechaEntity {
    private static final EntityDataAccessor<Boolean> DATA_SUPER = SynchedEntityData.defineId(GForceMechagodzillaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_ENERGY = SynchedEntityData.defineId(GForceMechagodzillaEntity.class, EntityDataSerializers.FLOAT);
    private static final int MEGA_BUSTER = 0, SHOCK_ANCHORS = 1, PLASMA_GRENADE = 2, MISSILES = 3, GARUDA_MASERS = 4;
    private static final float GRENADE_READY = 60f, MAX_ENERGY = 300f;

    private float grenadePower;
    private float garudaHealth = -1;

    public GForceMechagodzillaEntity(EntityType<? extends GForceMechagodzillaEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMechaAttributes(900, 24, 26, 0.2, 0.6);
    }

    @Override
    public Item packedItem() {
        return ModItems.GFORCE_MECHAGODZILLA.get();
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("mega_buster", BeamStyle.MEGA_BUSTER, 1, 0.75f, ModDamageTypes.MEGA_BUSTER, 15, 40, 120)
                .damage(12f, 4).blocks(1.6f, false).sounds(ModSounds.SPACE_BEAM_CHARGE, ModSounds.MASER_FIRE)
                .chargeParticle(ParticleTypes.ELECTRIC_SPARK).range(4, 56));
        list.add(new BeamAbility("shock_anchors", BeamStyle.ANCHOR, 2, 0.15f, ModDamageTypes.SHOCK_ANCHOR, 8, 60, 320)
                .damage(3f, 2).blocks(0f, false).tracking(1.0f, 0f)
                .onHit((k, e) -> {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3));
                    if (e instanceof KaijuEntity kaiju && kaiju.isAbilityActive() && k.getRandom().nextInt(4) == 0) kaiju.stopAbility();
                })
                .range(2, 30));
        list.add(new BeamAbility("plasma_grenade", BeamStyle.MEGA_BUSTER, 1, 1.4f, ModDamageTypes.MEGA_BUSTER, 20, 40, 200)
                .damage(10f, 4).blocks(2.5f, false).damageScaling(k -> 1.0 + ((GForceMechagodzillaEntity) k).grenadePower / 80.0)
                .sounds(ModSounds.SPACE_BEAM_CHARGE, ModSounds.ATOMIC_BREATH_FIRE).chargeParticle(ParticleTypes.END_ROD)
                .range(4, 56).when(k -> ((GForceMechagodzillaEntity) k).getEnergy() >= GRENADE_READY));
        list.add(new MissileBarrageAbility("paralyzer_missiles", new Vec3(-1.8, 7.0, 0.8), new Vec3(1.8, 7.0, 0.8),
                KaijuMissileEntity.Payload.PARALYZER, 220));
        list.add(new BeamAbility("garuda_masers", BeamStyle.MASER, 2, 0.3f, ModDamageTypes.MASER, 6, 35, 100)
                .damage(8f, 3).blocks(0.8f, false).range(4, 56).when(k -> ((GForceMechagodzillaEntity) k).isSuper()));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.0f, 2.0f, new Vec3(0, 8.3, 2.0), 1.2f),
                new KaijuPart(this, "tail", 2.4f, 2.0f, new Vec3(0, 1.6, -4.2), 0.8f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return switch (getActiveAbility() == null ? "" : getActiveAbility().name) {
            case "shock_anchors" -> new Vec3(index == 0 ? -2.0 : 2.0, 5.5, 1.8);
            case "plasma_grenade" -> new Vec3(0, 6.3, 1.6);
            case "garuda_masers" -> new Vec3(index == 0 ? -1.4 : 1.4, 8.6, -1.2);
            default -> new Vec3(0, 8.8, 2.8);
        };
    }

    @Override
    protected double variantScale() {
        return 2.0;
    }

    @Override
    protected double baseStepHeight() {
        return 3.0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SUPER, false);
        builder.define(DATA_ENERGY, 0f);
    }

    public boolean isSuper() {
        return entityData.get(DATA_SUPER);
    }

    public float getEnergy() {
        return entityData.get(DATA_ENERGY);
    }

    @Override
    public boolean canFlyNow() {
        return isSuper();
    }

    @Override
    protected double pilotFlightSpeed() {
        return 1.2;
    }

    // ------------------------------------------------------------------ Plasma Grenade energy

    @Override
    protected float absorbBeam(DamageSource source, float amount) {
        float before = getEnergy();
        entityData.set(DATA_ENERGY, Math.min(MAX_ENERGY, before + amount * 0.5f));
        if (before < GRENADE_READY && getEnergy() >= GRENADE_READY && getPilot() != null) {
            getPilot().displayClientMessage(Component.translatable("message.reikogodzilla.plasma_grenade_ready").withStyle(ChatFormatting.GOLD), true);
        }
        return amount * 0.5f;
    }

    @Override
    public void startAbility(int index, net.minecraft.world.entity.LivingEntity target) {
        if (index == PLASMA_GRENADE) {
            grenadePower = getEnergy();
            entityData.set(DATA_ENERGY, 0f);
        }
        super.startAbility(index, target);
    }

    // ------------------------------------------------------------------ pilot

    @Override
    protected void onPilotAction(ServerPlayer pilot, int action) {
        switch (action) {
            case ACTION_PRIMARY -> {
                if (abilities.get(MEGA_BUSTER).isReady(this)) tryPilotAbility(MEGA_BUSTER, pilot);
                else if (isSuper() && abilities.get(GARUDA_MASERS).isReady(this)) tryPilotAbility(GARUDA_MASERS, pilot);
                else tryPilotAbility(MISSILES, pilot);
            }
            case ACTION_SECONDARY -> tryPilotAbility(getEnergy() >= GRENADE_READY ? PLASMA_GRENADE : SHOCK_ANCHORS, pilot);
            case ACTION_TRANSFORM -> {
                if (isSuper()) undock(pilot);
            }
            default -> {}
        }
    }

    // ------------------------------------------------------------------ Super Mechagodzilla

    /** Garuda locks onto the back: Super Mechagodzilla. */
    public void dock(GarudaEntity garuda) {
        if (isSuper() || !(level() instanceof ServerLevel server)) return;
        garudaHealth = garuda.getHealth();
        var pilot = garuda.getPilot();
        garuda.discard();
        entityData.set(DATA_SUPER, true);
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(1024);
        var armor = getAttribute(Attributes.ARMOR);
        if (armor != null) armor.setBaseValue(28);
        heal(garudaHealth);
        if (pilot != null && !isVehicle()) transferPilot(pilot, this);
        server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(0.8), getZ(), 60, getBbWidth() * 0.5, 2, getBbWidth() * 0.5, 0.2);
        playSound(net.minecraft.sounds.SoundEvents.IRON_DOOR_CLOSE, 4f, 0.5f);
        for (var p : server.players()) {
            if (p.distanceToSqr(this) < 128 * 128) p.displayClientMessage(Component.translatable("message.reikogodzilla.super_mechagodzilla"), true);
        }
    }

    private void undock(ServerPlayer pilot) {
        if (!(level() instanceof ServerLevel server)) return;
        GarudaEntity garuda = ModEntities.GARUDA.get().create(server);
        if (garuda == null) return;
        garuda.moveTo(getX(), getY() + getBbHeight() + 2, getZ(), getYRot(), 0);
        garuda.finalizeSpawn(server, server.getCurrentDifficultyAt(blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        if (garudaHealth > 0) garuda.setHealth(Math.min(garuda.getMaxHealth(), garudaHealth));
        server.addFreshEntity(garuda);
        entityData.set(DATA_SUPER, false);
        setFlying(false);
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(900);
        var armor = getAttribute(Attributes.ARMOR);
        if (armor != null) armor.setBaseValue(24);
        setHealth(Math.min(getHealth(), getMaxHealth()));
        playSound(net.minecraft.sounds.SoundEvents.IRON_DOOR_OPEN, 4f, 0.5f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Super", isSuper());
        tag.putFloat("Energy", getEnergy());
        tag.putFloat("GarudaHealth", garudaHealth);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_SUPER, tag.getBoolean("Super"));
        entityData.set(DATA_ENERGY, tag.getFloat("Energy"));
        garudaHealth = tag.contains("GarudaHealth") ? tag.getFloat("GarudaHealth") : -1;
    }
}
