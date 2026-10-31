package com.reiko45.godzilla.entity.ai;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/** Guardians (Mothra) come to the aid of players being attacked by a kaiju. */
public class GuardianDefendGoal extends TargetGoal {
    private final KaijuEntity kaiju;
    @Nullable
    private LivingEntity threat;

    public GuardianDefendGoal(KaijuEntity kaiju) {
        super(kaiju, false);
        this.kaiju = kaiju;
    }

    @Override
    public boolean canUse() {
        if (kaiju.getRandom().nextInt(20) != 0) return false;
        threat = kaiju.level().getEntitiesOfClass(KaijuEntity.class, kaiju.getBoundingBox().inflate(64),
                        k -> k != kaiju && k.getTarget() instanceof Player)
                .stream().findFirst().orElse(null);
        return threat != null;
    }

    @Override
    public void start() {
        kaiju.setTarget(threat);
        super.start();
    }
}
