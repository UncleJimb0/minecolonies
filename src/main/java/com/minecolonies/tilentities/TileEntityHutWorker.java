package com.minecolonies.tilentities;

import com.minecolonies.entity.EntityCitizen;

public class TileEntityHutWorker extends TileEntityHut
{
    public boolean isProperWorker(EntityCitizen entityCitizen)
    {
        return entityCitizen.level.getSexInt() != 1 && !this.isHasWorker();
    }
}
