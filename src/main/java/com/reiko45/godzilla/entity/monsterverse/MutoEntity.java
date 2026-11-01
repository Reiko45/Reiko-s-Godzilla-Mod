package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.DiveAbility;
import com.reiko45.godzilla.entity.ability.EmpPulseAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.QuakeAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.registry.ModBlocks;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import com.reiko45.godzilla.world.Emp;
import com.reiko45.godzilla.world.Radiation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * MUTOs, the radiation-eating parasites of Godzilla (2014).
 * <ul>
 *     <li>The male flies, and fires EMP shockwaves from his claws that kill machines and bring down aircraft.</li>
 *     <li>The female is bigger and can't fly, but carries a constant EMP field around her.</li>
 *     <li>Both hunt radiation (nuclear warheads) and eat it. A fed female lays a nest of eggs: destroy it before
 *     it hatches.</li>
 *     <li>They fight as a pair: the female hits harder while her mate is near.</li>
 * </ul>
 */
public class MutoEntity extends FlyingKaijuEntity {
    public static final int MALE = 0;
    public static final int FEMALE = 1;

    private int nestCooldown = 2400;

    public MutoEntity(EntityType<? extends MutoEntity> type, Level level) {
        super(type, level);
        this.xpReward = 400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(600, 12, 6, 20, 0.26).add(Attributes.FLYING_SPEED, 0.8);
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new EmpPulseAbility().when(k -> k.getVariant() == MALE));
        list.add(new DiveAbility(1.8f).when(k -> k.getVariant() == MALE));
        list.add(new QuakeAbility("stomp", 6f, 1.3f, 220).when(k -> k.getVariant() == FEMALE));
        list.add(new RoarAbility(ModSounds.MUTO_CALL));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{new KaijuPart(this, "head", 1.6f, 1.4f, new Vec3(0, 5.2, 2.6), 1.4f)};
    }

    @Override
    protected double variantScale() {
        return getVariant() == FEMALE ? 1.9 : 1.3;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == FEMALE ? 800 : 520);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == FEMALE ? 26 : 18);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData data) {
        if (type != MobSpawnType.EVENT && type != MobSpawnType.CONVERSION) setVariant(random.nextInt(2));
        return super.finalizeSpawn(level, difficulty, type, data);
    }

    @Override
    public boolean canFlyNow() {
        return getVariant() == MALE;
    }

    @Override
    public net.minecraft.network.chat.Component getTypeName() {
        return Component.translatable(getVariant() == FEMALE ? "entity.reikogodzilla.muto_female" : "entity.reikogodzilla.muto_male");
    }

    /** The female's ever-present EMP field (the "Sphere of Influence"). */
    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        if (getVariant() == FEMALE && tickCount % 20 == 0) {
            Emp.pulse(server, position(), 28 + 8 * getScale(), 60, false);
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(0.7), getZ(), 6, getBbWidth() * 0.6, getBbHeight() * 0.3, getBbWidth() * 0.6, 0.2);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        if (nestCooldown > 0) nestCooldown--;
        // Feed on radiation, and hunt for it when there's nothing to fight.
        if (tickCount % 40 == 0) {
            BlockPos source = Radiation.findNearest(server, position(), 6 * getScale() + getBbWidth());
            if (source != null && Radiation.drain(server, source, getEyePosition()) != null) {
                heal(getMaxHealth() * 0.25f);
                if (getVariant() == FEMALE && nestCooldown <= 0) layNest(server);
            }
        }
        if (getTarget() == null && tickCount % 100 == 0) {
            BlockPos source = Radiation.findNearest(server, position(), 160);
            if (source != null) {
                setDestination(source);
            } else if (getVariant() == MALE) {
                // Mating call: he flies to his mate.
                server.getEntitiesOfClass(MutoEntity.class, getBoundingBox().inflate(256), m -> m.getVariant() == FEMALE && m.isAlive()).stream()
                        .findFirst().ifPresent(f -> {
                            if (distanceToSqr(f) > 48 * 48) setDestination(f.blockPosition());
                        });
            }
        }
    }

    /** Godzilla (2014): hundreds of eggs around a stolen warhead in San Francisco. */
    private void layNest(ServerLevel server) {
        nestCooldown = 12000;
        if (!KaijuDestruction.canGrief(server, this)) return;
        int eggs = 4 + random.nextInt(4);
        for (int i = 0; i < eggs * 3 && eggs > 0; i++) {
            int x = getBlockX() + random.nextInt(17) - 8, z = getBlockZ() + random.nextInt(17) - 8;
            BlockPos top = server.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
            if (server.getBlockState(top).isAir() && server.getBlockState(top.below()).isSolidRender(server, top.below())) {
                server.setBlockAndUpdate(top, ModBlocks.MUTO_EGG.get().defaultBlockState());
                eggs--;
            }
        }
        for (var p : server.players()) {
            if (p.distanceToSqr(this) < 160 * 160) p.sendSystemMessage(Component.translatable("message.reikogodzilla.muto_nest")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        // They fight as a team: she hits harder with her mate close by.
        if (hit && getVariant() == FEMALE && target instanceof LivingEntity living && level() instanceof ServerLevel server
                && !server.getEntitiesOfClass(MutoEntity.class, getBoundingBox().inflate(96), m -> m.getVariant() == MALE && m.isAlive()).isEmpty()) {
            living.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.3f);
        }
        return hit;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("NestCooldown", nestCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        nestCooldown = tag.getInt("NestCooldown");
    }

    @Override
    public int landChance() {
        return 500;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MUTO_CALL.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MUTO_CALL.get();
    }
}
