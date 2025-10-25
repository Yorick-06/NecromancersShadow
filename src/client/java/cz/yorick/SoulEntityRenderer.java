package cz.yorick;

import cz.yorick.entity.SoulEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;

public class SoulEntityRenderer extends EntityRenderer<SoulEntity, EntityRenderState> {
    protected SoulEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
