package com.reiko45.godzilla.entity.mecha;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.MissileBarrageAbility;
import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/** Star Falcon: MOGUERA's upper half as a jet. R lasers, V missiles, G near Land Moguera recombines into MOGUERA. */
public class StarFalconEntity extends MechaEntity {
    public StarFalconEntity(EntityType<? extends StarFalconEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMechaAttributes(400, 16, 8, 0.2, 0.85);
    }

    @Override
    public Item packedItem() {
        return ModItems.MOGUERA.get();
    }

    @Override
    public boolean alwaysFlies() {
        return true;
    }

    @Override
    protected double pilotFlightSpeed() {
        return 1.5;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("plasma_laser", BeamStyle.PLASMA, 2, 0.3f, ModDamageTypes.PLASMA_LASER, 8, 30, 90)
                .damage(8f, 3).blocks(1.0f, false).sounds(ModSounds.MASER_FIRE, ModSounds.MASER_FIRE).range(4, 56));
        list.add(new MissileBarrageAbility("spiral_missiles", new Vec3(-1.6, 0.6, 1.0), new Vec3(1.6, 0.6, 1.0),
                KaijuMissileEntity.Payload.EXPLOSIVE, 160).interval(3));
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(index == 0 ? -0.8 : 0.8, 1.0, 3.0);
    }

    @Override
    protected void onPilotAction(ServerPlayer pilot, int action) {
        switch (action) {
            case ACTION_PRIMARY -> tryPilotAbility(0, pilot);
            case ACTION_SECONDARY -> tryPilotAbility(1, pilot);
            case ACTION_TRANSFORM -> recombine(pilot);
            default -> {}
        }
    }

    private void recombine(ServerPlayer pilot) {
        if (!(level() instanceof ServerLevel server)) return;
        var landOpt = server.getEntitiesOfClass(LandMogueraEntity.class, getBoundingBox().inflate(24))
                .stream().min(Comparator.comparingDouble(this::distanceToSqr));
        if (landOpt.isEmpty()) {
            pilot.displayClientMessage(Component.translatable("message.reikogodzilla.no_dock_target"), true);
            return;
        }
        LandMogueraEntity land = landOpt.get();
        MogueraEntity moguera = ModEntities.MOGUERA.get().create(server);
        if (moguera == null) return;
        float fraction = (getHealth() / getMaxHealth() + land.getHealth() / land.getMaxHealth()) / 2f;
        moguera.moveTo(land.getX(), land.getY(), land.getZ(), land.getYRot(), 0);
        moguera.finalizeSpawn(server, server.getCurrentDifficultyAt(land.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        moguera.setHealth(moguera.getMaxHealth() * fraction);
        server.addFreshEntity(moguera);
        land.discard();
        discard();
        transferPilot(pilot, moguera);
        server.sendParticles(ParticleTypes.ELECTRIC_SPARK, moguera.getX(), moguera.getY(0.5), moguera.getZ(), 60, 2, 4, 2, 0.2);
        moguera.playSound(net.minecraft.sounds.SoundEvents.PISTON_CONTRACT, 4f, 0.5f);
    }
}
