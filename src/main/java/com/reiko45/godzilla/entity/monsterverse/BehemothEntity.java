package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.GrabThrowAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.QuakeAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.RollAbility;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Behemoth, the woolly mammoth-like Titan of the Amazon (King of the Monsters). His tusks slam the ground hard enough
 * to stun everything nearby, and wherever he rests the forest grows back. Follows the Alpha.
 */
public class BehemothEntity extends KaijuEntity {
    public BehemothEntity(EntityType<? extends BehemothEntity> type, Level level) {
        super(type, level);
        this.xpReward = 450;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1000, 20, 10, 28, 0.22);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return alphaAware(KaijuFaction.TITAN);
    }

    @Override
    public boolean followsAlpha() {
        return true;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new QuakeAbility("tusk_slam", 9f, 1.5f, 240));
        list.add(new RollAbility("trample", "charge", "walk", 1.0));
        list.add(new GrabThrowAbility());
        list.add(new RoarAbility(ModSounds.BEHEMOTH_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{new KaijuPart(this, "head", 2.6f, 2.4f, new Vec3(0, 7.0, 3.6), 1.3f)};
    }

    @Override
    protected double variantScale() {
        return 2.2;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server) || getTarget() != null || tickCount % 100 != 0) return;
        // The forest grows back around him.
        int radius = (int) (8 + 4 * getScale());
        for (int i = 0; i < 6; i++) {
            BlockPos column = blockPosition().offset(random.nextInt(radius * 2 + 1) - radius, 0, random.nextInt(radius * 2 + 1) - radius);
            BlockPos top = server.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
            BlockState ground = server.getBlockState(top.below());
            if (!server.getBlockState(top).isAir()) continue;
            if (ground.is(Blocks.DIRT) || ground.is(Blocks.COARSE_DIRT)) {
                server.setBlockAndUpdate(top.below(), Blocks.GRASS_BLOCK.defaultBlockState());
            } else if (ground.is(Blocks.GRASS_BLOCK)) {
                BlockState plant = random.nextInt(8) == 0 ? (random.nextBoolean() ? Blocks.JUNGLE_SAPLING : Blocks.OAK_SAPLING).defaultBlockState()
                        : random.nextBoolean() ? Blocks.FERN.defaultBlockState() : Blocks.SHORT_GRASS.defaultBlockState();
                server.setBlockAndUpdate(top, plant);
            } else {
                continue;
            }
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, top.getX() + 0.5, top.getY() + 0.5, top.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.BEHEMOTH_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BEHEMOTH_ROAR.get();
    }
}
