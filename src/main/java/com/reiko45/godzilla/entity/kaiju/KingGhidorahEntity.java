package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.WindGustAbility;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * King Ghidorah, the three-headed space dragon. Fires an independent gravity beam from each head (each head can
 * pick its own target), whips up hurricane winds with its wings, and calls lightning down around it.
 */
public class KingGhidorahEntity extends FlyingKaijuEntity {
    private static final Vec3[] HEADS = {new Vec3(-1.8, 10.8, 3.5), new Vec3(0, 11.3, 3.3), new Vec3(1.95, 10.7, 3.55)};

    public KingGhidorahEntity(EntityType<? extends KingGhidorahEntity> type, Level level) {
        super(type, level);
        this.xpReward = 600;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1000, 18, 8, 26, 0.22).add(Attributes.FLYING_SPEED, 0.55);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    /** The Space Hunter Nebula M sent King Ghidorah alongside Gigan (Godzilla vs. Gigan). */
    @Override
    public boolean controllableBy(com.reiko45.godzilla.world.AlienSignal signal) {
        return signal == com.reiko45.godzilla.world.AlienSignal.NEBULA_M || signal == com.reiko45.godzilla.world.AlienSignal.KILAAK;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("gravity_beams", BeamStyle.GRAVITY, 3, 0.5f, ModDamageTypes.GRAVITY_BEAM, 20, 80, 200)
                .damage(9f, 4).blocks(1.6f, false).tracking(0.1f, 3.0f).lift(0.35f).multiTarget()
                .sounds(ModSounds.GRAVITY_BEAM_CHARGE, ModSounds.GRAVITY_BEAM_FIRE)
                .chargeParticle(ParticleTypes.ELECTRIC_SPARK)
                .range(4, 52));
        list.add(new WindGustAbility(2.5f));
        list.add(new RoarAbility(ModSounds.GHIDORAH_CALL));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "left_head", 1.8f, 1.8f, HEADS[0].subtract(0, 0.9, 0), 1.25f),
                new KaijuPart(this, "middle_head", 1.8f, 1.8f, HEADS[1].subtract(0, 0.9, 0), 1.25f),
                new KaijuPart(this, "right_head", 1.8f, 1.8f, HEADS[2].subtract(0, 0.9, 0), 1.25f),
                new KaijuPart(this, "left_wing", 6.0f, 1.4f, new Vec3(-6.5, 8.0, -0.5), 0.7f),
                new KaijuPart(this, "right_wing", 6.0f, 1.4f, new Vec3(6.5, 8.0, -0.5), 0.7f),
                new KaijuPart(this, "tails", 2.0f, 1.5f, new Vec3(0, 1.0, -5.5), 0.6f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return HEADS[Math.max(0, Math.min(2, index))];
    }

    @Override
    protected float regenPerSecond() {
        return 1.0f;
    }

    @Override
    public int landChance() {
        return 300;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide && getTarget() != null && tickCount % 160 == 0 && random.nextInt(2) == 0) {
            double x = getX() + (random.nextDouble() - 0.5) * 48;
            double z = getZ() + (random.nextDouble() - 0.5) * 48;
            int y = level().getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level());
            if (bolt != null) {
                bolt.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
                bolt.setVisualOnly(true);
                level().addFreshEntity(bolt);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.GHIDORAH_CALL.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GHIDORAH_CALL.get();
    }
}
