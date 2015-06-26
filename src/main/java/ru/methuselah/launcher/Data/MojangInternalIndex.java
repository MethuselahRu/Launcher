package ru.methuselah.launcher.Data;

import java.util.LinkedHashMap;
import ru.methuselah.securitylibrary.Data.Mojang.MojangAssetIndex.AssetObject;

public class MojangInternalIndex
{
	public LinkedHashMap<String, AssetObject> objects = new LinkedHashMap<>();
	public boolean virtual;
}
