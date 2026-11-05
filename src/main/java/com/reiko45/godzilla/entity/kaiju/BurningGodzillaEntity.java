package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.NuclearPulseAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.TailSwipeAbility;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Burning Godzilla (Godzilla vs. Destoroyah, 1995): Heisei Godzilla with his heart melting down.
 * <p>
 * He glows, scorches everything around him, and fires the Infinite Heat Ray and the nuclear pulse. A heat meter climbs
 * from the moment he appears. Freezer weapons cool him down; if the meter fills he melts down in an enormous explosion.
 * If he's killed after being chilled enough, the meltdown is contained. A Godzilla Junior nearby absorbs the radiation
 * and becomes the new Godzilla.
 */
public class BurningGodzillaEntity extends GodzillaEntity {
    private static final EntityDataAccessor<Float> DATA_HEAT = SynchedEntityData.defineId(BurningGodzillaEntity.class, EntityDataSerializers.FLOAT);

    private boolean meltedDown;

    public BurningGodzillaEntity(EntityType<? extends BurningGodzillaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 800;
        setVariant(HEISEI);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1024, 22, 12, 34, 0.25);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("infinite_heat_ray", BeamStyle.INFINITE_HEAT, 1, 1.2f, ModDamageTypes.INFINITE_HEAT_RAY, 30, 70, 160)
                .damage(22f, 4).blocks(3.0f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.FLAME).nuclear()
                .range(4, 48));
        list.add(new TailSwipeAbility());
        list.add(new RoarAbility(ModSounds.GODZILLA_ROAR));
        list.add(new NuclearPulseAbility());
    }

    @Override
    protected int pickVariant() {
        return HEISEI;
    }

    @Override
    protected float regenPerSecond() {
        // His body is breaking down: barely any healing.
        return 0.5f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HEAT, 0f);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, data);
        setHealth(getMaxHealth());
        roarNow();
        announce("message.reikogodzilla.burning", ChatFormatting.GOLD);
        return result;
    }

    public float getHeat() {
        return entityData.get(DATA_HEAT);
    }

    @Override
    public void addFreeze(float amount) {
        // Freezer weapons fight the meltdown instead of freezing him outright.
        entityData.set(DATA_HEAT, Math.max(0f, getHeat() - amount * 0.6f));
        super.addFreeze(amount * 0.3f);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (meltedDown) return;
        float rate = 100f / GodzillaConfig.get(GodzillaConfig.MELTDOWN_TICKS, 6000);
        entityData.set(DATA_HEAT, Math.min(100f, getHeat() + rate));
        if (getHeat() >= 100f) {
            meltdown(true);
            return;
        }
        if (!(level() instanceof ServerLevel server)) return;
        if (tickCount % 4 == 0) {
            server.sendParticles(ParticleTypes.FLAME, getX(), getY(0.5), getZ(), 10, getBbWidth() * 0.5, getBbHeight() * 0.4, getBbWidth() * 0.5, 0.02);
            server.sendParticles(ParticleTypes.LAVA, getX(), getY(0.6), getZ(), 2, getBbWidth() * 0.4, getBbHeight() * 0.3, getBbWidth() * 0.4, 0.0);
        }
        if (tickCount % 20 == 0) {
            // Radiation and heat pour off his body.
            double radius = 6 * getScale() + getBbWidth();
            DamageSource radiation = ModDamageTypes.source(server, ModDamageTypes.RADIATION, this);
            for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius),
                    e -> e != this && !isAlliedTo(e) && !(e instanceof com.reiko45.godzilla.entity.KaijuAligned a && !getFaction().isHostileTo(a.getFaction()))
                            && !(e instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator())))) {
                e.hurt(radiation, 4f);
                if (!e.fireImmune()) e.igniteForSeconds(3);
            }
            if (KaijuDestruction.canGrief(server, this)) {
                for (int i = 0; i < 6; i++) {
                    BlockPos pos = BlockPos.containing(getX() + (random.nextDouble() - 0.5) * radius * 2, getY() + random.nextInt(3),
                            getZ() + (random.nextDouble() - 0.5) * radius * 2);
                    if (server.getBlockState(pos).isAir() && server.getBlockState(pos.below()).isSolidRender(server, pos.below())) {
                        server.setBlockAndUpdate(pos, BaseFireBlock.getState(server, pos));
                    }
                }
            }
        }
        if (tickCount % 600 == 0) {
            for (var p : server.players()) {
                if (p.distanceToSqr(this) < 160 * 160) {
                    p.displayClientMessage(Component.translatable("message.reikogodzilla.heat", Math.round(getHeat())).withStyle(ChatFormatting.GOLD), true);
                }
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!meltedDown && !level().isClientSide) {
            // Killed: if he's been chilled enough the meltdown is contained.
            meltdown(getFreeze() < 50f && !isFrozenSolid());
        }
        super.die(source);
    }

    /** The heart gives out. Optionally explodes; a Godzilla Junior nearby takes in the radiation. */
    private void meltdown(boolean explode) {
        if (meltedDown || !(level() instanceof ServerLevel server)) return;
        meltedDown = true;
        // A Godzilla Junior nearby takes in the radiation first, so it survives the blast as the new Godzilla.
        server.getEntitiesOfClass(GodzillaJuniorEntity.class, getBoundingBox().inflate(96)).stream()
                .min(java.util.Comparator.comparingDouble(this::distanceToSqr))
                .ifPresent(GodzillaJuniorEntity::absorbMeltdown);
        if (explode && GodzillaConfig.get(GodzillaConfig.MELTDOWN_EXPLOSION, true)) {
            boolean grief = KaijuDestruction.canGrief(server, this);
            server.explode(this, ModDamageTypes.source(server, ModDamageTypes.MELTDOWN, this), null, getX(), getY(0.5), getZ(),
                    10f + 2f * getScale(), grief, grief ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
            announce("message.reikogodzilla.meltdown", ChatFormatting.DARK_RED);
        } else {
            announce("message.reikogodzilla.meltdown_contained", ChatFormatting.AQUA);
        }
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(0.5), getZ(), 4, getBbWidth(), getBbHeight() * 0.3, getBbWidth(), 0);
        if (isAlive()) hurt(ModDamageTypes.source(server, ModDamageTypes.MELTDOWN, null), Float.MAX_VALUE);
    }

    /** A warm glow that turns deep orange and pulses harder as the heat climbs. */
    @Nullable
    @Override
    public float[] getTint() {
        float heat = getHeat() / 100f;
        float pulse = 0.9f + 0.1f * (float) Math.sin(tickCount * (0.2 + 0.3 * heat));
        float[] tint = {1.0f, (0.9f - 0.3f * heat) * pulse + 0.05f, (0.8f - 0.45f * heat) * pulse + 0.03f};
        float[] frost = super.getTint();
        if (frost != null) for (int i = 0; i < 3; i++) tint[i] *= frost[i];
        return tint;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Heat", getHeat());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_HEAT, tag.getFloat("Heat"));
        if (getVariant() != HEISEI) setVariant(HEISEI);
    }
}
