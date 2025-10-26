package cz.yorick.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.data.MutableShadowAccess;
import cz.yorick.data.ShadowData;
import net.minecraft.util.dynamic.Codecs;

public record UiId(Ui ui, int slot) {
    public static final Codec<UiId> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.NON_NEGATIVE_INT.xmap(id -> Ui.values()[id], Ui::ordinal).fieldOf("ui").forGetter(UiId::ui),
            Codecs.NON_NEGATIVE_INT.fieldOf("slot").forGetter(UiId::slot)
    ).apply(instance, UiId::new));

    public enum Ui {
        PLAYER,
        ITEM;

        public <T> T choose(T playerOption, T itemOption) {
            return switch (this) {
                case PLAYER -> playerOption;
                case ITEM -> itemOption;
            };
        }
    }

    public static void swap(UiId from, UiId to, MutableShadowAccess playerStorage, MutableShadowAccess itemStorage) {
        MutableShadowAccess fromStorage = from.ui.choose(playerStorage, itemStorage);
        MutableShadowAccess toStorage = to.ui.choose(playerStorage, itemStorage);
        ShadowData toData = toStorage.getShadow(to.slot);
        toStorage.setShadow(to.slot, fromStorage.getShadow(from.slot));
        fromStorage.setShadow(from.slot, toData);
    }
}
