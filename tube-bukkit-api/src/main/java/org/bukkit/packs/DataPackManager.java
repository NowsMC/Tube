package org.bukkit.packs;

import org.bukkit.World;

import java.util.Collection;

public interface DataPackManager {

    Collection<DataPack> getDataPacks();

    Collection<DataPack> getEnabledDataPacks(World world);

    Collection<DataPack> getDisabledDataPacks(World world);
}
