package ru.methuselah.launcher.Downloaders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import ru.methuselah.launcher.Configuration.GlobalConfig;
import ru.methuselah.launcher.Configuration.RuntimeConfig;
import ru.methuselah.launcher.Data.OfflineClient;
import ru.methuselah.launcher.Data.Platform;
import ru.methuselah.launcher.Launcher;

public class NativesManager
{
	private final ResourceManager resources;
	public NativesManager(ResourceManager resources)
	{
		this.resources = resources;
	}
	public String getClientNativesDir(OfflineClient client)
	{
		return resources.getResourcesHome()
			+ File.separator + "natives" + File.separator
			+ client.project.code + "_" + client.caption + File.separator;
	}
	public boolean isClientNativesDirExist(OfflineClient client)
	{
		return new File(getClientNativesDir(client)).isDirectory();
	}
	public List<DownloadTask> prepareClientNatives(OfflineClient client)
	{
		if(RuntimeConfig.RUNTIME_PLATFORM == Platform.UNKNOWN)
		{
			final String error = "OS (" + System.getProperty("os.name") + ") is not supported.";
			Launcher.showError(error);
			System.err.println(error);
			return null;
		}
		// Платформенно-зависимые бинарные файлы
		final String nativesURL = GlobalConfig.URL_LAUNCHER_BINS
			+ "natives/" + (client.nativesSubdir != null ? client.nativesSubdir : "1710")
			+ "/" + RuntimeConfig.RUNTIME_PLATFORM.name().toLowerCase()
			+ ".zip";
		final String nativesDIR = Launcher.getInstance().nativesMan.getClientNativesDir(client);
		final List<DownloadTask> result = new ArrayList<>();
		result.add(new DownloadTask(
			nativesURL,
			"Файлы платформы",
			new File(nativesDIR + "download.zip"),
			new File(nativesDIR)));
		return result;
	}
}
