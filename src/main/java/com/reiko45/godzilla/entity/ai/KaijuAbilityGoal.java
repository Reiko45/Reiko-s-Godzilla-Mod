package com.reiko45.godzilla.entity.ai;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/** Picks a ready special attack and holds the movement flags while it runs (the kaiju ticks the ability itself). */
public class KaijuAbilityGoal extends Goal {
    private final KaijuEntity kaiju;
    private int pending = -1;

    public KaijuAbilityGoal(KaijuEntity kaiju) {
        this.kaiju = kaiju;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (kaiju.isAbilityActive()) return true;
        if (kaiju.globalAbilityCooldown > 0 || kaiju.getControllingPassenger() != null) return false;
        LivingEntity target = kaiju.getTarget();
        if (target == null || !target.isAlive()) return false;
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < kaiju.getAbilities().size(); i++) order.add(i);
        Collections.shuffle(order, new java.util.Random(kaiju.getRandom().nextLong()));
        for (int i : order) {
            KaijuAbility ability = kaiju.getAbilities().get(i);
            if (kaiju.getRandom().nextFloat() < ability.useChance() && ability.canStart(kaiju, target)) {
                pending = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        if (!kaiju.isAbilityActive() && pending >= 0) kaiju.startAbility(pending, kaiju.getTarget());
        pending = -1;
    }

    @Override
    public boolean canContinueToUse() {
        return kaiju.isAbilityActive();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
