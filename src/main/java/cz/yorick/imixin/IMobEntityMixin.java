package cz.yorick.imixin;

import cz.yorick.data.ShadowData;

public interface IMobEntityMixin {
    void necromancers_shadow$setShadow(ShadowData.Instance shadowInstance);
    ShadowData.Instance necromancers_shadow$$getShadowInstance();
}
