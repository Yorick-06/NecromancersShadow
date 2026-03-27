package cz.yorick.screen.widget;

import cz.yorick.data.ShadowAccess;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;

//IMPORTANT use setPosition(width, height, x, y) to trigger recalculation of elements - the method is private
public class ShadowAccessWidget extends ContainerObjectSelectionList<ShadowAccessWidget.Entry> {
    private static final int ROW_SIZE = 8;
    private final BiConsumer<MouseButtonEvent, SoulSlotWidget> clickedCallback;
    public ShadowAccessWidget(ShadowAccess access, BiConsumer<MouseButtonEvent, SoulSlotWidget> clickedCallback) {
        //width, height and y is set in the screen, item height is 18
        super(Minecraft.getInstance(), 0, 0, 0, 18);
        this.clickedCallback = clickedCallback;
        refreshEntries(access);
    }

    public SoulSlotWidget getWidgetFor(int slot) {
        int row = slot/ROW_SIZE;
        int rowIndex = slot - (row * ROW_SIZE);
        return this.children().get(row).children().get(rowIndex);
    }

    public void refreshEntries(ShadowAccess access) {
        this.clearEntries();
        //leave at least one slot extra
        int rows = ((access.lastOccupiedSlot() + 1)/ROW_SIZE) + 1;
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            cz.yorick.screen.widget.ShadowAccessWidget.Entry entry = new cz.yorick.screen.widget.ShadowAccessWidget.Entry();
            for (int elementIndex = 0; elementIndex < ROW_SIZE; elementIndex++) {
                int id = (rowIndex * ROW_SIZE) + elementIndex;
                SoulSlotWidget widget = new SoulSlotWidget(id);
                widget.setShadowData(access.getShadow(id));
                entry.slots.add(widget);
            }
            this.addEntry(entry);
        }

        //scroll might be out of bounds
        refreshScrollAmount();
    }

    //row width is 220 by default, spent like an hour figuring out why everything is rendering weird
    @Override
    public int getRowWidth() {
        return ROW_SIZE * 18;
    }

    //-4 removes padding, extra -1 offsets it in combination with the hack in the entry
    @Override
    protected int contentHeight() {
        return super.contentHeight() - 5;
    }

    public class Entry extends ContainerObjectSelectionList.Entry<cz.yorick.screen.widget.ShadowAccessWidget.Entry> {
        private final List<SoulSlotWidget> slots = new ArrayList<>();

        @Override
        public List<SoulSlotWidget> narratables() {
            return this.slots;
        }

        @Override
        public List<SoulSlotWidget> children() {
            return this.slots;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            for (AbstractWidget widget : this.slots) {
                widget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
            }
        }

        @Override
        public void setX(int x) {
            super.setX(x);
            int xOffset = 0;
            for (AbstractWidget widget : this.slots) {
                widget.setX(x + xOffset);
                xOffset += widget.getWidth();
            }
        }

        @Override
        public void setY(int y) {
            //a weird hack since otherwise there is a weird gap at the top, i tried everything and have no idea how to fix it :(
            int actualY = y - 3;
            super.setY(actualY);
            this.slots.forEach(widget -> widget.setY(actualY));
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            for (SoulSlotWidget child : children()) {
                if(child.isMouseOver(click.x(), click.y())) {
                    ShadowAccessWidget.this.clickedCallback.accept(click, child);
                    return true;
                }
            }
            return false;
        }
    }
}
