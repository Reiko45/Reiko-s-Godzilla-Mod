package com.reiko45.godzilla.entity.ai;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import javax.annotation.Nullable;
import java.util.Comparator;

/** Picks fights according to faction: Invaders hunt everyone, Titans and Guardians hunt Invaders. */
public class KaijuTargetGoal extends TargetGoal {
    private final KaijuEntity kaiju;
    @Nullable
    private LivingEntity candidate;

    public KaijuTargetGoal(KaijuEntity kaiju) {
        super(kaiju, false);
        this.kaiju = kaiju;
    }

    @Override
    public boolean canUse() {
        if (kaiju.getRandom().nextInt(10) != 0 || kaiju.getControllingPassenger() != null) return false;
        double range = kaiju.getAttributeValue(Attributes.FOLLOW_RANGE);
        candidate = kaiju.level().getEntitiesOfClass(LivingEntity.class, kaiju.getBoundingBox().inflate(range, range * 0.5, range),
                        e -> kaiju.wantsToAttack(e) && kaiju.canAttack(e))
                .stream()
                .min(Comparator.comparingDouble(kaiju::distanceToSqr))
                .orElse(null);
        return candidate != null;
    }

    @Override
    public void start() {
        kaiju.setTarget(candidate);
        super.start();
    }
}
