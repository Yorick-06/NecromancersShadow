package cz.yorick.data;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;

public class MutableShadowStorage implements MutableShadowAccess {
    private final Int2ObjectMap<ShadowData> shadows;
    public MutableShadowStorage(Int2ObjectMap<ShadowData> shadows) {
        this.shadows = shadows;
    }

    @Override
    public ShadowData getShadow(int slot) {
        return this.shadows.get(slot);
    }

    @Override
    public void setShadow(int slot, ShadowData data) {
        //if setting a null value, do not store it
        if(data == null) {
            this.shadows.remove(slot);
            return;
        }

        this.shadows.put(slot, data);
    }

    @Override
    public int lastOccupiedSlot() {
        return this.shadows.keySet().intStream().max().orElse(0);
    }

    //insert it into the first non-occupied slot
    public void addShadow(ShadowData shadowData) {
        this.setShadow(firstFreeSlot(), shadowData);
    }

    public int firstFreeSlot() {
        IntSet takenSlots = this.shadows.keySet();
        int i = 0;
        while (takenSlots.contains(i)) {
            i++;
        }

        return i;
    }

    @Override
    public boolean isEmpty() {
        return this.shadows.isEmpty();
    }

    public ImmutableShadowStorage toImmutable() {
        return new ImmutableShadowStorage(this.shadows);
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
