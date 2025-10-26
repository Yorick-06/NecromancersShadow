package cz.yorick.data;

public interface MutableShadowAccess extends ShadowAccess {
    void setShadow(int slot, ShadowData shadowData);
    default void swapShadows(int from, int to) {
        ShadowData toData = getShadow(to);
        setShadow(to, getShadow(from));
        setShadow(from, toData);
    }
}
