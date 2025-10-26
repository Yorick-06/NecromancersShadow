package cz.yorick.data;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import cz.yorick.NecromancersShadow;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

//used for serialization, both systems for item components and data attachments are made for immutable data
public class ImmutableShadowStorage implements TooltipAppender, Iterable<Map.Entry<Integer, ShadowData>>, ShadowAccess {
    public static final String STORED_SHADOWS_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".stored_shadows";
    //map keys cannot be ints, store the int key as a string
    private static final Codec<Integer> STRINGIFIED_INT_CODEC = Codecs.NON_EMPTY_STRING.comapFlatMap(string -> {
        try {
            return DataResult.success(Integer.parseInt(string));
        } catch (NumberFormatException e) {
            return DataResult.error(() -> "Not a number: '" + string + "'");
        }
    }, String::valueOf);

    //sync codec does not send the entity nbt data, since the client does not normally need it,
    //full data are sent when opening a shadow storage gui
    public static final Codec<ImmutableShadowStorage> CODEC = buildCodec(ShadowData.CODEC);
    public static final Codec<ImmutableShadowStorage> SYNC_CODEC = buildCodec(ShadowData.SYNC_CODEC);
    private static Codec<ImmutableShadowStorage> buildCodec(Codec<ShadowData> shadowDataCodec) {
        return Codec.unboundedMap(STRINGIFIED_INT_CODEC, shadowDataCodec).xmap(
                ImmutableShadowStorage::new,
                storage -> storage.shadows
        );
    }

    private final ImmutableMap<Integer, ShadowData> shadows;
    public ImmutableShadowStorage(Map<Integer, ShadowData> shadows) {
        this.shadows = ImmutableMap.copyOf(shadows);
    }

    public MutableShadowStorage toMutable() {
        return new MutableShadowStorage(new Int2ObjectArrayMap<>(this.shadows));
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        textConsumer.accept(Text.translatable(STORED_SHADOWS_TRANSLATION_KEY).formatted(Formatting.GRAY));
        this.shadows.forEach((slot, shadow) -> textConsumer.accept(shadow.asText()));
    }


    @Override
    public @NotNull Iterator<Map.Entry<Integer, ShadowData>> iterator() {
        return this.shadows.entrySet().iterator();
    }

    @Override
    public ShadowData getShadow(int slot) {
        return this.shadows.get(slot);
    }

    @Override
    public int lastOccupiedSlot() {
        return this.shadows.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    @Override
    public boolean isEmpty() {
        return this.shadows.isEmpty();
    }

    public static ImmutableShadowStorage empty() {
        return new ImmutableShadowStorage(Map.of());
    }
}
