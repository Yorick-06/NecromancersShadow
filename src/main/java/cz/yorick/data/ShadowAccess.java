package cz.yorick.data;

public interface ShadowAccess {
    ShadowData getShadow(int slot);
    int lastOccupiedSlot();
    boolean isEmpty();
}
