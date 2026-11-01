package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.NuclearPulseAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.TailSwipeAbility;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import com.reiko45.godzilla.world.Radiation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Godzilla of the MonsterVerse (Legendary, 2014-2024), the Titan who keeps nature in balance.
 * <ul>
 *     <li>Feeds on radiation: nuclear warheads nearby heal him and fill his charge meter. At full charge he is
 *     <b>Energized</b> (bright blue breath, faster healing) for a few minutes.</li>
 *     <li>Taking in more while Energized overloads him: he goes <b>thermonuclear</b> (red-orange, scorching aura,
 *     repeated pulses) until he vents it. Mothra's life-force does the same (King of the Monsters).</li>
 *     <li>Absorbing Tiamat's stored solar radiation, or Tiamat dying near him, makes him <b>Evolved</b> for good
 *     (magenta plates and breath, more health): Godzilla x Kong: The New Empire.</li>
 *     <li>His gills are a weak point for explosives (Godzilla, 2014).</li>
 *     <li>His roar is the King's: Titans freed from an Alpha Call bow to him.</li>
 * </ul>
 */
public class MonsterverseGodzillaEntity extends KaijuEntity {
    public static final int BASE = 0;
    public static final int EVOLVED = 1;
    private static final int ENERGIZED_TICKS = 6000;
    private static final int THERMONUCLEAR_TICKS = 1200;

    private static final EntityDataAccessor<Float> DATA_CHARGE = SynchedEntityData.defineId(MonsterverseGodzillaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_ENERGIZED = SynchedEntityData.defineId(MonsterverseGodzillaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_THERMO = SynchedEntityData.defineId(MonsterverseGodzillaEntity.class, EntityDataSerializers.BOOLEAN);

    private int energizedTicks;
    private int thermoTicks;
    private int feedCooldown;

    public MonsterverseGodzillaEntity(EntityType<? extends MonsterverseGodzillaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 700;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1100, 22, 12, 30, 0.24);
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.TITAN;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("atomic_breath", BeamStyle.ATOMIC, 1, 1.1f, ModDamageTypes.ATOMIC_BREATH, 50, 60, 220)
                .damage(15f, 4).blocks(2.2f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.SOUL_FIRE_FLAME).nuclear()
                .styleBy(k -> ((MonsterverseGodzillaEntity) k).breathStyle())
                .damageScaling(k -> ((MonsterverseGodzillaEntity) k).breathPower())
                .range(4, 52).when(k -> !((MonsterverseGodzillaEntity) k).isThermonuclear()));
        list.add(new BeamAbility("thermonuclear_breath", BeamStyle.THERMONUCLEAR, 1, 1.3f, ModDamageTypes.ATOMIC_BREATH, 30, 70, 150)
                .damage(26f, 4).blocks(3.0f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.FLAME).nuclear()
                .range(4, 56).when(k -> ((MonsterverseGodzillaEntity) k).isThermonuclear()));
        // The drilling spiral breath (Godzilla vs. Kong): when supercharged, or against Mechagodzilla.
        list.add(new BeamAbility("spiral_breath", BeamStyle.SPIRAL_ATOMIC, 1, 0.8f, ModDamageTypes.ATOMIC_BREATH, 40, 50, 420)
                .damage(22f, 3).blocks(3.5f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.SOUL_FIRE_FLAME).nuclear()
                .range(4, 56).when(k -> ((MonsterverseGodzillaEntity) k).isEnergized() || k.getTarget() instanceof ApexMechagodzillaEntity));
        list.add(new NuclearPulseAbility());
        list.add(new TailSwipeAbility());
        list.add(new KingsRoarAbility());
    }

    /** Godzilla's roar: Titans no longer under an Alpha Call bow to him. */
    private static class KingsRoarAbility extends RoarAbility {
        KingsRoarAbility() {
            super(ModSounds.MONSTERVERSE_GODZILLA_ROAR);
        }

        @Override
        public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
            super.activate(kaiju, target);
            if (!(kaiju.level() instanceof ServerLevel level)) return;
            for (KaijuEntity k : level.getEntitiesOfClass(KaijuEntity.class, kaiju.getBoundingBox().inflate(128),
                    k -> k.isUnderAlphaCall() && (k.getAlpha() == null || k.getAlpha().distanceToSqr(k) > 192 * 192))) {
                k.releaseFromAlpha(true);
            }
        }
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.0f, 2.0f, new Vec3(0, 8.3, 2.2), 1.5f),
                new KaijuPart(this, "gills", 1.6f, 1.4f, new Vec3(0, 7.1, 1.4), 1.25f),
                new KaijuPart(this, "tail", 2.6f, 2.4f, new Vec3(0, 1.4, -4.2), 0.8f),
                new KaijuPart(this, "tail_tip", 1.8f, 1.6f, new Vec3(0, 0.6, -7.4), 0.6f)
        };
    }

    @Override
    public boolean hurtFromPart(KaijuPart part, DamageSource source, float amount) {
        // Godzilla (2014): the soldiers' explosives went for the gills.
        if (part.name.equals("gills") && source.is(DamageTypeTags.IS_EXPLOSION)) amount *= 2.0f;
        return super.hurtFromPart(part, source, amount);
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 9.0, 2.9);
    }

    @Override
    protected double variantScale() {
        return 2.4;
    }

    @Override
    protected double baseStepHeight() {
        return 3.0;
    }

    @Override
    protected float regenPerSecond() {
        return isEnergized() || isThermonuclear() ? 5.0f : 2.0f;
    }

    @Override
    protected boolean attacksPlayersOnSight() {
        return GodzillaConfig.get(GodzillaConfig.GODZILLA_HOSTILE, false);
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == EVOLVED ? 1500 : 1100);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == EVOLVED ? 36 : 30);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CHARGE, 0f);
        builder.define(DATA_ENERGIZED, false);
        builder.define(DATA_THERMO, false);
    }

    // ------------------------------------------------------------------ radiation and forms

    public float getCharge() {
        return entityData.get(DATA_CHARGE);
    }

    public boolean isEnergized() {
        return entityData.get(DATA_ENERGIZED);
    }

    public boolean isThermonuclear() {
        return entityData.get(DATA_THERMO);
    }

    public boolean isEvolved() {
        return getVariant() == EVOLVED;
    }

    BeamStyle breathStyle() {
        if (isEvolved()) return BeamStyle.EVOLVED;
        return isEnergized() ? BeamStyle.ATOMIC_CHARGED : BeamStyle.ATOMIC;
    }

    double breathPower() {
        return (isEnergized() ? 1.5 : 1.0) * (isEvolved() ? 1.4 : 1.0);
    }

    /** Radiation fed to Godzilla. Solar radiation (Tiamat's lair) evolves him. */
    public void absorbRadiation(int energy, boolean solar) {
        heal(energy * 4f);
        if (solar) evolve();
        if (isThermonuclear()) {
            thermoTicks = Math.min(THERMONUCLEAR_TICKS * 2, thermoTicks + energy * 6);
        } else if (isEnergized()) {
            startThermonuclear("message.reikogodzilla.mv_godzilla_overload");
        } else {
            float charge = getCharge() + energy;
            if (charge >= 100f) energize();
            else entityData.set(DATA_CHARGE, charge);
        }
    }

    private void energize() {
        entityData.set(DATA_CHARGE, 0f);
        entityData.set(DATA_ENERGIZED, true);
        energizedTicks = ENERGIZED_TICKS;
        roarNow();
        announce("message.reikogodzilla.mv_godzilla_energized", ChatFormatting.AQUA);
    }

    /** Mothra's sacrifice (King of the Monsters): her life-force pushes him thermonuclear. */
    public void receiveMothraEnergy() {
        heal(getMaxHealth() * 0.5f);
        startThermonuclear("message.reikogodzilla.mv_godzilla_mothra");
    }

    private void startThermonuclear(String message) {
        entityData.set(DATA_THERMO, true);
        thermoTicks = THERMONUCLEAR_TICKS;
        if (isAbilityActive()) stopAbility();
        roarNow();
        announce(message, ChatFormatting.GOLD);
    }

    private void endThermonuclear() {
        entityData.set(DATA_THERMO, false);
        entityData.set(DATA_ENERGIZED, false);
        energizedTicks = 0;
        thermalPulse(40f, 1.6);
    }

    /** Godzilla x Kong: The New Empire. Permanent. */
    public void evolve() {
        if (isEvolved()) return;
        setVariant(EVOLVED);
        setHealth(getMaxHealth());
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.6), getZ(), 200, getBbWidth(), getBbHeight() * 0.4, getBbWidth(), 0.2);
        }
        roarNow();
        announce("message.reikogodzilla.mv_godzilla_evolved", ChatFormatting.LIGHT_PURPLE);
    }

    private void roarNow() {
        level().playSound(null, getX(), getEyeY(), getZ(), ModSounds.MONSTERVERSE_GODZILLA_ROAR.get(), SoundSource.HOSTILE, 10f, 0.85f);
        triggerAnim("action", "roar");
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        if (energizedTicks > 0 && --energizedTicks <= 0 && !isThermonuclear()) entityData.set(DATA_ENERGIZED, false);
        if (isThermonuclear()) tickThermonuclear(server);

        // Feed on radiation within reach; go looking for it when hungry.
        if (feedCooldown > 0) feedCooldown--;
        if (feedCooldown <= 0 && tickCount % 20 == 0) {
            BlockPos source = Radiation.findNearest(server, position(), 10 * getScale() + getBbWidth());
            if (source != null) {
                Radiation.Absorbed absorbed = Radiation.drain(server, source, getBeamOrigin(0, 1f));
                if (absorbed != null) {
                    absorbRadiation(absorbed.energy(), absorbed.solar());
                    feedCooldown = 60;
                }
            }
        }
        if (getTarget() == null && getDestination() == null && tickCount % 200 == 0 && !isThermonuclear()) {
            BlockPos source = Radiation.findNearest(server, position(), 128);
            if (source != null) setDestination(source);
        }
    }

    private void tickThermonuclear(ServerLevel server) {
        if (--thermoTicks <= 0) {
            endThermonuclear();
            return;
        }
        if (tickCount % 4 == 0) {
            server.sendParticles(ParticleTypes.FLAME, getX(), getY(0.5), getZ(), 10, getBbWidth() * 0.5, getBbHeight() * 0.4, getBbWidth() * 0.5, 0.02);
            server.sendParticles(ParticleTypes.LAVA, getX(), getY(0.7), getZ(), 1, getBbWidth() * 0.4, getBbHeight() * 0.3, getBbWidth() * 0.4, 0.0);
        }
        if (tickCount % 20 == 0) {
            double radius = 6 * getScale() + getBbWidth();
            DamageSource radiation = ModDamageTypes.source(server, ModDamageTypes.RADIATION, this);
            for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius), this::isScorchable)) {
                e.hurt(radiation, 5f);
                if (!e.fireImmune()) e.igniteForSeconds(3);
            }
            if (KaijuDestruction.canGrief(server, this)) {
                for (int i = 0; i < 5; i++) {
                    BlockPos pos = BlockPos.containing(getX() + (random.nextDouble() - 0.5) * radius * 2, getY() + random.nextInt(3),
                            getZ() + (random.nextDouble() - 0.5) * radius * 2);
                    if (server.getBlockState(pos).isAir() && server.getBlockState(pos.below()).isSolidRender(server, pos.below())) {
                        server.setBlockAndUpdate(pos, BaseFireBlock.getState(server, pos));
                    }
                }
            }
        }
        // King of the Monsters: wave after wave of thermonuclear pulses.
        if (tickCount % 100 == 0 && getTarget() != null) thermalPulse(24f, 1.0);
    }

    private boolean isScorchable(LivingEntity e) {
        if (e == this || isAlliedTo(e)) return false;
        if (e instanceof com.reiko45.godzilla.entity.KaijuAligned a && !getFaction().isHostileTo(a.getFaction())) return false;
        return !(e instanceof Player p && (p.isCreative() || p.isSpectator() || !attacksPlayersOnSight() && getTarget() != p));
    }

    private void thermalPulse(float damage, double radiusMultiplier) {
        if (!(level() instanceof ServerLevel server)) return;
        double radius = 10 * getScale() * radiusMultiplier;
        level().playSound(null, getX(), getY(), getZ(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 8f, 0.5f);
        for (int i = 0; i < 64; i++) {
            double a = i / 64.0 * Math.PI * 2;
            server.sendParticles(ParticleTypes.FLAME, getX() + Math.cos(a) * radius * 0.6, getY(0.4), getZ() + Math.sin(a) * radius * 0.6, 3, 0.4, 0.6, 0.4, 0.05);
        }
        DamageSource pulse = ModDamageTypes.source(server, ModDamageTypes.NUCLEAR_PULSE, this);
        for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius), this::isScorchable)) {
            KaijuDestruction.hurtIgnoringCooldown(e, pulse, damage);
            Vec3 away = e.position().subtract(position()).multiply(1, 0, 1).normalize();
            e.push(away.x * 2.5, 0.7, away.z * 2.5);
        }
    }

    private void announce(String key, ChatFormatting color) {
        if (!(level() instanceof ServerLevel server)) return;
        for (var p : server.players()) {
            if (p.distanceToSqr(this) < 192 * 192) p.sendSystemMessage(Component.translatable(key).withStyle(color));
        }
    }

    @Nullable
    @Override
    public float[] getTint() {
        if (isThermonuclear()) {
            float pulse = 0.85f + 0.15f * (float) Math.sin(tickCount * 0.3);
            return new float[]{1.0f, 0.55f * pulse + 0.15f, 0.3f * pulse};
        }
        float[] frost = super.getTint();
        if (frost != null) return frost;
        if (isEnergized()) {
            float pulse = 0.9f + 0.1f * (float) Math.sin(tickCount * 0.2);
            return new float[]{0.75f * pulse, 0.95f, 1.0f};
        }
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Charge", getCharge());
        tag.putInt("Energized", energizedTicks);
        tag.putInt("Thermonuclear", thermoTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_CHARGE, tag.getFloat("Charge"));
        energizedTicks = tag.getInt("Energized");
        thermoTicks = tag.getInt("Thermonuclear");
        entityData.set(DATA_ENERGIZED, energizedTicks > 0);
        entityData.set(DATA_THERMO, thermoTicks > 0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MONSTERVERSE_GODZILLA_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GODZILLA_DEATH.get();
    }
}
