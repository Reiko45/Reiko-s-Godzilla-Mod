package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.LeapAbility;
import com.reiko45.godzilla.entity.ability.MissileBarrageAbility;
import com.reiko45.godzilla.entity.ability.SprayAbility;
import com.reiko45.godzilla.entity.ability.TailSwipeAbility;
import com.reiko45.godzilla.entity.mecha.MechaEntity;
import com.reiko45.godzilla.entity.projectile.HeavyProjectileEntity;
import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Apex Cybernetics' Mechagodzilla (Godzilla vs. Kong, 2021), steered through the telepathic link in King Ghidorah's
 * skull.
 * <ul>
 *     <li>R: Proton Scream (missiles while it recharges). V: rocket fist. G: thruster leap.</li>
 *     <li>On its own power it runs at about 40%: a weak Proton Scream. Use a <b>Hollow Earth Energy Core</b> on it
 *     for full power.</li>
 *     <li>At full power Ghidorah's mind can wake up in the machine: it throws out its pilot and goes rogue, a boss
 *     that attacks everything. A rogue Mechagodzilla can't be boarded, packed up or repaired.</li>
 *     <li>Kong's charged axe hits it twice as hard.</li>
 * </ul>
 */
public class ApexMechagodzillaEntity extends MechaEntity {
    private static final EntityDataAccessor<Boolean> DATA_POWERED = SynchedEntityData.defineId(ApexMechagodzillaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ROGUE = SynchedEntityData.defineId(ApexMechagodzillaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int PROTON = 0, MISSILES = 1, FIST = 2, LEAP = 4;

    public ApexMechagodzillaEntity(EntityType<? extends ApexMechagodzillaEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMechaAttributes(1400, 26, 30, 0.24, 0.5);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("proton_scream", BeamStyle.PROTON, 1, 1.0f, ModDamageTypes.PROTON_SCREAM, 40, 60, 200)
                .damage(22f, 4).blocks(2.5f, true).sounds(ModSounds.PROTON_SCREAM, ModSounds.PROTON_SCREAM)
                .chargeParticle(ParticleTypes.CRIMSON_SPORE)
                .damageScaling(k -> ((ApexMechagodzillaEntity) k).isPowered() ? 1.0 : 0.45)
                .range(4, 56));
        list.add(new MissileBarrageAbility("apex_missiles", new Vec3(-1.4, 6.6, 1.6), new Vec3(1.4, 6.6, 1.6),
                KaijuMissileEntity.Payload.EXPLOSIVE, 160).interval(2));
        list.add(new SprayAbility("rocket_fist", k -> new HeavyProjectileEntity(k.level(), k, new ItemStack(Items.IRON_BLOCK), 30f, 3.0f),
                () -> SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, 1.8f, 1f, 12, 220).interval(12).range(6, 48));
        list.add(new TailSwipeAbility());
        list.add(new LeapAbility("thruster_leap", 1.7, 1.5f, 200));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.0f, 2.0f, new Vec3(0, 8.3, 2.2), 1.4f),
                new KaijuPart(this, "tail", 2.4f, 2.0f, new Vec3(0, 1.4, -4.4), 0.8f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 8.8, 2.8);
    }

    @Override
    protected double variantScale() {
        return 2.4;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_POWERED, false);
        builder.define(DATA_ROGUE, false);
    }

    public boolean isPowered() {
        return entityData.get(DATA_POWERED);
    }

    public boolean isRogue() {
        return entityData.get(DATA_ROGUE);
    }

    @Override
    public Item packedItem() {
        return ModItems.APEX_MECHAGODZILLA.get();
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return isRogue() ? KaijuFaction.INVADER : KaijuFaction.HUMAN;
    }

    @Override
    public boolean canBePilotedBy(Player player) {
        return !isRogue();
    }

    @Override
    protected boolean allowsAttackingPlayers() {
        return isRogue();
    }

    @Override
    protected boolean attacksPlayersOnSight() {
        return isRogue();
    }

    @Override
    public boolean canWander() {
        return isRogue();
    }

    @Override
    public boolean showsBossBar() {
        return isRogue();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isRogue()) return InteractionResult.PASS;
        if (stack.is(ModItems.HOLLOW_EARTH_CORE.get()) && !isPowered()) {
            if (!level().isClientSide) {
                stack.consume(1, player);
                entityData.set(DATA_POWERED, true);
                heal(getMaxHealth());
                playSound(SoundEvents.BEACON_POWER_SELECT, 4f, 0.5f);
                if (level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.6), getZ(), 100, getBbWidth() * 0.5, getBbHeight() * 0.4, getBbWidth() * 0.5, 0.2);
                }
                player.displayClientMessage(Component.translatable("message.reikogodzilla.apex_powered").withStyle(ChatFormatting.RED), false);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    // ------------------------------------------------------------------ pilot

    @Override
    protected void onPilotAction(ServerPlayer pilot, int action) {
        switch (action) {
            case ACTION_PRIMARY -> {
                if (abilities.get(PROTON).isReady(this)) tryPilotAbility(PROTON, pilot);
                else tryPilotAbility(MISSILES, pilot);
            }
            case ACTION_SECONDARY -> tryPilotAbility(FIST, pilot);
            case ACTION_TRANSFORM -> tryPilotAbility(LEAP, pilot);
            default -> {}
        }
    }

    // ------------------------------------------------------------------ Ghidorah takes over

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (isPowered() && !isRogue() && tickCount % 200 == 0
                && (getTarget() != null || isAbilityActive() || getPilot() != null)
                && random.nextDouble() < GodzillaConfig.get(GodzillaConfig.APEX_TAKEOVER_CHANCE, 0.08)) {
            goRogue();
        }
    }

    /** Godzilla vs. Kong: at full power, Ghidorah's mind seized the link and killed its pilot. */
    public void goRogue() {
        if (isRogue() || !(level() instanceof ServerLevel server)) return;
        Player pilot = getPilot();
        if (pilot != null) {
            pilot.stopRiding();
            pilot.hurt(damageSources().magic(), 10f);
            pilot.push(0, 1.2, 0);
            pilot.hurtMarked = true;
        }
        entityData.set(DATA_ROGUE, true);
        setTarget(pilot);
        refreshBossColor();
        playSound(ModSounds.PROTON_SCREAM.get(), 8f, 0.5f);
        server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(0.7), getZ(), 120, getBbWidth() * 0.6, getBbHeight() * 0.4, getBbWidth() * 0.6, 0.4);
        for (ServerPlayer p : server.players()) {
            if (p.distanceToSqr(this) < 256 * 256) p.sendSystemMessage(Component.translatable("message.reikogodzilla.apex_rogue").withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Powered", isPowered());
        tag.putBoolean("Rogue", isRogue());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_POWERED, tag.getBoolean("Powered"));
        entityData.set(DATA_ROGUE, tag.getBoolean("Rogue"));
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return isRogue() ? ModSounds.MECHAGODZILLA_ROAR.get() : null;
    }
}
