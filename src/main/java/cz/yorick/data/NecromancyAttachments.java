package cz.yorick.data;

import com.mojang.serialization.Codec;
import cz.yorick.NecromancersShadow;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Function;

public class NecromancyAttachments {
    private static final AttachmentType<Boolean> IS_SHADOW = AttachmentRegistry.create(Identifier.of(NecromancersShadow.MOD_ID, "is_shadow"),
            builder -> builder
                    .copyOnDeath()
                    .persistent(Codec.BOOL)
                    .syncWith(PacketCodecs.BOOLEAN, AttachmentSyncPredicate.all())
    );

    private static final AttachmentType<NecromancerData> NECROMANCER_DATA = AttachmentRegistry.create(Identifier.of(NecromancersShadow.MOD_ID, "necromancer_data"),
            builder -> builder
                    .copyOnDeath()
                    .initializer(NecromancerData::empty)
                    .persistent(NecromancerData.CODEC)
                    .syncWith(NecromancerData.PACKET_CODEC, AttachmentSyncPredicate.targetOnly())
    );

    public static boolean isMarkedAsShadow(Entity entity) {
        return entity.hasAttached(IS_SHADOW);
    }

    public static void markAsShadow(Entity entity, boolean shadow) {
        if(shadow) {
            entity.setAttached(IS_SHADOW, true);
        } else {
            entity.removeAttached(IS_SHADOW);
        }
    }

    public static void modifyNecromancerData(ServerPlayerEntity player, Consumer<NecromancerData> action) {
        modifyNecromancerDataWithResult(player, data -> {
            action.accept(data);
            return 0;
        });
    }

    public static<T> T modifyNecromancerDataWithResult(ServerPlayerEntity player, Function<NecromancerData, T> action) {
        //using get -> set to mark the value as modified and send it to the client
        NecromancerData data = player.getAttachedOrCreate(NECROMANCER_DATA);
        T result = action.apply(data);
        player.setAttached(NECROMANCER_DATA, data);
        return result;
    }

    public static NecromancerData getNecromancerData(PlayerEntity player) {
        return player.getAttachedOrCreate(NECROMANCER_DATA);
    }

    public static void init() {
    }
}
