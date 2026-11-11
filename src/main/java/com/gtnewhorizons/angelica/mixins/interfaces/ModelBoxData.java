package com.gtnewhorizons.angelica.mixins.interfaces;

import com.gtnewhorizons.angelica.glsm.ffp.CubeParams;

public interface ModelBoxData {

    CubeParams angelica$cubeParams();

    /** [Akira] For a flat box (which has no {@link #angelica$cubeParams}): the thin cube it can be drawn as where back faces are culled. */
    default CubeParams angelica$flatCubeParams() {
        return null;
    }
}
