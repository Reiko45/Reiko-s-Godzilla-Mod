package com.reiko45.godzilla.entity.mecha;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Garuda: G-Force's fast attack aircraft with twin maser cannons. R fires the masers. Fly it close to a G-Force
 * Mechagodzilla and press G to dock, forming Super Mechagodzilla.
 */
public class GarudaEntity extends MechaEntity {
    public GarudaEntity(EntityType<? extends GarudaEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMechaAttributes(250, 14, 6, 0.2, 0.9);
    }

    @Override
    public Item packedItem() {
        return ModItems.GARUDA.get();
    }

    @Override
    public boolean alwaysFlies() {
        return true;
    }

    @Override
    protected double pilotFlightSpeed() {
        return 1.6;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("garuda_masers", BeamStyle.MASER, 2, 0.3f, ModDamageTypes.MASER, 6, 35, 90)
                .damage(7f, 3).blocks(0.8f, false).sounds(ModSounds.MASER_FIRE, ModSounds.MASER_FIRE).range(4, 56));
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(index == 0 ? -1.3 : 1.3, 0.6, 2.4);
    }

    @Override
    protected void onPilotAction(ServerPlayer pilot, int action) {
        if (action == ACTION_PRIMARY) tryPilotAbility(0, pilot);
        if (action == ACTION_TRANSFORM) {
            var target = level().getEntitiesOfClass(GForceMechagodzillaEntity.class, getBoundingBox().inflate(24), mg -> !mg.isSuper())
                    .stream().min(Comparator.comparingDouble(this::distanceToSqr));
            if (target.isPresent()) target.get().dock(this);
            else pilot.displayClientMessage(Component.translatable("message.reikogodzilla.no_dock_target"), true);
        }
    }
}
