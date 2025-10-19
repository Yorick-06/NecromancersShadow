package cz.yorick.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.NecromancersShadow;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class NecromancerData {
    public static final String SOUL_ENERGY_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".soul_energy";
    private static final MapCodec<Pair<Double, Integer>> ENERGY_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.fieldOf("soulEnergy").forGetter(Pair::getFirst),
            Codecs.NON_NEGATIVE_INT.fieldOf("maxSoulEnergy").forGetter(Pair::getSecond)
    ).apply(instance, Pair::new));

    public static Codec<NecromancerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ShadowData.CODEC.listOf().fieldOf("shadows").forGetter(data -> data.storedShadows),
            ENERGY_CODEC.forGetter(data -> Pair.of(data.soulEnergy, data.maxSoulEnergy))
    ).apply(instance, NecromancerData::new));

    public static PacketCodec<RegistryByteBuf, NecromancerData> PACKET_CODEC = PacketCodecs.registryCodec(
            RecordCodecBuilder.create(instance -> instance.group(
                ShadowData.SYNC_CODEC.listOf().fieldOf("shadows").forGetter(data -> data.storedShadows),
                ENERGY_CODEC.forGetter(data -> Pair.of(data.soulEnergy, data.maxSoulEnergy))
            ).apply(instance, NecromancerData::new))
    );

    private final List<ShadowData> storedShadows;
    private double soulEnergy;
    private int maxSoulEnergy;
    private final ConcurrentHashMap<ShadowData, MobEntity> spawnedShadows;
    private NecromancerData(List<ShadowData> storedShadows, Pair<Double, Integer> soulEnergy) {
        this(new ArrayList<>(storedShadows), soulEnergy.getFirst(), soulEnergy.getSecond(), new ConcurrentHashMap<>());
    }

    private NecromancerData(List<ShadowData> storedShadows, double soulEnergy, int maxSoulEnergy, ConcurrentHashMap<ShadowData, MobEntity> spawnedShadows) {
        this.storedShadows = storedShadows;
        this.soulEnergy = soulEnergy;
        this.maxSoulEnergy = maxSoulEnergy;
        this.spawnedShadows = spawnedShadows;
    }

    public NecromancerData copy() {
        return new NecromancerData(this.storedShadows, this.soulEnergy, this.maxSoulEnergy, this.spawnedShadows);
    }

    private void toggleShadowInternal(ServerPlayerEntity player, ShadowData shadow) {
        if(!this.spawnedShadows.containsKey(shadow)) {
            spawnShadow(player, shadow);
        } else {
            despawnShadow(player, shadow);
        }
    }

    private boolean toggleShadowsInternal(ServerPlayerEntity player) {
        if(!this.spawnedShadows.isEmpty()) {
            despawnShadowsInternal(player.isCreative());
            return true;
        } else {
            return spawnShadowsInternal(player);
        }
    }

    private boolean spawnShadowsInternal(ServerPlayerEntity player) {
        for (ShadowData shadow : this.storedShadows) {
            if(!spawnShadowInternal(player, shadow)) {
                break;
            }
        }

        return !this.spawnedShadows.isEmpty();
    }

    private boolean spawnShadowInternal(ServerPlayerEntity player, ShadowData shadow) {
        //creative players can spawn shadows for free
        if(!player.isCreative()) {
            if (shadow.getEnergyCost() > this.soulEnergy) {
                return false;
            }
            this.soulEnergy -= shadow.getEnergyCost();
        }

        BlockPos spawnPos = player.getBlockPos().add(-2 + player.getRandom().nextInt(5), 1, -2 + player.getRandom().nextInt(5));
        shadow.getEntityType().spawn(player.getEntityWorld(), spawned -> {
            if(spawned instanceof MobEntity mobEntity) {
                shadow.onSpawn(mobEntity, player);
                this.spawnedShadows.put(shadow, mobEntity);
            }
        }, spawnPos, SpawnReason.TRIGGERED, false, false);
        return true;
    }

    private void despawnShadowsInternal(boolean infiniteResources) {
        this.spawnedShadows.forEach((data, entity) -> despawnShadowInternal(infiniteResources, data));
        this.spawnedShadows.clear();
    }

    private void despawnShadowInternal(boolean infiniteResources, ShadowData data) {
        MobEntity entity = this.spawnedShadows.get(data);
        if(entity == null) {
            return;
        }

        //if the shadow died, do not return the soul energy
        if(entity.isAlive() && !infiniteResources) {
            this.soulEnergy = Math.clamp(this.soulEnergy + data.getEnergyCost(), 0, this.maxSoulEnergy);
        }

        data.updateFrom(entity);
        this.spawnedShadows.remove(data);
        entity.remove(Entity.RemovalReason.DISCARDED);
    }

    private void releaseShadow(boolean infiniteResources, ShadowData shadow) {
        despawnShadowInternal(infiniteResources, shadow);
        this.storedShadows.remove(shadow);
    }

    private List<ShadowData> releaseShadows(boolean infiniteResources) {
        despawnShadowsInternal(infiniteResources);
        List<ShadowData> allShadows = List.copyOf(this.storedShadows);
        this.storedShadows.clear();
        return allShadows;
    }

    //used in mob-conversion cases (zombie -> drowned, hoglin -> zoglin) to keep the mob as a shadow
    private void convertShadow(ShadowData shadow, ServerPlayerEntity player, MobEntity converted) {
        shadow.onSpawn(converted, player);
        this.spawnedShadows.put(shadow, converted);
    }

    public static void toggleShadow(ServerPlayerEntity player, ShadowData shadow) {
        NecromancyAttachments.modifyNecromancerData(player, data -> data.toggleShadowInternal(player, shadow));
    }

    public static boolean toggleShadows(ServerPlayerEntity player) {
        return NecromancyAttachments.modifyNecromancerDataWithResult(player, data -> data.toggleShadowsInternal(player));
    }

    public static void spawnShadow(ServerPlayerEntity player, ShadowData shadow) {
        NecromancyAttachments.getNecromancerData(player).spawnShadowInternal(player, shadow);
    }

    public static void despawnShadow(ServerPlayerEntity player, ShadowData shadow) {
        NecromancyAttachments.getNecromancerData(player).despawnShadowInternal(player.isCreative(), shadow);
    }

    public static void despawnShadows(ServerPlayerEntity player) {
        NecromancyAttachments.modifyNecromancerData(player, data -> data.despawnShadowsInternal(player.isCreative()));
    }

    public static void addShadow(ServerPlayerEntity player, ShadowData shadow) {
        NecromancyAttachments.modifyNecromancerData(player, data -> data.storedShadows.add(shadow));
    }

    public static void addShadows(ServerPlayerEntity player, List<ShadowData> shadows) {
        NecromancyAttachments.modifyNecromancerData(player, data -> data.storedShadows.addAll(shadows));
    }

    public static void releaseShadow(ServerPlayerEntity player, ShadowData shadow) {
        NecromancyAttachments.modifyNecromancerData(player, data -> {
            data.despawnShadowInternal(player.isCreative(), shadow);
            data.storedShadows.remove(shadow);
        });
    }

    public static List<ShadowData> releaseShadows(ServerPlayerEntity player) {
        return NecromancyAttachments.modifyNecromancerDataWithResult(player, data -> data.releaseShadows(player.isCreative()));
    }

    public static ShadowData releaseFirstShadow(ServerPlayerEntity player) {
        return NecromancyAttachments.modifyNecromancerDataWithResult(player, data -> {
            if(data.storedShadows.isEmpty()) {
                return null;
            }
            ShadowData firstShadow = data.storedShadows.removeFirst();
            data.releaseShadow(player.isCreative(), firstShadow);
            return firstShadow;
        });
    }

    public static int storedAmount(ServerPlayerEntity player) {
        return NecromancyAttachments.getNecromancerData(player).storedShadows.size();
    }

    public static void convertShadow(ShadowData.Instance previous, MobEntity converted) {
        NecromancyAttachments.modifyNecromancerData(previous.owner(), data -> data.convertShadow(previous.shadow(), previous.owner(), converted));
    }

    public static double getEnergy(ServerPlayerEntity player) {
        return NecromancyAttachments.getNecromancerData(player).soulEnergy;
    }

    public static void setEnergy(ServerPlayerEntity player, double amount) {
        NecromancyAttachments.modifyNecromancerData(player, data -> data.soulEnergy = Math.clamp(amount, 0, data.maxSoulEnergy));
    }

    public static int getMaxEnergy(ServerPlayerEntity player) {
        return NecromancyAttachments.getNecromancerData(player).maxSoulEnergy;
    }

    public static void setMaxEnergy(ServerPlayerEntity player, int amount) {
        NecromancyAttachments.modifyNecromancerData(player, data -> data.maxSoulEnergy = Math.max(0, amount));
    }

    public static List<ShadowData> getShadows(PlayerEntity player) {
        return List.copyOf(NecromancyAttachments.getNecromancerData(player).storedShadows);
    }

    public static void appendTotemTooltip(ComponentsAccess components, Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type) {
        NecromancersShadow.LOCAL_PLAYER.get().ifPresent(player -> {
            NecromancerData data = NecromancyAttachments.getNecromancerData(player);
            textConsumer.accept(Text.translatable(SOUL_ENERGY_TRANSLATION_KEY)
                    .append(Text.literal(NecromancersShadow.DECIMAL_FORMAT.format(data.soulEnergy)).formatted(Formatting.AQUA))
                            .append(Text.literal("/").formatted(Formatting.GRAY))
                                    .append(Text.literal(String.valueOf(data.maxSoulEnergy)).formatted(Formatting.AQUA))
            );
            ShadowData.appendShadowsTooltip(components, context, textConsumer, type, data.storedShadows);
        });
    }

    public static int getItemBarStep() {
        Optional<PlayerEntity> player = NecromancersShadow.LOCAL_PLAYER.get();
        if(player.isPresent()) {
            NecromancerData data = NecromancyAttachments.getNecromancerData(player.get());
            float percentage = (float)data.soulEnergy/(float)data.maxSoulEnergy;
            //item bar has 13 steps
            return Math.clamp(Math.round(percentage * 13), 0, 13);
        }

        return 0;
    }

    public static NecromancerData empty() {
        return new NecromancerData(List.of(), Pair.of(0D, 10));
    }
}
