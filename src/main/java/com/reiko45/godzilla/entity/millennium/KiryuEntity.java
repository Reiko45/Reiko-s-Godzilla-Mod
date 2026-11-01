package com.reiko45.godzilla.entity.millennium;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.entity.mecha.MechaEntity;
import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.Frost;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Kiryu, Mechagodzilla 3 (Godzilla Against Mechagodzilla, 2002): built around the skeleton of the 1954 Godzilla.
 * <ul>
 *     <li>R: twin mouth masers. V: back unit missiles and Type-0 railguns. G: the Absolute Zero Cannon, which freezes
 *     its target solid and costs 40% of Kiryu's power.</li>
 *     <li>Kiryu runs on stored energy. Everything costs power, and at zero he shuts down. Recharge him with a White
 *     Heron Power Cell or park him by a Maintenance Gantry.</li>
 *     <li>He is built on the first Godzilla's bones. When a Godzilla roars nearby, the old spirit may wake: Kiryu
 *     throws out his pilot and rampages until his batteries run dry.</li>
 *     <li>Kiryu Kai (Tokyo S.O.S., 2003), with the upgrade kit: the Absolute Zero Cannon is replaced by the Triple Hyper
 *     Maser, and the right hand becomes the Spiral Claw drill (G), which bores through anything.</li>
 * </ul>
 */
public class KiryuEntity extends MechaEntity {
    public static final int KIRYU = 0;
    public static final int KAI = 1;
    private static final int MASERS = 0, TRIPLE_MASER = 1, MISSILES = 2, RAILGUNS = 3, ZERO_CANNON = 4, SPIRAL_CLAW = 5, LEAP = 6;
    private static final float[] COST = {3, 6, 2, 2, 40, 3, 2};
    private static final EntityDataAccessor<Float> DATA_ENERGY = SynchedEntityData.defineId(KiryuEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_RAMPAGE = SynchedEntityData.defineId(KiryuEntity.class, EntityDataSerializers.INT);

    public KiryuEntity(EntityType<? extends KiryuEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMechaAttributes(1000, 30, 28, 0.24, 0.5);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("twin_masers", BeamStyle.MASER, 2, 0.35f, ModDamageTypes.MASER, 15, 40, 120)
                .damage(9f, 3).blocks(1.4f, true).tracking(0.18f, 0f).sounds(ModSounds.MASER_FIRE, ModSounds.MASER_FIRE)
                .range(4, 52).when(k -> k.getVariant() == KIRYU));
        list.add(new BeamAbility("triple_hyper_maser", BeamStyle.MASER, 3, 0.5f, ModDamageTypes.MASER, 25, 50, 160)
                .damage(13f, 3).blocks(2.0f, true).sounds(ModSounds.MASER_FIRE, ModSounds.MASER_FIRE)
                .range(4, 56).when(k -> k.getVariant() == KAI));
        list.add(new MissileBarrageAbility("back_unit", new Vec3(-1.3, 7.6, -1.2), new Vec3(1.3, 7.6, -1.2),
                KaijuMissileEntity.Payload.EXPLOSIVE, 160).interval(2));
        list.add(new MissileBarrageAbility("railguns", new Vec3(-2.3, 5.6, 1.8), new Vec3(2.3, 5.6, 1.8),
                KaijuMissileEntity.Payload.RAILGUN, 120).interval(2));
        list.add(new BeamAbility("absolute_zero_cannon", BeamStyle.FREEZE, 1, 1.2f, ModDamageTypes.ABSOLUTE_ZERO, 50, 30, 1200)
                .damage(20f, 5).sounds(ModSounds.SPACE_BEAM_CHARGE, ModSounds.SPACE_BEAM_FIRE).chargeParticle(ParticleTypes.SNOWFLAKE)
                .onHit((k, e) -> {
                    if (e instanceof KaijuEntity kaiju) kaiju.addFreeze(100f);
                    else e.setTicksFrozen(400);
                })
                .terrain((level, k, hit, radius) -> Frost.freezeTerrain(level, k, hit, radius + 3))
                .range(4, 48).when(k -> k.getVariant() == KIRYU && ((KiryuEntity) k).getEnergy() >= 40f));
        list.add(new RollAbility("spiral_claw", "charge", "drill", 1.6).when(k -> k.getVariant() == KAI));
        list.add(new LeapAbility("thruster_leap", 1.5, 1.5f, 160));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.0f, 2.0f, new Vec3(0, 8.3, 2.0), 1.3f),
                new KaijuPart(this, "tail", 2.4f, 2.0f, new Vec3(0, 1.6, -4.2), 0.8f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        if (getActiveAbility() != null && getActiveAbility().name.equals("triple_hyper_maser")) return new Vec3((index - 1) * 0.7, 6.4, 1.8);
        if (getActiveAbility() != null && getActiveAbility().name.equals("absolute_zero_cannon")) return new Vec3(0, 6.2, 1.9);
        return new Vec3(index == 0 ? -0.35 : 0.35, 9.0, 2.8);
    }

    @Override
    protected double variantScale() {
        return 1.25;
    }

    @Override
    public Item packedItem() {
        return ModItems.KIRYU.get();
    }

    @Override
    public net.minecraft.network.chat.Component getTypeName() {
        return Component.translatable(getVariant() == KAI ? "entity.reikogodzilla.kiryu_kai" : "entity.reikogodzilla.kiryu");
    }

    // ---------------------------------------------------------------- energy

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ENERGY, 100f);
        builder.define(DATA_RAMPAGE, 0);
    }

    public float getEnergy() {
        return entityData.get(DATA_ENERGY);
    }

    public void addEnergy(float amount) {
        entityData.set(DATA_ENERGY, Math.max(0f, Math.min(100f, getEnergy() + amount)));
    }

    public boolean isPoweredDown() {
        return getEnergy() <= 0f;
    }

    @Override
    public boolean isImmobile() {
        return (isPoweredDown() && !isRampaging()) || super.isImmobile();
    }

    @Override
    public void startAbility(int index, LivingEntity target) {
        float cost = index < COST.length ? COST[index] : 0;
        if (!isRampaging() && getEnergy() < cost) return;
        super.startAbility(index, target);
        if (!level().isClientSide && isAbilityActive()) addEnergy(-cost);
    }

    @Override
    protected boolean tryPilotAbility(int index, ServerPlayer pilot) {
        float cost = index < COST.length ? COST[index] : 0;
        if (getEnergy() < cost) {
            pilot.displayClientMessage(Component.translatable("message.reikogodzilla.kiryu_low_power", (int) getEnergy()).withStyle(ChatFormatting.RED), true);
            return false;
        }
        return super.tryPilotAbility(index, pilot);
    }

    @Override
    protected void onPilotAction(ServerPlayer pilot, int action) {
        switch (action) {
            case ACTION_PRIMARY -> tryPilotAbility(getVariant() == KAI ? TRIPLE_MASER : MASERS, pilot);
            case ACTION_SECONDARY -> tryPilotAbility(abilities.get(MISSILES).isReady(this) ? MISSILES : RAILGUNS, pilot);
            case ACTION_TRANSFORM -> {
                if (getVariant() == KAI) tryPilotAbility(SPIRAL_CLAW, pilot);
                else if (getEnergy() >= 40f) tryPilotAbility(ZERO_CANNON, pilot);
                else tryPilotAbility(LEAP, pilot);
            }
            default -> {}
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        boolean active = getPilot() != null || getTarget() != null || isRampaging();
        if (active && tickCount % 600 == 0) addEnergy(-1f);
        if (getPilot() != null && tickCount % 40 == 0) {
            getPilot().displayClientMessage(Component.translatable("message.reikogodzilla.kiryu_power", (int) getEnergy()), true);
        }
        int rampage = entityData.get(DATA_RAMPAGE);
        if (rampage > 0) {
            entityData.set(DATA_RAMPAGE, rampage - 1);
            if (tickCount % 10 == 0 && level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY(0.8), getZ(), 8, getBbWidth() * 0.3, getBbHeight() * 0.2, getBbWidth() * 0.3, 0.02);
            }
            if (rampage == 1) {
                // The spirit settles; the batteries are flat.
                entityData.set(DATA_ENERGY, 0f);
                setTarget(null);
                refreshBossColor();
                playSound(SoundEvents.BEACON_DEACTIVATE, 6f, 0.5f);
            }
        }
    }

    // ---------------------------------------------------------------- the first Godzilla's spirit

    public boolean isRampaging() {
        return entityData.get(DATA_RAMPAGE) > 0;
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return isRampaging() ? KaijuFaction.INVADER : KaijuFaction.HUMAN;
    }

    @Override
    protected boolean allowsAttackingPlayers() {
        return isRampaging();
    }

    @Override
    public boolean canBePilotedBy(Player player) {
        return !isRampaging();
    }

    @Override
    public boolean canWander() {
        return isRampaging();
    }

    @Override
    public void onRoarHeard(KaijuEntity roarer) {
        if (!roarer.isGodzilla() || isRampaging() || isPoweredDown() || level().isClientSide || roarer.distanceTo(this) > 80) return;
        double chance = GodzillaConfig.get(GodzillaConfig.KIRYU_RAMPAGE_CHANCE, 0.25) * (getVariant() == KAI ? 0.4 : 1.0);
        if (random.nextDouble() >= chance) return;
        entityData.set(DATA_RAMPAGE, 600);
        Player pilot = getPilot();
        if (pilot != null) pilot.stopRiding();
        if (isAbilityActive()) stopAbility();
        refreshBossColor();
        playSound(ModSounds.KIRYU_ROAR.get(), 10f, 0.8f);
        if (level() instanceof ServerLevel server) {
            for (var p : server.players()) {
                if (p.distanceToSqr(this) < 200 * 200) p.displayClientMessage(Component.translatable("message.reikogodzilla.kiryu_rampage").withStyle(ChatFormatting.DARK_RED), false);
            }
        }
    }

    // ---------------------------------------------------------------- recharging and upgrades

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ModItems.WHITE_HERON_CELL.get()) && getEnergy() < 100f) {
            stack.consume(1, player);
            addEnergy(50f);
            playSound(SoundEvents.BEACON_POWER_SELECT, 3f, 1.2f);
            player.displayClientMessage(Component.translatable("message.reikogodzilla.kiryu_power", (int) getEnergy()), true);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (stack.is(ModItems.KIRYU_KAI_KIT.get()) && getVariant() == KIRYU) {
            stack.consume(1, player);
            setVariant(KAI);
            playSound(SoundEvents.SMITHING_TABLE_USE, 3f, 0.6f);
            player.displayClientMessage(Component.translatable("message.reikogodzilla.kiryu_kai"), true);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (isRampaging()) return InteractionResult.PASS;
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Energy", getEnergy());
        tag.putInt("Rampage", entityData.get(DATA_RAMPAGE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_ENERGY, tag.contains("Energy") ? tag.getFloat("Energy") : 100f);
        entityData.set(DATA_RAMPAGE, tag.getInt("Rampage"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isRampaging() ? ModSounds.KIRYU_ROAR.get() : null;
    }
}
