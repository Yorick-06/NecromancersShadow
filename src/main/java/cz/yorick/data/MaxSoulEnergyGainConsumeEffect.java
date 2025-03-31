package cz.yorick.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public record MaxSoulEnergyGainConsumeEffect(int soulEnergy) implements ConsumeEffect {
    private static final MapCodec<MaxSoulEnergyGainConsumeEffect> CODEC = Codec.INT.fieldOf("energy").xmap(MaxSoulEnergyGainConsumeEffect::new, MaxSoulEnergyGainConsumeEffect::soulEnergy);
    public static final Type<MaxSoulEnergyGainConsumeEffect> TYPE = new Type<>(CODEC, PacketCodecs.registryCodec(CODEC.codec()));

    @Override
    public Type<MaxSoulEnergyGainConsumeEffect> getType() {
        return TYPE;
    }

    @Override
    public boolean onConsume(World world, ItemStack stack, LivingEntity user) {
        if(user instanceof ServerPlayerEntity player) {
            NecromancerData.setMaxEnergy(player, NecromancerData.getMaxEnergy(player) + this.soulEnergy);
            return true;
        }
        return false;
    }
}
