package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.TameableKaijuEntity;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Baby Godzilla (1993) → Little Godzilla (1994) → Godzilla Junior (1995).
 * <p>
 * Hatches from the Godzilla Egg on Monster Island and imprints on the nearest player, following and defending them.
 * It grows with time (feeding it a G-Cell skips straight to the next stage). Little Godzilla blows bubble rings;
 * Junior has real atomic breath. If a Burning Godzilla melts down nearby, Junior absorbs the radiation and becomes
 * the new adult Godzilla.
 */
public class GodzillaJuniorEntity extends TameableKaijuEntity {
    public static final int BABY = 0;
    public static final int LITTLE = 1;
    public static final int JUNIOR = 2;
    private static final double[] SCALE = {0.25, 0.45, 0.8};
    private static final double[] HEALTH = {80, 250, 600};
    private static final double[] ATTACK = {4, 12, 22};

    private int growth;

    public GodzillaJuniorEntity(EntityType<? extends GodzillaJuniorEntity> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(80, 4, 0, 4, 0.26).add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.TITAN;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("bubble_breath", BeamStyle.BUBBLE, 1, 0.35f, ModDamageTypes.ATOMIC_BREATH, 20, 30, 200)
                .damage(3f, 5).blocks(0f, false).range(2, 14).when(k -> k.getVariant() == LITTLE));
        list.add(new BeamAbility("atomic_breath", BeamStyle.ATOMIC, 1, 0.6f, ModDamageTypes.ATOMIC_BREATH, 30, 40, 240)
                .damage(8f, 4).blocks(1.0f, true).nuclear().range(3, 32).when(k -> k.getVariant() == JUNIOR));
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 9.0, 2.9);
    }

    @Override
    protected double variantScale() {
        return SCALE[Math.max(0, Math.min(2, getVariant()))];
    }

    @Override
    protected double baseStepHeight() {
        return 1.2 / SCALE[Math.max(0, Math.min(2, getVariant()))];
    }

    @Override
    protected float regenPerSecond() {
        return 0.5f;
    }

    @Override
    public boolean showsBossBar() {
        return getVariant() == JUNIOR && !isTame();
    }

    @Override
    public void setVariant(int stage) {
        super.setVariant(stage);
        int s = Math.max(0, Math.min(2, stage));
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(HEALTH[s]);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(ATTACK[s]);
        setHealth(getMaxHealth());
    }

    /** Grow to the next stage. */
    public void growUp() {
        if (getVariant() >= JUNIOR) return;
        setVariant(getVariant() + 1);
        growth = 0;
        playSound(ModSounds.GODZILLA_ROAR.get(), 3f, getVoicePitch());
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY(0.5), getZ(), 30, getBbWidth(), getBbHeight() * 0.5, getBbWidth(), 0.1);
            Player owner = getOwner() instanceof Player p ? p : null;
            if (owner != null) {
                owner.displayClientMessage(Component.translatable(getVariant() == LITTLE ? "message.reikogodzilla.grew_little" : "message.reikogodzilla.grew_junior"), true);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (getVariant() < JUNIOR && ++growth >= GodzillaConfig.get(GodzillaConfig.JUNIOR_GROWTH_TICKS, 24000)) growUp();
    }

    /** Imprints on whoever is closest when it hatches. */
    public void imprintOn(Player player) {
        setOwner(player);
        showTamingResult(true);
        player.displayClientMessage(Component.translatable("message.reikogodzilla.baby_imprinted"), true);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isTame()) {
            if (stack.is(ItemTags.MEAT) || stack.is(ModItems.G_CELL.get())) {
                stack.consume(1, player);
                if (!level().isClientSide) imprintOn(player);
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
            return super.mobInteract(player, hand);
        }
        if (!isOwnedBy(player)) return super.mobInteract(player, hand);
        if (stack.is(ModItems.G_CELL.get()) && getVariant() < JUNIOR) {
            stack.consume(1, player);
            if (!level().isClientSide) growUp();
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (stack.is(ItemTags.MEAT) && getHealth() < getMaxHealth()) {
            stack.consume(1, player);
            heal(getMaxHealth() * 0.25f);
            if (!level().isClientSide) showTamingResult(true);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (player.isSecondaryUseActive()) {
            toggleSitting(player);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    /** Burning Godzilla's meltdown: Junior soaks up the radiation and becomes an adult Heisei Godzilla. */
    public void absorbMeltdown() {
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        GodzillaEntity adult = ModEntities.GODZILLA.get().create(server);
        if (adult == null) return;
        adult.moveTo(getX(), getY(), getZ(), getYRot(), 0);
        adult.finalizeSpawn(server, server.getCurrentDifficultyAt(blockPosition()), MobSpawnType.CONVERSION, null);
        adult.setVariant(GodzillaEntity.HEISEI);
        adult.setHealth(adult.getMaxHealth());
        if (hasCustomName()) adult.setCustomName(getCustomName());
        server.addFreshEntity(adult);
        server.sendParticles(ParticleTypes.FLASH, getX(), getY(0.5), getZ(), 2, 0, 0, 0, 0);
        server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY(0.5), getZ(), 150, getBbWidth() * 2, getBbHeight(), getBbWidth() * 2, 0.15);
        for (var p : server.players()) {
            if (p.distanceToSqr(this) < 192 * 192) {
                p.sendSystemMessage(Component.translatable("message.reikogodzilla.junior_reborn").withStyle(ChatFormatting.AQUA));
            }
        }
        discard();
    }

    @Override
    public float getVoicePitch() {
        return switch (getVariant()) {
            case BABY -> 1.9f;
            case LITTLE -> 1.5f;
            default -> 1.2f;
        };
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return getVariant() == BABY ? SoundEvents.TURTLE_AMBIENT_LAND : ModSounds.GODZILLA_ROAR.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.0f + getVariant() * 1.5f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return getVariant() == BABY ? SoundEvents.TURTLE_HURT : ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return getVariant() == BABY ? SoundEvents.TURTLE_DEATH : ModSounds.GODZILLA_DEATH.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Growth", growth);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        growth = tag.getInt("Growth");
    }
}
