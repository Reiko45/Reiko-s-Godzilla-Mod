package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.AlphaCallAbility;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.LightningStrikeAbility;
import com.reiko45.godzilla.entity.ability.WindGustAbility;
import com.reiko45.godzilla.entity.kaiju.KingGhidorahEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Monster Zero, King Ghidorah of the MonsterVerse (Godzilla: King of the Monsters, 2019): an alien "false king".
 * <ul>
 *     <li>Alpha Call: wakes the world's Titans and makes them fight for him.</li>
 *     <li>Each head fires its own gravity beam. Tear off a side head and it grows back after a while (fire and
 *     atomic breath on the stump slow that down). The first severed head leaves a <b>Ghidorah skull</b>.</li>
 *     <li>Drinks lightning: a strike heals him and charges his wingtip lightning. He drags a thunderstorm along.</li>
 *     <li>The ORCA can't calm him. When he dies, his followers bow to Godzilla.</li>
 * </ul>
 */
public class MonsterverseGhidorahEntity extends KingGhidorahEntity {
    private static final EntityDataAccessor<Integer> DATA_SEVERED = SynchedEntityData.defineId(MonsterverseGhidorahEntity.class, EntityDataSerializers.INT);
    private static final int REGROW_TICKS = 1200;

    private final float[] headDamage = new float[3];
    private final int[] regrowTicks = new int[3];
    private boolean droppedSkull;
    private int stormCharge;

    public MonsterverseGhidorahEntity(EntityType<? extends MonsterverseGhidorahEntity> type, Level level) {
        super(type, level);
        this.xpReward = 1000;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1600, 22, 10, 32, 0.24).add(Attributes.FLYING_SPEED, 0.62);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("gravity_beams", BeamStyle.GRAVITY, 3, 0.6f, ModDamageTypes.GRAVITY_BEAM, 20, 80, 180)
                .damage(8f, 4).blocks(2.0f, false).tracking(0.12f, 3.0f).lift(0.4f).multiTarget()
                .sounds(ModSounds.GRAVITY_BEAM_CHARGE, ModSounds.GRAVITY_BEAM_FIRE)
                .chargeParticle(ParticleTypes.ELECTRIC_SPARK)
                .range(4, 60));
        list.add(new AlphaCallAbility().initialCooldown(40).when(k -> com.reiko45.godzilla.GodzillaConfig.get(com.reiko45.godzilla.GodzillaConfig.ALPHA_CALL, true)));
        list.add(new WindGustAbility(3.0f));
        list.add(new LightningStrikeAbility("wingtip_lightning", 4, 12f, 260).when(k -> ((MonsterverseGhidorahEntity) k).stormCharge > 0));
    }

    @Override
    protected double variantScale() {
        return 2.2;
    }

    @Override
    protected float regenPerSecond() {
        return 2.0f;
    }

    @Override
    public boolean ignoresOrca() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SEVERED, 0);
    }

    // ------------------------------------------------------------------ heads

    public boolean isHeadSevered(int head) {
        return (entityData.get(DATA_SEVERED) & (1 << head)) != 0;
    }

    @Override
    public boolean isBeamEmitterActive(int index) {
        return !isHeadSevered(index);
    }

    @Override
    public boolean hurtFromPart(KaijuPart part, DamageSource source, float amount) {
        int head = part.name.equals("left_head") ? 0 : part.name.equals("right_head") ? 2 : -1;
        if (head >= 0) {
            if (isHeadSevered(head)) return super.hurtFromPart(part, source, amount * 0.3f);
            headDamage[head] += amount * part.damageMultiplier;
            if (!level().isClientSide && headDamage[head] > getMaxHealth() * 0.12f) severHead(head);
        }
        return super.hurtFromPart(part, source, amount);
    }

    /** King of the Monsters: Godzilla tore off the left head... and it grew back. */
    private void severHead(int head) {
        if (!(level() instanceof ServerLevel server)) return;
        entityData.set(DATA_SEVERED, entityData.get(DATA_SEVERED) | (1 << head));
        headDamage[head] = 0;
        regrowTicks[head] = REGROW_TICKS;
        Vec3 at = getBeamOrigin(head, 1f);
        server.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, 6, 1, 1, 1, 0);
        server.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 60, 1, 1, 1, 0.4);
        playSound(ModSounds.GHIDORAH_CALL.get(), 8f, 0.5f);
        if (!droppedSkull) {
            droppedSkull = true;
            spawnAtLocation(new ItemStack(ModItems.GHIDORAH_SKULL.get()), (float) (at.y - getY()));
        }
        announce("message.reikogodzilla.ghidorah_head_severed", ChatFormatting.GOLD);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Fire and atomic energy on a raw stump slow the regrowth.
        if (!level().isClientSide && (source.is(DamageTypeTags.IS_FIRE) || source.is(ModDamageTypes.ATOMIC_BREATH) || source.is(ModDamageTypes.NUCLEAR_PULSE))) {
            for (int i = 0; i < 3; i++) if (isHeadSevered(i)) regrowTicks[i] = Math.min(REGROW_TICKS * 2, regrowTicks[i] + 40);
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        for (int i = 0; i < 3; i++) {
            if (isHeadSevered(i) && --regrowTicks[i] <= 0) {
                entityData.set(DATA_SEVERED, entityData.get(DATA_SEVERED) & ~(1 << i));
                Vec3 at = getBeamOrigin(i, 1f);
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 60, 1, 1, 1, 0.4);
                announce("message.reikogodzilla.ghidorah_head_regrown", ChatFormatting.GOLD);
            }
        }
        if (stormCharge > 0) stormCharge--;
        // He brings a superstorm with him.
        if (getTarget() != null && tickCount % 600 == 0 && server.dimensionType().hasSkyLight()) {
            server.setWeatherParameters(0, 2400, true, true);
        }
    }

    /** Lightning feeds him (he drained Boston's power grid). */
    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {
        heal(getMaxHealth() * 0.03f);
        stormCharge = 1200;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!(level() instanceof ServerLevel server)) return;
        boolean kingNearby = !server.getEntitiesOfClass(MonsterverseGodzillaEntity.class, getBoundingBox().inflate(256), KaijuEntity::isAlive).isEmpty();
        int freed = 0;
        for (KaijuEntity k : server.getEntitiesOfClass(KaijuEntity.class, getBoundingBox().inflate(AlphaCallAbility.RANGE * 2), KaijuEntity::isUnderAlphaCall)) {
            k.releaseFromAlpha(true);
            freed++;
        }
        if (freed > 0 || kingNearby) {
            for (ServerPlayer p : server.players()) {
                if (p.distanceToSqr(this) < 1024 * 1024) {
                    p.sendSystemMessage(Component.translatable(kingNearby ? "message.reikogodzilla.titans_bow" : "message.reikogodzilla.alpha_fallen")
                            .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
                }
            }
        }
    }

    private void announce(String key, ChatFormatting color) {
        if (!(level() instanceof ServerLevel server)) return;
        for (ServerPlayer p : server.players()) {
            if (p.distanceToSqr(this) < 192 * 192) p.sendSystemMessage(Component.translatable(key).withStyle(color));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Severed", entityData.get(DATA_SEVERED));
        tag.putIntArray("Regrow", regrowTicks);
        tag.putBoolean("DroppedSkull", droppedSkull);
        tag.putInt("Storm", stormCharge);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_SEVERED, tag.getInt("Severed"));
        int[] r = tag.getIntArray("Regrow");
        for (int i = 0; i < Math.min(3, r.length); i++) regrowTicks[i] = r[i];
        droppedSkull = tag.getBoolean("DroppedSkull");
        stormCharge = tag.getInt("Storm");
    }
}
