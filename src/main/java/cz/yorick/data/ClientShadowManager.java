package cz.yorick.data;

public record ClientShadowManager(ShadowStorage storage) implements ShadowManager {
    @Override
    public ShadowData getShadow(int slot) {
        return this.storage.getShadow(slot);
    }

    @Override
    public void setShadow(int slot, ShadowData shadowData) {
        this.storage.setShadow(slot, shadowData);
    }
}
