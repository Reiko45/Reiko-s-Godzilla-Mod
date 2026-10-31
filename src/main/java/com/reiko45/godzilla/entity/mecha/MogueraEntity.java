package com.reiko45.godzilla.entity.mecha;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.MissileBarrageAbility;
import com.reiko45.godzilla.entity.ability.RollAbility;
import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * MOGUERA (Godzilla vs. SpaceGodzilla, 1994): Mobile Operation Godzilla Universal Expert Robot Aero-type.
 * R fires the eye plasma lasers, V the Spiral Grenade Missiles (or the drill charge while they reload),
 * G splits it into Star Falcon (you fly) and Land Moguera (AI drill tank). Fly Star Falcon back to Land Moguera and
 * press G to recombine.
 */
public class MogueraEntity extends MechaEntity {
    private static final int PLASMA = 0, MISSILES = 1, DRILL = 2;

    public MogueraEntity(EntityType<? extends MogueraEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMechaAttributes(850, 22, 28, 0.21, 0.6);
    }

    @Override
    public Item packedItem() {
        return ModItems.MOGUERA.get();
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("plasma_laser", BeamStyle.PLASMA, 2, 0.4f, ModDamageTypes.PLASMA_LASER, 12, 40, 110)
                .damage(10f, 3).blocks(1.4f, false).sounds(ModSounds.SPACE_BEAM_CHARGE, ModSounds.MASER_FIRE)
                .chargeParticle(ParticleTypes.ELECTRIC_SPARK).range(4, 56));
        list.add(new MissileBarrageAbility("spiral_missiles", new Vec3(-1.4, 5.8, 1.6), new Vec3(1.4, 5.8, 1.6),
                KaijuMissileEntity.Payload.EXPLOSIVE, 160).interval(2));
        list.add(new RollAbility("drill_charge", "charge", "drill", 1.0));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{new KaijuPart(this, "head", 2.0f, 2.0f, new Vec3(0, 8.3, 1.6), 1.2f)};
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(index == 0 ? -0.5 : 0.5, 9.0, 2.4);
    }

    @Override
    protected double variantScale() {
        return 2.0;
    }

    @Override
    protected double baseStepHeight() {
        return 3.0;
    }

    @Override
    protected void onPilotAction(ServerPlayer pilot, int action) {
        switch (action) {
            case ACTION_PRIMARY -> tryPilotAbility(PLASMA, pilot);
            case ACTION_SECONDARY -> {
                if (abilities.get(MISSILES).isReady(this)) tryPilotAbility(MISSILES, pilot);
                else if (tryPilotAbility(DRILL, pilot)) ((RollAbility) abilities.get(DRILL)).setDirection(pilot.getLookAngle());
            }
            case ACTION_TRANSFORM -> split(pilot);
            default -> {}
        }
    }

    /** Separate into Star Falcon (piloted) and Land Moguera (AI). */
    private void split(ServerPlayer pilot) {
        if (!(level() instanceof ServerLevel server)) return;
        StarFalconEntity falcon = ModEntities.STAR_FALCON.get().create(server);
        LandMogueraEntity land = ModEntities.LAND_MOGUERA.get().create(server);
        if (falcon == null || land == null) return;
        float fraction = getHealth() / getMaxHealth();
        land.moveTo(getX(), getY(), getZ(), getYRot(), 0);
        land.finalizeSpawn(server, server.getCurrentDifficultyAt(blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        land.setHealth(land.getMaxHealth() * fraction);
        falcon.moveTo(getX(), getY() + getBbHeight() * 0.7, getZ(), getYRot(), 0);
        falcon.finalizeSpawn(server, server.getCurrentDifficultyAt(blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        falcon.setHealth(falcon.getMaxHealth() * fraction);
        server.addFreshEntity(land);
        server.addFreshEntity(falcon);
        discard();
        transferPilot(pilot, falcon);
        server.sendParticles(ParticleTypes.CLOUD, getX(), getY(0.5), getZ(), 60, getBbWidth() * 0.5, getBbHeight() * 0.4, getBbWidth() * 0.5, 0.1);
        playSound(net.minecraft.sounds.SoundEvents.PISTON_EXTEND, 4f, 0.5f);
    }
}
