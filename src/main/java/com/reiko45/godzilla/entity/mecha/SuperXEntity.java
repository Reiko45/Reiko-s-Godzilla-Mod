package com.reiko45.godzilla.entity.mecha;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.MissileBarrageAbility;
import com.reiko45.godzilla.entity.ability.ReflectShieldAbility;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The Super X flying fortresses.
 * <ul>
 *     <li>Super X (1984): R cadmium missiles (shut down a kaiju's regeneration and nuclear attacks), V lasers.</li>
 *     <li>Super X2 (1989): R torpedo missiles, V the synthetic-diamond Fire Mirror, which reflects beams back
 *     stronger but melts after five hits (repair with a diamond block).</li>
 *     <li>Super X3 (1995): R cryo laser (fills the freeze meter), V freezer missiles. Built to stop Burning Godzilla.</li>
 * </ul>
 */
public class SuperXEntity extends MechaEntity {
    private static final EntityDataAccessor<Integer> DATA_MIRROR = SynchedEntityData.defineId(SuperXEntity.class, EntityDataSerializers.INT);
    public static final int MIRROR_MAX = 5;
    private static final int CADMIUM = 0, LASERS = 1, TORPEDOES = 2, FIRE_MIRROR = 3, CRYO = 4, FREEZER_MISSILES = 5;

    private final int mark;

    public SuperXEntity(EntityType<? extends SuperXEntity> type, Level level, int mark) {
        super(type, level);
        this.mark = mark;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMechaAttributes(350, 18, 4, 0.2, 0.55);
    }

    public int getMark() {
        return mark;
    }

    @Override
    public Item packedItem() {
        return switch (mark) {
            case 2 -> ModItems.SUPER_X2.get();
            case 3 -> ModItems.SUPER_X3.get();
            default -> ModItems.SUPER_X.get();
        };
    }

    @Override
    public boolean alwaysFlies() {
        return true;
    }

    @Override
    protected double pilotFlightSpeed() {
        return 1.1;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        // The list is built before "mark" is assigned, so every mark gets every weapon and they are gated per mark.
        list.add(new MissileBarrageAbility("cadmium_missiles", new Vec3(-1.5, 0.8, 2.0), new Vec3(1.5, 0.8, 2.0),
                KaijuMissileEntity.Payload.CADMIUM, 200).interval(4).when(k -> ((SuperXEntity) k).mark != 2));
        list.add(new BeamAbility("super_x_lasers", BeamStyle.MASER, 2, 0.2f, ModDamageTypes.MASER, 5, 30, 80)
                .damage(6f, 3).blocks(0.6f, false).sounds(ModSounds.MASER_FIRE, ModSounds.MASER_FIRE).range(4, 48)
                .when(k -> ((SuperXEntity) k).mark == 1));
        list.add(new MissileBarrageAbility("torpedo_missiles", new Vec3(-1.5, 0.6, 2.0), new Vec3(1.5, 0.6, 2.0),
                KaijuMissileEntity.Payload.EXPLOSIVE, 120).interval(3).when(k -> ((SuperXEntity) k).mark == 2));
        list.add(new ReflectShieldAbility("fire_mirror", ParticleTypes.END_ROD)
                .when(k -> ((SuperXEntity) k).mark == 2 && ((SuperXEntity) k).getMirror() > 0));
        list.add(new BeamAbility("cryo_laser", BeamStyle.FREEZE, 1, 0.45f, ModDamageTypes.FREEZE_RAY, 10, 40, 140)
                .damage(2f, 2).blocks(0f, false).tracking(0.3f, 0f)
                .onHit((k, e) -> {
                    if (e instanceof KaijuEntity kaiju) kaiju.addFreeze(4f);
                    else e.setTicksFrozen(Math.min(e.getTicksFrozen() + 40, 400));
                })
                .range(4, 48).when(k -> ((SuperXEntity) k).mark == 3));
        list.add(new MissileBarrageAbility("freezer_missiles", new Vec3(-2.0, 0.6, 2.0), new Vec3(2.0, 0.6, 2.0),
                KaijuMissileEntity.Payload.FREEZER, 160).interval(4).when(k -> ((SuperXEntity) k).mark == 3));
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(index == 0 ? -0.9 : 0.9, 0.8, 3.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MIRROR, MIRROR_MAX);
    }

    public int getMirror() {
        return entityData.get(DATA_MIRROR);
    }

    // ------------------------------------------------------------------ Fire Mirror

    @Override
    public boolean reflectsBeams() {
        return mark == 2 && getMirror() > 0 && ReflectShieldAbility.isActive(this);
    }

    @Override
    protected float onBeamReflected(DamageSource source, float amount) {
        int left = getMirror() - 1;
        entityData.set(DATA_MIRROR, left);
        if (left <= 0) {
            stopAbility();
            playSound(SoundEvents.GLASS_BREAK, 4f, 0.6f);
            if (getPilot() != null) getPilot().displayClientMessage(Component.translatable("message.reikogodzilla.fire_mirror_melted").withStyle(ChatFormatting.RED), true);
        }
        return 1.5f;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (mark == 2 && stack.is(Items.DIAMOND_BLOCK) && getMirror() < MIRROR_MAX) {
            stack.consume(1, player);
            entityData.set(DATA_MIRROR, MIRROR_MAX);
            playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 2f, 1f);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    // ------------------------------------------------------------------ pilot

    @Override
    protected void onPilotAction(ServerPlayer pilot, int action) {
        int index = switch (mark) {
            case 2 -> action == ACTION_PRIMARY ? TORPEDOES : action == ACTION_SECONDARY ? FIRE_MIRROR : -1;
            case 3 -> action == ACTION_PRIMARY ? CRYO : action == ACTION_SECONDARY ? FREEZER_MISSILES : -1;
            default -> action == ACTION_PRIMARY ? CADMIUM : action == ACTION_SECONDARY ? LASERS : -1;
        };
        if (index == FIRE_MIRROR && getMirror() <= 0) {
            pilot.displayClientMessage(Component.translatable("message.reikogodzilla.fire_mirror_melted").withStyle(ChatFormatting.RED), true);
            return;
        }
        if (index >= 0) tryPilotAbility(index, pilot);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FireMirror", getMirror());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("FireMirror")) entityData.set(DATA_MIRROR, tag.getInt("FireMirror"));
    }
}
