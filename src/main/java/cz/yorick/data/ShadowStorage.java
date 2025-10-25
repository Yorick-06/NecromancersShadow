package cz.yorick.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Consumer;

public class ShadowStorage implements Iterable<Int2ObjectMap.Entry<ShadowData>>, TooltipAppender {
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
    public static final Codec<ShadowStorage> CODEC = buildCodec(ShadowData.CODEC);
    public static final Codec<ShadowStorage> SYNC_CODEC = buildCodec(ShadowData.SYNC_CODEC);

    private final Int2ObjectMap<ShadowData> shadows;
    public ShadowStorage(Int2ObjectMap<ShadowData> shadows) {
        this.shadows = shadows;
    }

    public static ShadowStorage empty() {
        return new ShadowStorage(new Int2ObjectArrayMap<>());
    }

    private static Codec<ShadowStorage> buildCodec(Codec<ShadowData> shadowDataCodec) {
        return Codec.unboundedMap(STRINGIFIED_INT_CODEC, shadowDataCodec).xmap(
                map -> new ShadowStorage(new Int2ObjectArrayMap<>(map)),
                storage -> storage.shadows
        );
    }

    public ShadowData getShadow(int slot) {
        return this.shadows.get(slot);
    }

    @Override
    public @NotNull Iterator<Int2ObjectMap.Entry<ShadowData>> iterator() {
        return this.shadows.int2ObjectEntrySet().iterator();
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        this.shadows.forEach((slot, shadow) -> textConsumer.accept(shadow.asText()));
    }

    //if setting a null value, do not store it
    public void setShadow(int slot, ShadowData data) {
        if(data == null) {
            this.shadows.remove(slot);
            return;
        }

        this.shadows.put(slot, data);
    }

    public int lasSlot() {
        return this.shadows.keySet().intStream().max().orElse(0);
    }

    //insert it into the first non-occupied slot
    public void addShadow(ShadowData shadowData) {
        IntSet takenSlots = this.shadows.keySet();
        int i = 0;
        while (takenSlots.contains(i)) {
            i++;
        }

        this.setShadow(i, shadowData);
    }

    public boolean isEmpty() {
        return this.shadows.isEmpty();
    }

    @Override
    public String toString() {
        String[] converted = new String[this.shadows.size()];
        int index = 0;
        for (Int2ObjectMap.Entry<ShadowData> shadowDataEntry : this.shadows.int2ObjectEntrySet()) {
            converted[index] = shadowDataEntry.getIntKey() + ":" + shadowDataEntry.getValue().simpleToString();
            index++;
        }

        return "ShadowStorage{" +
                "shadows=[" + String.join(", ", converted) +
                "]}";
    }
}
