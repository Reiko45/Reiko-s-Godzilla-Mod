package com.reiko45.godzilla.entity.ai;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Roams to random nearby spots, or toward a set destination (for example, the coast during an event). */
public class KaijuWanderGoal extends Goal {
    private final KaijuEntity kaiju;
    private Vec3 goal;
    private int timer;
    private Vec3 lastPos = Vec3.ZERO;
    private int stuck;

    public KaijuWanderGoal(KaijuEntity kaiju) {
        this.kaiju = kaiju;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (kaiju.getTarget() != null || kaiju.isVehicle() || kaiju instanceof FlyingKaijuEntity f && f.isFlying()) return false;
        if (!kaiju.canWander()) return false;
        BlockPos dest = kaiju.getDestination();
        if (dest != null) {
            if (dest.distToCenterSqr(kaiju.position()) < Math.pow(kaiju.getBbWidth() * 2 + 4, 2)) {
                kaiju.setDestination(null);
                return false;
            }
            goal = Vec3.atBottomCenterOf(dest);
            return true;
        }
        if (kaiju.getRandom().nextInt(160) != 0) return false;
        double angle = kaiju.getRandom().nextDouble() * Math.PI * 2;
        double dist = (12 + kaiju.getRandom().nextDouble() * 28) * Math.max(1.0, Math.sqrt(kaiju.getScale()));
        int x = (int) (kaiju.getX() + Math.cos(angle) * dist);
        int z = (int) (kaiju.getZ() + Math.sin(angle) * dist);
        if (!kaiju.level().hasChunkAt(new BlockPos(x, 0, z))) return false;
        int y = kaiju.level().getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        goal = new Vec3(x + 0.5, y, z + 0.5);
        return true;
    }

    @Override
    public void start() {
        timer = 0;
        stuck = 0;
        lastPos = kaiju.position();
    }

    @Override
    public boolean canContinueToUse() {
        return goal != null && timer < 600 && stuck < 60 && kaiju.getTarget() == null
                && !(kaiju instanceof FlyingKaijuEntity f && f.isFlying())
                && goal.distanceToSqr(kaiju.getX(), goal.y, kaiju.getZ()) > Math.pow(kaiju.getBbWidth() * 0.5 + 1, 2);
    }

    @Override
    public void tick() {
        timer++;
        kaiju.getMoveControl().setWantedPosition(goal.x, goal.y, goal.z, kaiju.getDestination() != null ? 1.0 : 0.6);
        if (timer % 20 == 0) {
            if (kaiju.position().distanceToSqr(lastPos) < 0.25) stuck += 20;
            else stuck = 0;
            lastPos = kaiju.position();
        }
    }

    @Override
    public void stop() {
        goal = null;
    }
}
