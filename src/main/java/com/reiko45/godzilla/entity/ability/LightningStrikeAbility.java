package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Lightning called down on the target: MonsterVerse King Ghidorah after draining Boston's power grid fired
 * electricity from his wingtips and wrapped himself in a living superstorm.
 */
public class LightningStrikeAbility extends KaijuAbility {
    private final int bolts;
    private final float bonusDamage;

    public LightningStrikeAbility(String name, int bolts, float bonusDamage, int cooldown) {
        super(name, 20, 30, cooldown, "charge", "gust");
        this.bolts = bolts;
        this.bonusDamage = bonusDamage;
        this.minRange = 6;
        this.maxRange = 64;
        this.useChance = 0.04f;
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (!(kaiju.level() instanceof ServerLevel level) || target == null || tick % Math.max(1, 30 / bolts) != 0) return;
        Vec3 at = target.position().add((kaiju.getRandom().nextDouble() - 0.5) * 6, 0, (kaiju.getRandom().nextDouble() - 0.5) * 6);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) Math.floor(at.x), (int) Math.floor(at.z));
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        bolt.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(at.x, Math.min(y, target.getY() + 1), at.z)));
        bolt.setVisualOnly(target instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator()));
        level.addFreshEntity(bolt);
        if (target.distanceToSqr(at) < 16) target.hurt(level.damageSources().lightningBolt(), bonusDamage);
    }
}
