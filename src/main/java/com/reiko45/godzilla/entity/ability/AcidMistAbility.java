package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/**
 * Hedorah's sulfuric acid mist: a choking cloud that poisons and withers everything inside, rusts armour, kills
 * plants and turns grass to bare earth.
 */
public class AcidMistAbility extends KaijuAbility {
    private static final DustParticleOptions SMOG = new DustParticleOptions(new Vector3f(0.45f, 0.4f, 0.3f), 3.0f);
    private static final DustParticleOptions ACID = new DustParticleOptions(new Vector3f(0.55f, 0.6f, 0.15f), 2.0f);
    private final float radius;

    public AcidMistAbility(float radius) {
        super("acid_mist", 10, 80, 320, "charge", "spray");
        this.radius = radius;
        this.minRange = 0;
        this.maxRange = 16;
        this.useChance = 0.03f;
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (!(kaiju.level() instanceof ServerLevel level)) return;
        double r = radius * kaiju.getScale() + kaiju.getBbWidth() * 0.5;
        Vec3 c = kaiju.position().add(Vec3.directionFromRotation(0, kaiju.yBodyRot).scale(r * 0.4));
        if (tick % 2 == 0) {
            level.sendParticles(SMOG, c.x, kaiju.getY() + kaiju.getBbHeight() * 0.4, c.z, 30, r * 0.5, kaiju.getBbHeight() * 0.4, r * 0.5, 0.02);
            level.sendParticles(ACID, c.x, kaiju.getY() + 1, c.z, 12, r * 0.5, 1.0, r * 0.5, 0.02);
        }
        if (tick % 20 == 0) level.playSound(null, c.x, c.y, c.z, SoundEvents.LAVA_EXTINGUISH, SoundSource.HOSTILE, 3f, 0.5f);
        if (tick % 10 != 0) return;
        for (LivingEntity e : AreaAbility.victims(kaiju, r)) {
            e.hurt(ModDamageTypes.source(level, ModDamageTypes.ACID_MIST, kaiju), e instanceof KaijuEntity ? 6f : 2.5f);
            e.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
            e.addEffect(new MobEffectInstance(MobEffects.WITHER, 50, 0));
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            if (e instanceof Player player) corrodeArmor(player);
        }
        if (!KaijuDestruction.canGrief(level, kaiju)) return;
        for (int i = 0; i < 16; i++) {
            BlockPos pos = BlockPos.containing(c.add((kaiju.getRandom().nextDouble() - 0.5) * r * 2, kaiju.getRandom().nextInt(6) - 2,
                    (kaiju.getRandom().nextDouble() - 0.5) * r * 2));
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM)) {
                level.setBlockAndUpdate(pos, Blocks.COARSE_DIRT.defaultBlockState());
            } else if (state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS) || state.getBlock() instanceof CropBlock
                    || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN)) {
                level.destroyBlock(pos, false, kaiju);
            }
        }
    }

    private static void corrodeArmor(Player player) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = player.getItemBySlot(slot);
            if (!armor.isEmpty() && armor.isDamageableItem()) armor.hurtAndBreak(3, player, slot);
        }
    }
}
