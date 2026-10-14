package com.gtnewhorizons.angelica.mixins.early.angelica.entity;

import com.gtnewhorizons.angelica.glsm.GLStateManager;
import com.gtnewhorizons.angelica.rendering.tesr.ModelPartBatcher;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public abstract class MixinTextureManager {

    @Shadow public abstract ITextureObject getTexture(ResourceLocation location);
    @Shadow public abstract boolean loadTexture(ResourceLocation location, ITextureObject texture);

    /**
     * While the model part batcher is capturing a renderer it knows to be safe, the texture an entity renderer
     * binds is only ever used by the batcher (which binds it itself, sorted, at flush time), so the bind is
     * made lazy: recorded for the batcher, sent to the driver only if something draws or otherwise needs it
     * (GLStateManager.glBindTexture2DLazily). Everything else binds as before and is tracked at RETURN.
     */
    @Inject(method = "bindTexture", at = @At("HEAD"), cancellable = true)
    private void angelica$lazyBind(ResourceLocation location, CallbackInfo ci) {
        if (!ModelPartBatcher.lazyBindsAllowed()) return;
        ITextureObject texture = getTexture(location);
        if (texture == null) {
            texture = new SimpleTexture(location);
            loadTexture(location, texture);
        }
        final int glId = texture.getGlTextureId();
        GLStateManager.glBindTexture2DLazily(glId);
        ModelPartBatcher.onTextureBind(location, glId);
        ci.cancel();
    }

    @Inject(method = "bindTexture", at = @At("RETURN"))
    private void angelica$trackBind(ResourceLocation location, CallbackInfo ci) {
        ModelPartBatcher.onTextureBind(location, GLStateManager.getBoundTextureForServerState());
    }
}
