package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.PoisonScalesAbility;
import com.reiko45.godzilla.entity.ability.SprayAbility;
import com.reiko45.godzilla.entity.ability.WindGustAbility;
import com.reiko45.godzilla.entity.kaiju.MothraEntity;
import com.reiko45.godzilla.entity.projectile.SilkShotEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Mothra of the MonsterVerse, the Queen of the Monsters.
 * <ul>
 *     <li>Blinding "god rays" from her wings, sticky webbing that pins enemies down, stingers, and a shield of scales.</li>
 *     <li>If she dies near Godzilla, her life-force falls on him and he goes thermonuclear (King of the Monsters).</li>
 *     <li>Peacemaker: she breaks up any fight between Godzilla and Kong (Godzilla x Kong: The New Empire).</li>
 * </ul>
 */
public class MonsterverseMothraEntity extends MothraEntity {
    public MonsterverseMothraEntity(EntityType<? extends MonsterverseMothraEntity> type, Level level) {
        super(type, level);
        this.xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(700, 12, 4, 20, 0.22).add(Attributes.FLYING_SPEED, 0.7);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("god_rays", BeamStyle.GOD_RAY, 2, 0.45f, ModDamageTypes.GOD_RAY, 20, 40, 160)
                .damage(10f, 4).blocks(1.0f, false).sounds(ModSounds.MOTHRA_CRY, ModSounds.SPACE_BEAM_FIRE)
                .chargeParticle(ParticleTypes.END_ROD).range(4, 44));
        list.add(new SprayAbility("silk_spray", k -> new SilkShotEntity(k.level(), k), () -> SoundEvents.LLAMA_SPIT, 1.8f, 4f, 30, 180).range(4, 32));
        list.add(new PoisonScalesAbility());
        list.add(new WindGustAbility(3.0f));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "left_wing", 7.0f, 1.0f, new Vec3(-6.0, 1.6, 0), 0.6f),
                new KaijuPart(this, "right_wing", 7.0f, 1.0f, new Vec3(6.0, 1.6, 0), 0.6f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(index == 0 ? -3.5 : 3.5, 1.8, 1.0);
    }

    @Override
    protected double variantScale() {
        return 2.0;
    }

    @Override
    public void performMelee(LivingEntity target) {
        super.performMelee(target);
        // Stingers.
        target.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5f);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server) || tickCount % 40 != 0) return;
        // Peacemaker: no fighting between Godzilla and Kong while she's around.
        for (MonsterverseGodzillaEntity g : server.getEntitiesOfClass(MonsterverseGodzillaEntity.class, getBoundingBox().inflate(96), KaijuEntity::isAlive)) {
            for (KongEntity kong : server.getEntitiesOfClass(KongEntity.class, getBoundingBox().inflate(96), KaijuEntity::isAlive)) {
                if (g.getTarget() == kong || kong.getTarget() == g) {
                    g.calm(200);
                    kong.calm(200);
                    server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 80, getBbWidth(), 1, getBbWidth(), 0.1);
                    for (var p : server.players()) {
                        if (p.distanceToSqr(this) < 160 * 160) p.sendSystemMessage(Component.translatable("message.reikogodzilla.mothra_peace").withStyle(ChatFormatting.GOLD));
                    }
                }
            }
        }
    }

    /** King of the Monsters: her life-force falls on Godzilla like golden dust. */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!(level() instanceof ServerLevel server)) return;
        server.getEntitiesOfClass(MonsterverseGodzillaEntity.class, getBoundingBox().inflate(128), KaijuEntity::isAlive).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .ifPresent(g -> {
                    for (int i = 0; i <= 30; i++) {
                        Vec3 p = position().lerp(g.position().add(0, g.getBbHeight() * 0.6, 0), i / 30.0);
                        server.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 4, 0.5, 0.5, 0.5, 0.02);
                    }
                    g.receiveMothraEnergy();
                });
    }
}
