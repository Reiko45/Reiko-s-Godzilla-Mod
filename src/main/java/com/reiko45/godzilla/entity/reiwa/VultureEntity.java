package com.reiko45.godzilla.entity.reiwa;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.*;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import com.reiko45.godzilla.world.InstantBeam;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Vulture (GODZILLA: City on the Edge of Battle): a powered suit upgraded with nanometal. R fires its rail cannons,
 * V an EMP harpoon. Flown inside a living nanometal zone it slowly assimilates its pilot; at full assimilation it
 * throws them out and joins the City. A Houtua scale in the pilot's inventory keeps the nanometal out.
 */
public class VultureEntity extends com.reiko45.godzilla.entity.mecha.MechaEntity {
    private static final int RAIL = 0, HARPOON = 1;
    private int assimilation;
    private boolean assimilated;

    public VultureEntity(EntityType<? extends VultureEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMechaAttributes(160, 14, 10, 0.3, 0.9);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new MissileBarrageAbility("rail_cannon", new Vec3(-0.6, 1.8, 0.8), new Vec3(0.6, 1.8, 0.8),
                com.reiko45.godzilla.entity.projectile.KaijuMissileEntity.Payload.RAILGUN, 40).interval(4));
        list.add(new BeamAbility("emp_harpoon", BeamStyle.NANOMETAL, 1, 0.15f, ModDamageTypes.NANOMETAL, 10, 20, 220)
                .damage(6f, 5).blocks(0f, false).onHit((k, e) -> {
                    if (e instanceof EmpSensitive emp) emp.applyEmp(100);
                    if (e instanceof KaijuEntity kaiju) kaiju.stun(20);
                }).range(2, 40));
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return assimilated ? KaijuFaction.INVADER : KaijuFaction.HUMAN;
    }

    @Override
    protected boolean allowsAttackingPlayers() {
        return assimilated;
    }

    @Override
    public boolean canBePilotedBy(Player player) {
        return !assimilated;
    }

    @Override
    public boolean alwaysFlies() {
        return true;
    }

    @Override
    protected double pilotFlightSpeed() {
        return 1.4;
    }

    @Override
    public Item packedItem() {
        return ModItems.VULTURE.get();
    }

    @Override
    protected void onPilotAction(ServerPlayer pilot, int action) {
        switch (action) {
            case ACTION_PRIMARY -> tryPilotAbility(RAIL, pilot);
            case ACTION_SECONDARY -> tryPilotAbility(HARPOON, pilot);
            default -> {}
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server) || assimilated || tickCount % 1200 != 0) return;
        Player pilot = getPilot();
        if (pilot == null) return;
        if (!com.reiko45.godzilla.world.NanometalCores.isNear(server, blockPosition(), GodzillaConfig.get(GodzillaConfig.NANOMETAL_RADIUS, 48) + 32)) return;
        if (pilot.getInventory().contains(new ItemStack(ModItems.HOUTUA_SCALE.get()))) {
            pilot.displayClientMessage(Component.translatable("message.reikogodzilla.houtua_protects"), true);
            return;
        }
        assimilation++;
        pilot.displayClientMessage(Component.translatable("message.reikogodzilla.assimilation", assimilation).withStyle(ChatFormatting.DARK_AQUA), true);
        if (assimilation >= 10) {
            assimilated = true;
            pilot.stopRiding();
            pilot.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
            pilot.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
            pilot.sendSystemMessage(Component.translatable("message.reikogodzilla.assimilated").withStyle(ChatFormatting.DARK_AQUA));
            refreshBossColor();
            for (MechagodzillaCityEntity city : server.getEntitiesOfClass(MechagodzillaCityEntity.class, getBoundingBox().inflate(256))) city.turnHostile();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Assimilation", assimilation);
        tag.putBoolean("Assimilated", assimilated);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        assimilation = tag.getInt("Assimilation");
        assimilated = tag.getBoolean("Assimilated");
    }

    @Override
    public void playWingSound() {
        playSound(ModSounds.NANOMETAL_HUM.get(), 0.6f, 1.6f);
    }
}
