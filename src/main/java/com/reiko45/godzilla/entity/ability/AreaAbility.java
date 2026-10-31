package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuAligned;
import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Helpers shared by abilities that hit everything in an area. */
final class AreaAbility {
    private AreaAbility() {}

    static List<LivingEntity> victims(KaijuEntity kaiju, double radius) {
        return kaiju.level().getEntitiesOfClass(LivingEntity.class, kaiju.getBoundingBox().inflate(radius),
                e -> isVictim(kaiju, e) && e.distanceToSqr(kaiju) <= (radius + kaiju.getBbWidth()) * (radius + kaiju.getBbWidth()));
    }

    static boolean isVictim(KaijuEntity kaiju, LivingEntity e) {
        if (e == kaiju || !e.isAlive() || kaiju.isAlliedTo(e) || kaiju.hasPassenger(e)) return false;
        if (e instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        if (e instanceof KaijuAligned a && !kaiju.getFaction().isHostileTo(a.getFaction()) && e != kaiju.getTarget()) return false;
        return true;
    }

    /** Is {@code e} inside a cone in front of the kaiju? */
    static boolean inCone(KaijuEntity kaiju, LivingEntity e, double cosHalfAngle) {
        Vec3 forward = Vec3.directionFromRotation(0, kaiju.yBodyRot);
        Vec3 to = e.position().subtract(kaiju.position()).multiply(1, 0, 1);
        if (to.lengthSqr() < 1.0E-4) return true;
        return forward.dot(to.normalize()) >= cosHalfAngle;
    }
}
