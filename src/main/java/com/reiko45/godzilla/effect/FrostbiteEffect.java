package com.reiko45.godzilla.effect;

import com.reiko45.godzilla.ReikoGodzilla;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Shimo's freezing breath (Godzilla x Kong: The New Empire froze Kong's arm solid). Slows you, weakens your blows and
 * your digging. Heat cures it: stand in fire or lava. Kong's B.E.A.S.T. Glove does the same for him.
 */
public class FrostbiteEffect extends MobEffect {
    public FrostbiteEffect() {
        super(MobEffectCategory.HARMFUL, 0xA8E4FF);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, ReikoGodzilla.id("frostbite_speed"), -0.35, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, ReikoGodzilla.id("frostbite_attack"), -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, ReikoGodzilla.id("frostbite_attack_speed"), -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, ReikoGodzilla.id("frostbite_mining"), -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.isOnFire() || entity.isInLava()) return false;
        // The frosted screen edge, without powder snow's freezing damage.
        if (entity.canFreeze()) entity.setTicksFrozen(Math.min(130, entity.getTicksFrozen() + 3));
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
