package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.ability.DiveAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.SprayAbility;
import com.reiko45.godzilla.entity.ability.WindGustAbility;
import com.reiko45.godzilla.entity.kaiju.RodanEntity;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Rodan of the MonsterVerse (King of the Monsters), the "Fire Demon" woken from a volcano on Isla de Mara. Molten
 * rock drips from his wings, and the sonic boom of a low pass shatters glass and strips trees. He follows whoever is
 * Alpha: Ghidorah's call turns him, and he bows to Godzilla when it's over.
 */
public class MonsterverseRodanEntity extends RodanEntity {
    public MonsterverseRodanEntity(EntityType<? extends MonsterverseRodanEntity> type, Level level) {
        super(type, level);
        this.xpReward = 450;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(650, 14, 6, 22, 0.26).add(Attributes.FLYING_SPEED, 1.05).add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new DiveAbility(2.6f));
        list.add(new SprayAbility("lava_rain", k -> {
            Vec3 down = new Vec3(0, -1, 0);
            return new SmallFireball(k.level(), k, down);
        }, () -> SoundEvents.BLAZE_SHOOT, 0.6f, 12f, 40, 200).interval(3).range(2, 30));
        list.add(new WindGustAbility(2.6f));
        list.add(new RoarAbility(ModSounds.RODAN_SCREECH));
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return alphaAware(KaijuFaction.TITAN);
    }

    /** The MonsterVerse Rodan answers only to the Alpha, never to alien signals. */
    @Override
    public boolean hasMBase() {
        return false;
    }

    @Override
    public boolean controllableBy(com.reiko45.godzilla.world.AlienSignal signal) {
        return false;
    }

    @Override
    public boolean followsAlpha() {
        return true;
    }

    @Override
    protected double variantScale() {
        return 1.9;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, type, data);
        setVariant(SHOWA);
        setHealth(getMaxHealth());
        return result;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server) || !isFlying() || !isAlive()) return;
        if (tickCount % 6 == 0) {
            server.sendParticles(ParticleTypes.LAVA, getX(), getY(0.5), getZ(), 2, getBbWidth() * 1.5, 0.2, getBbWidth() * 1.5, 0);
            server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(0.5), getZ(), 2, getBbWidth(), 0.2, getBbWidth(), 0.01);
        }
        // Puebla: the shockwave of a supersonic fly-over tears through the town below.
        if (tickCount % 10 == 0 && getDeltaMovement().length() > 0.9 && KaijuDestruction.canGrief(server, this)) {
            BlockPos center = blockPosition();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            int broken = 0;
            for (int dx = -5; dx <= 5 && broken < 24; dx++) {
                for (int dz = -5; dz <= 5 && broken < 24; dz++) {
                    for (int dy = -16; dy <= 0 && broken < 24; dy++) {
                        pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                        BlockState state = server.getBlockState(pos);
                        if (state.is(BlockTags.LEAVES) || state.is(net.neoforged.neoforge.common.Tags.Blocks.GLASS_BLOCKS)
                                || state.is(net.neoforged.neoforge.common.Tags.Blocks.GLASS_PANES)) {
                            server.destroyBlock(pos, false);
                            broken++;
                        }
                    }
                }
            }
        }
    }
}
