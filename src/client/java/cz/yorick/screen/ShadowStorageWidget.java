package cz.yorick.screen;

import cz.yorick.data.ShadowStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

import java.util.ArrayList;
import java.util.List;

//IMPORTANT use setPosition(width, height, x, y) to trigger recalculation of elements - the method is private
public class ShadowStorageWidget extends ElementListWidget<ShadowStorageWidget.Entry> {
    private static final int ROW_SIZE = 9;
    private final ShadowStorage storage;
    public ShadowStorageWidget(ShadowStorage storage) {
        //width, height and y is set in the screen, item height is 18
        super(MinecraftClient.getInstance(), 0, 0, 0, 18);
        this.storage = storage;
        refreshEntries();
    }

    public void refreshEntries() {
        this.clearEntries();
        //leave at least one slot extra
        int rows = (this.storage.lasSlot()/ROW_SIZE) + 1;
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            Entry entry = new Entry();
            for (int elementIndex = 0; elementIndex < ROW_SIZE; elementIndex++) {
                int id = (rowIndex * ROW_SIZE) + elementIndex;
                SoulSlotWidget widget = new SoulSlotWidget(id);
                widget.setShadowData(this.storage.getShadow(id));
                entry.slots.add(widget);
            }
            this.addEntry(entry);
        }
    }

    //row width is 220 by default, spent like an hour figuring out why everything is rendering weird
    @Override
    public int getRowWidth() {
        return ROW_SIZE * 18;
    }

    /*
    //removes the weird 4 pixel gap at the top, this wisdom is recycled from one of my previous projects
    @Override
    public int getRowTop(int index) {
        return super.getRowTop(index) - 4;
    }*/

    //first entry adds 2, we remove 2 (method is private)
    @Override
    public int getYOfNextEntry() {
        return super.getYOfNextEntry() + 20;
    }

    /*
    //remove the padding
    @Override
    protected int getContentsHeightWithPadding() {
        return super.getContentsHeightWithPadding() - 4;
    }*/

    public static class Entry extends ElementListWidget.Entry<Entry> {
        private final List<SoulSlotWidget> slots = new ArrayList<>();

        @Override
        public List<SoulSlotWidget> selectableChildren() {
            return this.slots;
        }

        @Override
        public List<SoulSlotWidget> children() {
            return this.slots;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            for (ClickableWidget widget : this.slots) {
                widget.render(context, mouseX, mouseY, deltaTicks);
            }
        }


        @Override
        public void setX(int x) {
            super.setX(x);
            int xOffset = 0;
            for (ClickableWidget widget : this.slots) {
                widget.setX(x + xOffset);
                xOffset += widget.getWidth();
            }
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            this.slots.forEach(widget -> widget.setY(y));
        }
    }
}
