package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.QuakeAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.TailSwipeAbility;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import com.reiko45.godzilla.world.Frost;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Shimo, the ice Titan who started the last Ice Age (Godzilla x Kong: The New Empire). The tallest Titan of all.
 * <ul>
 *     <li>Frost Bite Blast: a freezing beam that leaves ice where it hits. Kaiju freeze solid; anything else gets
 *     frostbite. Water and lava freeze around her as she walks.</li>
 *     <li>While the Skar King's crystal controls her she is chained in his realm and fights for him, and her
 *     unchecked breath brings snow and storms. Shatter the crystal and she turns on him.</li>
 *     <li>Can't be frozen; fire and atomic breath hurt her more.</li>
 * </ul>
 */
public class ShimoEntity extends KaijuEntity {
    private static final EntityDataAccessor<Boolean> DATA_CONTROLLED = SynchedEntityData.defineId(ShimoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CHAINED = SynchedEntityData.defineId(ShimoEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private UUID controllerId;
    private boolean warnedIceAge;

    public ShimoEntity(EntityType<? extends ShimoEntity> type, Level level) {
        super(type, level);
        this.xpReward = 700;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1300, 20, 10, 30, 0.2);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return isControlled() ? KaijuFaction.INVADER : KaijuFaction.TITAN;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("frost_bite_blast", BeamStyle.FROST, 1, 1.2f, ModDamageTypes.FROST_BREATH, 50, 60, 240)
                .damage(12f, 4).blocks(3.0f, false).terrain(Frost::freezeTerrain)
                .onHit((k, e) -> Frost.chill(e, 22f))
                .sounds(ModSounds.SHIMO_ROAR, ModSounds.ATOMIC_BREATH_FIRE).chargeParticle(ParticleTypes.SNOWFLAKE)
                .range(4, 52));
        list.add(new QuakeAbility("ice_stomp", 8f, 1.2f, 300));
        list.add(new TailSwipeAbility());
        list.add(new RoarAbility(ModSounds.SHIMO_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.2f, 2.0f, new Vec3(0, 8.0, 5.0), 1.4f),
                new KaijuPart(this, "tail", 2.4f, 2.0f, new Vec3(0, 3.0, -6.0), 0.7f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 8.6, 6.2);
    }

    @Override
    protected double variantScale() {
        return 2.3;
    }

    @Override
    protected double baseStepHeight() {
        return 3.0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CONTROLLED, false);
        builder.define(DATA_CHAINED, false);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData data) {
        // In the Skar King's realm she is found chained in her cavern.
        if (type == MobSpawnType.STRUCTURE) entityData.set(DATA_CHAINED, true);
        return super.finalizeSpawn(level, difficulty, type, data);
    }

    // ------------------------------------------------------------------ the Skar King's crystal

    public boolean isControlled() {
        return entityData.get(DATA_CONTROLLED);
    }

    public boolean isChained() {
        return entityData.get(DATA_CHAINED);
    }

    public boolean isControlledBy(SkarKingEntity skar) {
        return isControlled() && skar.getUUID().equals(controllerId);
    }

    public void setController(SkarKingEntity skar) {
        controllerId = skar.getUUID();
        entityData.set(DATA_CONTROLLED, true);
        refreshBossColor();
    }

    /** The crystal points: she attacks. Breaks her chains. */
    public void command(LivingEntity target) {
        entityData.set(DATA_CHAINED, false);
        if (getTarget() != target && canAttack(target)) setTarget(target);
    }

    /** Freed: she remembers who chained her. */
    public void free(@Nullable SkarKingEntity oppressor) {
        controllerId = null;
        entityData.set(DATA_CONTROLLED, false);
        entityData.set(DATA_CHAINED, false);
        setTarget(oppressor != null && oppressor.isAlive() ? oppressor : null);
        refreshBossColor();
        triggerAnim("action", "roar");
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY(0.6), getZ(), 200, getBbWidth(), getBbHeight() * 0.4, getBbWidth(), 0.2);
            for (var p : server.players()) {
                if (p.distanceToSqr(this) < 192 * 192) p.sendSystemMessage(Component.translatable("message.reikogodzilla.shimo_free").withStyle(ChatFormatting.AQUA));
            }
        }
    }

    @Override
    public boolean wantsToAttack(LivingEntity other) {
        // Free Shimo goes for the Skar King and his apes first.
        if (!isControlled() && (other instanceof SkarKingEntity || other instanceof GreatApeEntity ape && ape.isEnslaved())) return true;
        return super.wantsToAttack(other);
    }

    @Override
    public boolean isImmobile() {
        return isChained() || super.isImmobile();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(ModDamageTypes.ATOMIC_BREATH) || source.is(ModDamageTypes.INFINITE_HEAT_RAY)) amount *= 1.5f;
        boolean hurt = super.hurt(source, amount);
        if (hurt && isChained() && source.getEntity() != null) entityData.set(DATA_CHAINED, false);
        return hurt;
    }

    @Override
    public float freezeMultiplier() {
        return 0f;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        if (isControlled() && tickCount % 20 == 0) {
            SkarKingEntity skar = controllerId != null && server.getEntity(controllerId) instanceof SkarKingEntity s && s.isAlive() && s.hasCrystal() ? s : null;
            if (skar == null) free(null);
        }
        if (isChained()) {
            if (tickCount % 10 == 0) {
                server.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY(0.5), getZ(), 6, getBbWidth() * 0.5, getBbHeight() * 0.3, getBbWidth() * 0.5, 0.01);
            }
            return;
        }
        // Her presence alone freezes the ground: water to ice, lava to stone.
        if (tickCount % 20 == 0 && KaijuDestruction.canGrief(server, this)) {
            Frost.freezeArea(server, blockPosition(), (int) Math.min(8, 2 + 2 * getScale()), random, false);
        }
        // Left in the Skar King's hands, she brings on a new ice age.
        if (isControlled() && getTarget() != null && tickCount % 400 == 0 && GodzillaConfig.get(GodzillaConfig.ICE_AGE, true)) {
            server.setWeatherParameters(0, 6000, true, true);
            if (KaijuDestruction.canGrief(server, this)) {
                for (int i = 0; i < 6; i++) {
                    BlockPos at = blockPosition().offset(random.nextInt(49) - 24, 0, random.nextInt(49) - 24);
                    Frost.freezeArea(server, server.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, at), 3, random, false);
                }
            }
            if (!warnedIceAge) {
                warnedIceAge = true;
                for (var p : server.players()) {
                    if (p.distanceToSqr(this) < 256 * 256) p.sendSystemMessage(Component.translatable("message.reikogodzilla.ice_age").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
                }
            }
        }
    }

    @Nullable
    @Override
    public float[] getTint() {
        return null;
    }

    @Override
    public boolean showsBossBar() {
        return !isChained() && super.showsBossBar();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (controllerId != null) tag.putUUID("Controller", controllerId);
        tag.putBoolean("Chained", isChained());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        controllerId = tag.hasUUID("Controller") ? tag.getUUID("Controller") : null;
        entityData.set(DATA_CONTROLLED, controllerId != null);
        entityData.set(DATA_CHAINED, tag.getBoolean("Chained"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SHIMO_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHIMO_ROAR.get();
    }

    private static final software.bernie.geckolib.animation.RawAnimation DORMANT = software.bernie.geckolib.animation.RawAnimation.begin().thenLoop("dormant");

    @Override
    protected software.bernie.geckolib.animation.PlayState mainAnimation(software.bernie.geckolib.animation.AnimationState<KaijuEntity> state) {
        if (isChained()) return state.setAndContinue(DORMANT);
        return super.mainAnimation(state);
    }
}
