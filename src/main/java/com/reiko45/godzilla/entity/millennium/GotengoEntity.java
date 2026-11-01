package com.reiko45.godzilla.entity.millennium;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.entity.mecha.MechaEntity;
import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.Frost;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The Gotengo (Atragon, 1963; Godzilla Final Wars, 2004), the Earth Defense Force's flying battleship. It flies at
 * Mach 2, cruises underwater, and drills straight through rock (and a frozen Manda) with its bow drill.
 * <ul>
 *     <li>R: maser cannons. V: the Freezing Maser (freezes kaiju and turns water to ice). G: drill ram, which bores
 *     through the terrain.</li>
 *     <li>The electrified hull shocks anything clinging to it (Manda's coils).</li>
 * </ul>
 */
public class GotengoEntity extends MechaEntity {
    private static final int MASERS = 0, FREEZING = 1, DRILL = 2;

    public GotengoEntity(EntityType<? extends GotengoEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMechaAttributes(1400, 30, 24, 0.2, 0.75);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("maser_cannons", BeamStyle.MASER, 2, 0.45f, ModDamageTypes.MASER, 15, 40, 120)
                .damage(10f, 3).blocks(1.6f, true).sounds(ModSounds.MASER_FIRE, ModSounds.MASER_FIRE).range(4, 64));
        list.add(new BeamAbility("freezing_maser", BeamStyle.FREEZE, 1, 0.8f, ModDamageTypes.ABSOLUTE_ZERO, 25, 40, 400)
                .damage(8f, 4).chargeParticle(ParticleTypes.SNOWFLAKE)
                .onHit((k, e) -> {
                    if (e instanceof KaijuEntity kaiju) kaiju.addFreeze(30f);
                    else e.setTicksFrozen(300);
                })
                .terrain((level, k, hit, radius) -> Frost.freezeTerrain(level, k, hit, radius + 2)).range(4, 56));
        list.add(new RollAbility("drill_ram", "charge", "drill", 2.0));
        list.add(new MissileBarrageAbility("gotengo_missiles", new Vec3(-3.0, 2.6, 4.0), new Vec3(3.0, 2.6, 4.0),
                KaijuMissileEntity.Payload.EXPLOSIVE, 180).interval(3));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "bow", 6.0f, 5.0f, new Vec3(0, 0.5, 10.5), 1.0f),
                new KaijuPart(this, "stern", 6.5f, 5.5f, new Vec3(0, 0.3, -10.5), 1.0f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        if (getActiveAbility() != null && getActiveAbility().name.equals("freezing_maser")) return new Vec3(0, 2.0, 15.0);
        return new Vec3(index == 0 ? -2.2 : 2.2, 4.4, 6.0);
    }

    @Override
    public boolean alwaysFlies() {
        return true;
    }

    @Override
    protected double pilotFlightSpeed() {
        return 1.6;
    }

    @Override
    public Item packedItem() {
        return ModItems.GOTENGO.get();
    }

    @Override
    protected void onPilotAction(ServerPlayer pilot, int action) {
        switch (action) {
            case ACTION_PRIMARY -> tryPilotAbility(MASERS, pilot);
            case ACTION_SECONDARY -> tryPilotAbility(FREEZING, pilot);
            case ACTION_TRANSFORM -> tryPilotAbility(DRILL, pilot);
            default -> {}
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 10 != 0 || !(level() instanceof ServerLevel server)) return;
        // Electrified hull.
        for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(1.5 + getBbWidth() * 0.2),
                e -> e != this && !hasPassenger(e) && !isAlliedTo(e) && !(e instanceof net.minecraft.world.entity.player.Player))) {
            e.hurt(com.reiko45.godzilla.registry.ModDamageTypes.source(server, ModDamageTypes.THUNDER_SPARK, this), 6f);
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, e.getX(), e.getY(0.5), e.getZ(), 12, 0.5, 0.5, 0.5, 0.2);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    public void playWingSound() {
        playSound(ModSounds.GOTENGO_HORN.get(), 0.5f, 1.4f);
    }
}
