package com.gtnewhorizons.angelica.rendering.tesr;

import com.gtnewhorizons.angelica.glsm.ffp.CubeParams;
import com.gtnewhorizons.angelica.glsm.ffp.UnitCubeMesh;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;

public final class ModelBoxCapture {

    private static final float EPSILON = 1.0e-4f;

    private ModelBoxCapture() {}

    public static CubeParams capture(ModelBox box, TexturedQuad[] quads, ModelRenderer part, int texU, int texV, int sizeX, int sizeY, int sizeZ, float inflate) {
        return capture(quads, part.textureWidth, part.textureHeight, part.mirror, texU, texV, box.posX1, box.posY1, box.posZ1, sizeX, sizeY, sizeZ, inflate);
    }

    public static CubeParams capture(TexturedQuad[] quads, float texWidth, float texHeight, boolean mirror, int texU, int texV, float x, float y, float z, int sizeX, int sizeY, int sizeZ, float inflate) {
        if (quads == null || quads.length != UnitCubeMesh.FACE_COUNT) return null;
        final CubeParams params = CubeParams.of(texWidth, texHeight, mirror, texU, texV, x, y, z, sizeX, sizeY, sizeZ, inflate);
        if (params == null) return null;
        return matches(quads, params, FLAT_NONE, texWidth, texHeight, mirror, texU, texV, sizeX, sizeY, sizeZ) ? params : null;
    }

    private static final float FLAT_NONE = 0.0f;

    /**
     * [Akira] The same for a flat box (see {@link CubeParams#ofFlat}): the box's quads have to be vanilla's, checked
     * against where they really are (the thickness the cube is given is allowed for).
     */
    public static CubeParams captureFlat(ModelBox box, TexturedQuad[] quads, ModelRenderer part, int texU, int texV, int sizeX, int sizeY, int sizeZ, float inflate) {
        if (quads == null || quads.length != UnitCubeMesh.FACE_COUNT) return null;
        final CubeParams params = CubeParams.ofFlat(part.textureWidth, part.textureHeight, part.mirror, texU, texV, box.posX1, box.posY1, box.posZ1, sizeX, sizeY, sizeZ, inflate);
        if (params == null) return null;
        return matches(quads, params, CubeParams.FLAT_SPAN * 0.5f, part.textureWidth, part.textureHeight, part.mirror, texU, texV, sizeX, sizeY, sizeZ) ? params : null;
    }

    private static boolean matches(TexturedQuad[] quads, CubeParams params, float slack, float texWidth, float texHeight, boolean mirror, int texU, int texV, int sizeX, int sizeY, int sizeZ) {
        final float lowX = mirror ? params.minX() + params.spanX() : params.minX();
        final float spanX = mirror ? -params.spanX() : params.spanX();
        for (int face = 0; face < UnitCubeMesh.FACE_COUNT; face++) {
            final TexturedQuad quad = quads[face];
            if (quad == null || quad.vertexPositions == null || quad.vertexPositions.length != 4) return false;
            final int[] corners = UnitCubeMesh.FACES[face].corners();
            final UnitCubeMesh.Net net = UnitCubeMesh.FACES[face].net();
            for (int corner = 0; corner < 4; corner++) {
                final PositionTextureVertex vertex = quad.vertexPositions[mirror ? 3 - corner : corner];
                if (vertex == null || vertex.vector3D == null) return false;
                final float[] unit = UnitCubeMesh.CORNERS[corners[corner]];
                if (!near((float) vertex.vector3D.xCoord, lowX + unit[0] * spanX, slack) || !near((float) vertex.vector3D.yCoord, params.minY() + unit[1] * params.spanY(), slack) || !near((float) vertex.vector3D.zCoord, params.minZ() + unit[2] * params.spanZ(), slack)) {
                    return false;
                }
                final int u = texU + net.u(corner).texels(sizeX, sizeZ);
                final int v = texV + net.v(corner).texels(sizeY, sizeZ);
                if (!near(vertex.texturePositionX, u / texWidth, 0.0f) || !near(vertex.texturePositionY, v / texHeight, 0.0f)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean near(float a, float b, float slack) {
        return Math.abs(a - b) < EPSILON + slack;
    }
}
