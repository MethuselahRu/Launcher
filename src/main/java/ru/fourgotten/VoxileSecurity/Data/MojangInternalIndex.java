package ru.fourgotten.VoxileSecurity.Data;
import java.util.LinkedHashMap;
import ru.fourgotten.VoxileSecurity.Data.GameFiles.MojangAssetIndex.AssetObject;

public class MojangInternalIndex
{
	public LinkedHashMap<String, AssetObject> objects = new LinkedHashMap<>();
	public boolean virtual;
}
