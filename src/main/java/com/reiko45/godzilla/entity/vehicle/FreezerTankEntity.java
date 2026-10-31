package com.reiko45.godzilla.entity.vehicle;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModItems;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * DAG-MB96 freezer tank (Godzilla vs. Destoroyah, 1995). Drives like the Maser Tank, but its beam fills a kaiju's
 * freeze meter instead of doing much damage. A battery of these can freeze a kaiju solid or hold off Burning Godzilla's
 * meltdown.
 */
public class FreezerTankEntity extends MaserTankEntity {
    public FreezerTankEntity(EntityType<? extends FreezerTankEntity> type, Level level) {
        super(type, level);
    }

    /** Also targets Burning Godzilla, to hold off the meltdown. */
    @Override
    protected boolean isAutoTarget(KaijuEntity k) {
        return super.isAutoTarget(k) || k instanceof com.reiko45.godzilla.entity.kaiju.GodzillaEntity g && g.isBurning();
    }

    @Override
    protected float pulseDamage() {
        return 1.5f;
    }

    @Override
    protected ResourceKey<DamageType> damageType() {
        return ModDamageTypes.FREEZE_RAY;
    }

    @Override
    protected void onPulseHit(LivingEntity target) {
        if (target instanceof KaijuEntity kaiju) kaiju.addFreeze(5f);
        else target.setTicksFrozen(Math.min(target.getTicksFrozen() + 60, 400));
    }

    @Override
    protected Item packedItem() {
        return ModItems.FREEZER_TANK.get();
    }

    @Override
    public BeamStyle getBeamStyle() {
        return BeamStyle.FREEZE;
    }

    @Override
    public float getBeamWidth() {
        return 0.3f;
    }
}
