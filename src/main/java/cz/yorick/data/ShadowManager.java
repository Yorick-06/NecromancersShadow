package cz.yorick.data;

public interface ShadowManager {
    ShadowData getShadow(int slot);
    void setShadow(int slot, ShadowData shadowData);
    default void swapShadows(int from, int to) {
        //swap the actual data - nulls handled by the storage
        ShadowData toShadowData = this.getShadow(to);
        this.setShadow(to, this.getShadow(from));
        setShadow(from, toShadowData);
    }
}
