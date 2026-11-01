package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.world.Emp;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/** The male MUTO's EMP: a shockwave from its claws that kills every machine around it. */
public class EmpPulseAbility extends KaijuAbility {
    public EmpPulseAbility() {
        super("emp_pulse", 20, 10, 400, "charge", "emp");
        this.minRange = 0;
        this.maxRange = 48;
        this.useChance = 0.04f;
    }

    @Override
    public void tickCharge(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (kaiju.level() instanceof ServerLevel level && tick % 2 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, kaiju.getX(), kaiju.getY(0.6), kaiju.getZ(), 12,
                    kaiju.getBbWidth() * 0.6, kaiju.getBbHeight() * 0.3, kaiju.getBbWidth() * 0.6, 0.3);
        }
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        if (kaiju.level() instanceof ServerLevel level) {
            Emp.pulse(level, kaiju.position(), Math.min(96, 32 + 12 * kaiju.getScale()), 300, true);
        }
    }
}
