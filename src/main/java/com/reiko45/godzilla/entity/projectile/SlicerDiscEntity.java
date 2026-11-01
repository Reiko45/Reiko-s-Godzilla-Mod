package com.reiko45.godzilla.entity.projectile;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Modified Gigan's Bladed Slicer: a spinning razor disc that homes in on its target and comes back for a second pass
 * if it misses. A kaiju that reflects beams (King Caesar's Prism Eye, a shield) sends it back at the thrower, as
 * Mothra did in Final Wars. Players can throw the ones Gigan drops.
 */
public class SlicerDiscEntity extends ThrowableItemProjectile {
    private static final double SPEED = 1.3;
    @Nullable
    private UUID targetId;
    private boolean secondPass;
    private boolean reflected;

    public SlicerDiscEntity(EntityType<? extends SlicerDiscEntity> type, Level level) {
        super(type, level);
    }

    public SlicerDiscEntity(Level level, LivingEntity thrower) {
        super(ModEntities.SLICER_DISC.get(), thrower, level);
        if (thrower instanceof Mob mob && mob.getTarget() != null) targetId = mob.getTarget().getUUID();
    }

    public void setTarget(@Nullable Entity target) {
        targetId = target == null ? null : target.getUUID();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BLADED_SLICER.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Nullable
    private Entity target() {
        return targetId != null && level() instanceof ServerLevel server ? server.getEntity(targetId) : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.CRIT, getX(), getY(), getZ(), 0, 0, 0);
            return;
        }
        if (tickCount > 160) {
            discard();
            return;
        }
        Entity target = target();
        if (target == null || !target.isAlive()) return;
        Vec3 to = target.getBoundingBox().getCenter().subtract(position());
        Vec3 motion = getDeltaMovement();
        if (!secondPass && motion.lengthSqr() > 0.01 && motion.dot(to) < 0 && to.length() > 5) secondPass = true;
        Vec3 steer = motion.scale(0.8).add(to.normalize().scale(SPEED * 0.35));
        setDeltaMovement(steer.normalize().scale(SPEED));
        if (tickCount % 6 == 0) level().playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.5f, 1.6f);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target instanceof PartEntity<?> part && part.getParent() == getOwner()) return false;
        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(level() instanceof ServerLevel server)) return;
        Entity hit = result.getEntity();
        KaijuEntity kaiju = hit instanceof KaijuEntity k ? k : hit instanceof KaijuPart p ? p.getParent() : null;
        if (!reflected && kaiju != null && kaiju.reflectsBeams() && getOwner() != null) {
            // Batted straight back at whoever threw it.
            reflected = true;
            Entity thrower = getOwner();
            setOwner(kaiju);
            setTarget(thrower);
            setDeltaMovement(getDeltaMovement().scale(-1));
            server.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 12, 0.3, 0.3, 0.3, 0.1);
            return;
        }
        if (hit == getOwner() || (kaiju != null && kaiju == getOwner())) return;
        float damage = reflected ? 24f : 12f;
        LivingEntity victim = hit instanceof LivingEntity l ? l : kaiju;
        if (victim != null) KaijuDestruction.hurtIgnoringCooldown(victim, ModDamageTypes.source(server, ModDamageTypes.BUZZSAW, getOwner()), damage);
        server.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 20, 0.4, 0.4, 0.4, 0.4);
        server.playSound(null, getX(), getY(), getZ(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.2f, 1.6f);
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level().isClientSide) return;
        if (!secondPass) {
            secondPass = true;
            setDeltaMovement(getDeltaMovement().scale(-1).add(0, 0.3, 0));
        } else {
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (targetId != null) tag.putUUID("Target", targetId);
        tag.putBoolean("SecondPass", secondPass);
        tag.putBoolean("Reflected", reflected);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        targetId = tag.hasUUID("Target") ? tag.getUUID("Target") : null;
        secondPass = tag.getBoolean("SecondPass");
        reflected = tag.getBoolean("Reflected");
    }
}
