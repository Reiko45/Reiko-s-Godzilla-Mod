package com.reiko45.godzilla.entity.showa;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.TameableKaijuEntity;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.LeapAbility;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Minilla, Son of Godzilla (1967). Hatches from the egg on Sollgel Island and attaches itself to the nearest player.
 * At first all he can manage are smoke rings; every ring is practice, and practice next to a grown Godzilla counts
 * triple. Enough practice and he breathes real atomic breath. He grows with time. Feed a wild one fish to win it over.
 */
public class MinillaEntity extends TameableKaijuEntity {
    public static final int BABY = 0;
    public static final int CHILD = 1;
    private static final int TRAINING_NEEDED = 12;
    private static final EntityDataAccessor<Boolean> DATA_BREATH = SynchedEntityData.defineId(MinillaEntity.class, EntityDataSerializers.BOOLEAN);

    private int growth;
    private int training;

    public MinillaEntity(EntityType<? extends MinillaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(120, 4, 0, 6, 0.24).add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.TITAN;
    }

    @Override
    public boolean controllableBy(AlienSignal signal) {
        return signal == AlienSignal.KILAAK;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("smoke_ring", BeamStyle.SMOKE_RING, 1, 0.5f, ModDamageTypes.ATOMIC_BREATH, 25, 30, 160)
                .damage(1.5f, 6).blocks(0f, false).chargeParticle(ParticleTypes.SMOKE)
                .range(3, 24).when(k -> !((MinillaEntity) k).hasBreath()));
        list.add(new BeamAbility("atomic_breath", BeamStyle.ATOMIC, 1, 0.5f, ModDamageTypes.ATOMIC_BREATH, 30, 40, 220)
                .damage(7f, 4).blocks(0.8f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.SOUL_FIRE_FLAME).nuclear()
                .range(3, 32).when(k -> ((MinillaEntity) k).hasBreath()));
        list.add(new LeapAbility("hop", 0.9, 1.0f, 200));
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 8.8, 2.6);
    }

    @Override
    protected double variantScale() {
        return getVariant() == CHILD ? 0.38 : 0.27;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == CHILD ? 250 : 120);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == CHILD ? 10 : 6);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BREATH, false);
    }

    public boolean hasBreath() {
        return entityData.get(DATA_BREATH);
    }

    @Override
    public void startAbility(int index, LivingEntity target) {
        super.startAbility(index, target);
        if (!level().isClientSide && abilities.get(index).name.equals("smoke_ring")) practice();
    }

    private void practice() {
        boolean coached = !level().getEntitiesOfClass(KaijuEntity.class, getBoundingBox().inflate(32), KaijuEntity::isGodzilla).isEmpty();
        training += coached ? 3 : 1;
        if (training >= TRAINING_NEEDED && !hasBreath()) {
            entityData.set(DATA_BREATH, true);
            playSound(ModSounds.MINILLA_CRY.get(), 3f, 1.2f);
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY(0.8), getZ(), 40, 0.6, 0.6, 0.6, 0.1);
            }
            if (getOwner() instanceof Player owner) {
                owner.displayClientMessage(Component.translatable("message.reikogodzilla.minilla_breath").withStyle(ChatFormatting.AQUA), false);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (getVariant() == BABY && ++growth >= GodzillaConfig.get(GodzillaConfig.MINILLA_GROWTH_TICKS, 24000)) {
            setVariant(CHILD);
            setHealth(getMaxHealth());
            triggerAnim("action", "roar");
            if (getOwner() instanceof Player owner) owner.displayClientMessage(Component.translatable("message.reikogodzilla.minilla_grew"), true);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ItemTags.FISHES)) {
            if (!isTame()) {
                stack.consume(1, player);
                if (!level().isClientSide) {
                    boolean tamed = random.nextInt(3) == 0;
                    if (tamed) setOwner(player);
                    showTamingResult(tamed);
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
            if (getHealth() < getMaxHealth()) {
                stack.consume(1, player);
                heal(20);
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        }
        if (isTame() && isOwnedBy(player) && stack.isEmpty()) {
            toggleSitting(player);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected float regenPerSecond() {
        return 1.0f;
    }

    @Override
    public boolean showsBossBar() {
        return false;
    }

    @Override
    public Component getTypeName() {
        return Component.translatable("entity.reikogodzilla.minilla");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Growth", growth);
        tag.putInt("Training", training);
        tag.putBoolean("Breath", hasBreath());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        growth = tag.getInt("Growth");
        training = tag.getInt("Training");
        entityData.set(DATA_BREATH, tag.getBoolean("Breath"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MINILLA_CRY.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MINILLA_CRY.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MINILLA_CRY.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }

    @Override
    public float getVoicePitch() {
        return 1.3f + random.nextFloat() * 0.1f;
    }
}
