package com.reiko45.godzilla.entity.mecha;

import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.RollAbility;
import com.reiko45.godzilla.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Land Moguera: MOGUERA's lower half, a drill tank that bores straight through terrain. R drill charge. */
public class LandMogueraEntity extends MechaEntity {
    public LandMogueraEntity(EntityType<? extends LandMogueraEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMechaAttributes(450, 22, 24, 0.24, 0.0);
    }

    @Override
    public Item packedItem() {
        return ModItems.MOGUERA.get();
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new RollAbility("drill_charge", "charge", "drill", 1.1).range(2, 40));
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 2.0, 3.0);
    }

    @Override
    protected double baseStepHeight() {
        return 2.0;
    }

    @Override
    protected void onPilotAction(ServerPlayer pilot, int action) {
        if (action == ACTION_PRIMARY && tryPilotAbility(0, pilot)) ((RollAbility) abilities.get(0)).setDirection(pilot.getLookAngle());
    }
}
