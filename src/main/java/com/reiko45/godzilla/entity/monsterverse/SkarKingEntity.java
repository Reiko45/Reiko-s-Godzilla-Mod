package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.GrabThrowAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.LeapAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.Frost;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Skar King, tyrant of the Great Apes (Godzilla x Kong: The New Empire).
 * <ul>
 *     <li>Whipslash: a whip made from a Titan's spine. Lashes from a distance and drags victims in.</li>
 *     <li>The blue crystal at its tip enslaves Shimo: while it's intact, any Shimo nearby fights for him and he can
 *     fire freezing blasts from it. Hit the crystal itself (or let Suko at it) to shatter it and free her.</li>
 *     <li>Calls his ape army once the fight turns against him. When he dies, the apes are free.</li>
 * </ul>
 */
public class SkarKingEntity extends KaijuEntity {
    private static final EntityDataAccessor<Boolean> DATA_CRYSTAL = SynchedEntityData.defineId(SkarKingEntity.class, EntityDataSerializers.BOOLEAN);
    private static final float CRYSTAL_HEALTH = 90f;
    private static final Vec3 WHIP_HAND = new Vec3(-2.3, 5.4, 1.4);

    private float crystalHealth = CRYSTAL_HEALTH;
    private boolean apesCalled;

    public SkarKingEntity(EntityType<? extends SkarKingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 600;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(900, 14, 8, 26, 0.34);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("whipslash", BeamStyle.WHIP, 1, 0.22f, ModDamageTypes.WHIPSLASH, 6, 12, 70)
                .damage(16f, 3).blocks(0.8f, false).tracking(0.6f, 0f)
                .onHit((k, e) -> {
                    Vec3 pull = k.position().subtract(e.position()).multiply(1, 0, 1).normalize();
                    double strength = e instanceof KaijuEntity ? 0.6 : 1.6;
                    e.push(pull.x * strength, 0.4, pull.z * strength);
                    e.hurtMarked = true;
                })
                .range(3, 30));
        list.add(new BeamAbility("crystal_flare", BeamStyle.FROST, 1, 0.35f, ModDamageTypes.FROST_BREATH, 15, 30, 300)
                .damage(6f, 4).blocks(2.0f, false).terrain(Frost::freezeTerrain)
                .onHit((k, e) -> Frost.chill(e, 12f))
                .range(4, 36).when(k -> ((SkarKingEntity) k).hasCrystal()));
        list.add(new LeapAbility("pounce", 1.4, 1.4f, 200));
        list.add(new GrabThrowAbility());
        list.add(new RoarAbility(ModSounds.SKAR_KING_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 1.8f, 1.8f, new Vec3(0, 8.4, 1.0), 1.4f),
                new KaijuPart(this, "crystal", 0.9f, 0.9f, WHIP_HAND.add(0, -0.4, 0.4), 0f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return WHIP_HAND;
    }

    @Override
    protected double variantScale() {
        return 1.95;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CRYSTAL, true);
    }

    public boolean hasCrystal() {
        return entityData.get(DATA_CRYSTAL);
    }

    // ------------------------------------------------------------------ the control crystal

    @Override
    public boolean hurtFromPart(KaijuPart part, DamageSource source, float amount) {
        if (part.name.equals("crystal")) {
            if (!hasCrystal()) return false;
            damageCrystal(amount, source.getEntity());
            return true;
        }
        return super.hurtFromPart(part, source, amount);
    }

    public void damageCrystal(float amount, @Nullable Entity by) {
        if (!hasCrystal() || level().isClientSide) return;
        crystalHealth -= amount;
        playSound(SoundEvents.AMETHYST_BLOCK_HIT, 3f, 0.6f);
        if (crystalHealth <= 0) shatterCrystal();
    }

    /** Suko smashed it with Kong's axe: the shockwave freed Shimo. */
    public void shatterCrystal() {
        if (!hasCrystal() || !(level() instanceof ServerLevel server)) return;
        entityData.set(DATA_CRYSTAL, false);
        if (getActiveAbility() != null && "crystal_flare".equals(getActiveAbility().name)) stopAbility();
        Vec3 at = getBeamOrigin(0, 1f);
        server.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 120, 1, 1, 1, 0.6);
        server.sendParticles(ParticleTypes.SONIC_BOOM, at.x, at.y, at.z, 1, 0, 0, 0, 0);
        server.playSound(null, at.x, at.y, at.z, SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 6f, 0.4f);
        for (ShimoEntity shimo : server.getEntitiesOfClass(ShimoEntity.class, getBoundingBox().inflate(256), s -> s.isControlledBy(this))) {
            shimo.free(this);
        }
        announce("message.reikogodzilla.crystal_shattered", ChatFormatting.AQUA);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        if (hasCrystal() && tickCount % 20 == 0) {
            // The crystal's grip on Shimo: she fights whatever he fights.
            for (ShimoEntity shimo : server.getEntitiesOfClass(ShimoEntity.class, getBoundingBox().inflate(192), LivingEntity::isAlive)) {
                if (!shimo.isControlled()) shimo.setController(this);
                if (shimo.isControlledBy(this) && getTarget() != null && getTarget() != shimo) shimo.command(getTarget());
            }
        }
        if (!apesCalled && getTarget() != null && getHealth() < getMaxHealth() * 0.75f) {
            apesCalled = true;
            callApes(server, 4);
        }
    }

    private void callApes(ServerLevel server, int count) {
        triggerAnim("action", "roar");
        playSound(ModSounds.SKAR_KING_ROAR.get(), 8f, 0.8f);
        for (int i = 0; i < count; i++) {
            GreatApeEntity ape = ModEntities.GREAT_APE.get().create(server);
            if (ape == null) continue;
            double a = random.nextDouble() * Math.PI * 2;
            double x = getX() + Math.cos(a) * (getBbWidth() + 6), z = getZ() + Math.sin(a) * (getBbWidth() + 6);
            int y = server.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);
            if (Math.abs(y - getY()) > 12) y = (int) getY();
            ape.moveTo(x, y, z, random.nextFloat() * 360f, 0);
            ape.finalizeSpawn(server, server.getCurrentDifficultyAt(ape.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            ape.setEnslaved(true);
            if (getTarget() != null) ape.setTarget(getTarget());
            server.addFreshEntity(ape);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!(level() instanceof ServerLevel server)) return;
        for (GreatApeEntity ape : server.getEntitiesOfClass(GreatApeEntity.class, getBoundingBox().inflate(160), GreatApeEntity::isEnslaved)) {
            ape.setEnslaved(false);
        }
        for (ShimoEntity shimo : server.getEntitiesOfClass(ShimoEntity.class, getBoundingBox().inflate(256), s -> s.isControlledBy(this))) {
            shimo.free(null);
        }
        announce("message.reikogodzilla.skar_king_falls", ChatFormatting.GOLD);
    }

    private void announce(String key, ChatFormatting color) {
        if (!(level() instanceof ServerLevel server)) return;
        for (var p : server.players()) {
            if (p.distanceToSqr(this) < 192 * 192) p.sendSystemMessage(Component.translatable(key).withStyle(color));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Crystal", hasCrystal());
        tag.putFloat("CrystalHealth", crystalHealth);
        tag.putBoolean("ApesCalled", apesCalled);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_CRYSTAL, !tag.contains("Crystal") || tag.getBoolean("Crystal"));
        crystalHealth = tag.contains("CrystalHealth") ? tag.getFloat("CrystalHealth") : CRYSTAL_HEALTH;
        apesCalled = tag.getBoolean("ApesCalled");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SKAR_KING_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.6f;
    }
}
