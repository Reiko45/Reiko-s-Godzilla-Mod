package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.TailSwipeAbility;
import com.reiko45.godzilla.entity.ability.VineGrabAbility;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The Skull Devil, the biggest of the Skullcrawlers (Kong: Skull Island's "Big One"). Its tongue drags prey into its
 * mouth; Kong killed it by tearing the tongue out, so its mouth takes double damage while it's grabbing.
 */
public class SkullDevilEntity extends KaijuEntity {
    public SkullDevilEntity(EntityType<? extends SkullDevilEntity> type, Level level) {
        super(type, level);
        this.xpReward = 300;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(480, 10, 4, 22, 0.3);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new VineGrabAbility("tongue_lash"));
        list.add(new TailSwipeAbility());
        list.add(new RoarAbility(ModSounds.SKULLCRAWLER_HISS));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "mouth", 1.8f, 1.6f, new Vec3(0, 4.2, 3.6), 1.3f),
                new KaijuPart(this, "tail", 1.6f, 1.2f, new Vec3(0, 1.0, -6.0), 0.7f)
        };
    }

    @Override
    public boolean hurtFromPart(KaijuPart part, DamageSource source, float amount) {
        if (part.name.equals("mouth") && getActiveAbility() != null && getActiveAbility().name.equals("tongue_lash")) amount *= 2f;
        return super.hurtFromPart(part, source, amount);
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 4.2, 4.2);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SKULLCRAWLER_HISS.get();
    }

    @Override
    public float getVoicePitch() {
        return 0.6f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.HOGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HOGLIN_DEATH;
    }
}
