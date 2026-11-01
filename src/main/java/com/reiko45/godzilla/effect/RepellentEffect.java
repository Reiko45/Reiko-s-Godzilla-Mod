package com.reiko45.godzilla.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * The Red Bamboo's yellow juice (Ebirah, Horror of the Deep): Ebirah can't stand the smell and keeps away from you
 * and the boat you're in. Checked by {@link com.reiko45.godzilla.entity.showa.EbirahEntity}.
 */
public class RepellentEffect extends MobEffect {
    public RepellentEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE8D020);
    }
}
