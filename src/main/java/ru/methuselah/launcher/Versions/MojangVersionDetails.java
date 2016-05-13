package ru.methuselah.launcher.Versions;

public class MojangVersionDetails extends MojangManifestVersion
{
	public String          minecraftArguments;
	public String          inheritsFrom;
	public MojangLibrary[] libraries;
	public String          mainClass;
	public String          assets;
	// public AssetIndexInfo  assetIndex;
	public int             minimumLauncherVersion;
	// public Map<DownloadType, DownloadInfo> downloads = Maps.newEnumMap(DownloadType.class);
	// processArguments?
}
