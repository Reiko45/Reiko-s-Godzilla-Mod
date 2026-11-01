package com.reiko45.godzilla.entity.showa;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.HoldAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.TailSwipeAbility;
import com.reiko45.godzilla.registry.ModEffects;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Ebirah, Horror of the Deep (1966): a giant spiny lobster mutated by the Red Bamboo's nuclear waste, guarding the
 * waters around Letchi Island. A huge scissor claw and a spear-like pincer; he wrecks boats and drags swimmers under.
 * <p>
 * The Red Bamboo keep him off with a bitter yellow juice: anyone who has drunk Yellow Juice (and the boat they're in)
 * is left alone. He's out of his element on land, slow and weaker, and always heads back to the sea.
 * The Final Wars version is twice the size and carries M-Base.
 */
public class EbirahEntity extends KaijuEntity {
    public static final int SHOWA = 0;
    public static final int FINAL_WARS = 1;
    private int dryTicks;

    public EbirahEntity(EntityType<? extends EbirahEntity> type, Level level) {
        super(type, level);
        this.xpReward = 350;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(500, 24, 10, 24, 0.24);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    public boolean hasMBase() {
        return true;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new HoldAbility("crisis_scissors", "grab", HoldAbility.Mode.FRONT, 30, 6, 0.5f, 0f, 200).sound(ModSounds.EBIRAH_CLICK));
        list.add(new KaijuAbility("spear_claw", 8, 6, 120, "charge", "attack") {
            {
                minRange = 0;
                maxRange = 4;
                useChance = 0.06f;
            }

            @Override
            public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
                if (target == null || kaiju.distanceTo(target) > kaiju.getMeleeReach(target) + 2) return;
                // The pincer goes straight through armour.
                float damage = (float) kaiju.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.2f;
                target.hurt(kaiju.damageSources().mobAttack(kaiju), damage);
                kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.TRIDENT_HIT, SoundSource.HOSTILE, 3f, 0.6f);
            }
        });
        list.add(new TailSwipeAbility());
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "claw", 2.4f, 2.0f, new Vec3(-1.6, 1.4, 4.2), 0.9f),
                new KaijuPart(this, "tail", 3.0f, 1.8f, new Vec3(0, 0.6, -4.6), 0.8f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 2.4, 3.2);
    }

    @Override
    protected double variantScale() {
        return getVariant() == FINAL_WARS ? 2.0 : 1.0;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == FINAL_WARS ? 900 : 500);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == FINAL_WARS ? 32 : 24);
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(getVariant() == FINAL_WARS ? "entity.reikogodzilla.ebirah_final_wars" : "entity.reikogodzilla.ebirah");
    }

    /** The yellow juice: whoever has drunk it (or rides with someone who has) is safe. */
    public static boolean isRepelled(Entity e) {
        if (e instanceof LivingEntity living && living.hasEffect(ModEffects.REPELLENT)) return true;
        if (e instanceof Boat boat) return boat.getPassengers().stream().anyMatch(EbirahEntity::isRepelled);
        return e.getVehicle() != null && isRepelled(e.getVehicle());
    }

    @Override
    public boolean wantsToAttack(LivingEntity other) {
        return !isRepelled(other) && super.wantsToAttack(other);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        if (getTarget() != null && isRepelled(getTarget())) {
            setTarget(null);
            playSound(ModSounds.EBIRAH_CLICK.get(), 3f, 0.6f);
        }
        boolean wet = isInWater() || level().getFluidState(blockPosition().below()).is(FluidTags.WATER);
        dryTicks = wet ? 0 : dryTicks + 1;
        if (tickCount % 20 == 0) {
            // Boats are fair game, unless someone aboard smells of the juice.
            for (Boat boat : server.getEntitiesOfClass(Boat.class, getBoundingBox().inflate(4 + getBbWidth()))) {
                if (isRepelled(boat) || isAlliedTo(boat)) continue;
                boat.ejectPassengers();
                boat.kill();
                server.sendParticles(ParticleTypes.SPLASH, boat.getX(), boat.getY() + 0.5, boat.getZ(), 30, 1, 0.5, 1, 0.2);
                playSound(SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, 3f, 0.6f);
            }
            if (dryTicks > 200 && getTarget() == null && getDestination() == null) {
                BlockPos water = findWater(server);
                if (water != null) setDestination(water);
            }
        }
    }

    @Nullable
    private BlockPos findWater(ServerLevel server) {
        BlockPos base = blockPosition();
        for (int i = 0; i < 64; i++) {
            BlockPos p = base.offset(random.nextInt(97) - 48, random.nextInt(9) - 6, random.nextInt(97) - 48);
            if (server.getFluidState(p).is(FluidTags.WATER) && server.getFluidState(p.below()).is(FluidTags.WATER)) return p;
        }
        return null;
    }

    /** Clumsy and weak out of the water. */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (dryTicks > 100 && random.nextInt(3) == 0) return false;
        return super.doHurtTarget(target);
    }

    @Override
    public float getSpeed() {
        return dryTicks > 100 ? super.getSpeed() * 0.6f : super.getSpeed();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.EBIRAH_CLICK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.EBIRAH_CLICK.get();
    }
}
