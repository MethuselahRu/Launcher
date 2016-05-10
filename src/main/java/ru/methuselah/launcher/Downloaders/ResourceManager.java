package ru.methuselah.launcher.Downloaders;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import ru.methuselah.launcher.Configuration.GlobalConfig;
import ru.methuselah.launcher.Configuration.RuntimeConfig;
import ru.methuselah.launcher.Data.MojangInternalIndex;
import ru.methuselah.launcher.Data.OfflineClient;
import ru.methuselah.launcher.Data.OfflineProject;
import ru.methuselah.launcher.Data.Platform;
import ru.methuselah.launcher.Launcher;
import ru.methuselah.launcher.Utilities;
import ru.methuselah.securitylibrary.Data.Launcher.LauncherAnswerDesign;
import ru.methuselah.securitylibrary.Data.Mojang.MojangAssetIndex;
import ru.methuselah.securitylibrary.Data.Mojang.MojangAssetIndex.AssetObject;
import ru.simsonic.rscCommonsLibrary.HashAndCipherUtilities;

public class ResourceManager
{
	private static final String RESOURCES_FOLDER = "launcher-resources";
	private final String resourcesHome;
	public ResourceManager(File launcherHome)
	{
		// Create resources directory
		final File resourcesHomeFile = new File(launcherHome, RESOURCES_FOLDER);
		try
		{
			Files.setAttribute(resourcesHomeFile.toPath(), "dos:hidden", true);
		} catch(IOException ex) {
		}
		resourcesHome = resourcesHomeFile.getPath() + File.separator;
		resourcesHomeFile.mkdirs();
	}
	public String getGlobalAssetsDir()
	{
		return resourcesHome;
	}
	public String getNativesDirForClient(OfflineClient client)
	{
		return resourcesHome
			+ "natives" + File.separator
			+ client.project.code + "_" + client.caption + File.separator;
	}
	public void saveDesignFile(OfflineProject project, LauncherAnswerDesign design)
	{
		final File designFile = new File(project.getProjectHome(), "design.bin");
		HashAndCipherUtilities.saveEncryptedObject(designFile, design, LauncherAnswerDesign.class);
	}
	public LauncherAnswerDesign loadDesignFile(OfflineProject project)
	{
		final File designFile = new File(project.getProjectHome(), "design.bin");
		return HashAndCipherUtilities.loadEncryptedObject(designFile, LauncherAnswerDesign.class);
	}
	public void checkClientAssets(OfflineClient client)
	{
		Launcher.showGrant("Проверка необходимых ресурсов...");
		// Получить официальный индекс для базовой версии
		String version = client.baseVersion;
		if(version == null || "".equals(version))
			version = "legacy";
		String json = Utilities.executePost("https://s3.amazonaws.com/Minecraft.Download/indexes/" + version + ".json", null);
		if(json == null || "".equals(json))
			json = Utilities.executePost("https://s3.amazonaws.com/Minecraft.Download/indexes/legacy.json", null);
		final MojangAssetIndex mojangIndex = parseMojangIndex(json);
		// Скачать дополнительный индекс для указанной сборки
		final MojangAssetIndex voxileIndex = null; // Launcher.getInstance().voxileConnection.onLauncherClientAssets(client.project.code, client.caption);
		// Слить их вместе
		if(voxileIndex != null)
		{
			final HashMap<String, AssetObject> finalIndex = new HashMap<>();
			for(AssetObject asset : mojangIndex.objects)
				finalIndex.put(asset.originalName, asset);
			for(AssetObject asset : voxileIndex.objects)
				finalIndex.put(asset.originalName, asset);
			mojangIndex.objects = finalIndex.values().toArray(new AssetObject[finalIndex.size()]);
		}
		// Скачать все необходимые файлы
		final ThreadGroup group = new ThreadGroup("assets");
		final HashSet<Thread> threads = new HashSet<>();
		for(final AssetObject object : mojangIndex.objects)
		{
			final String objectHashPath = object.hash.substring(0, 2) + "/" + object.hash;
			final File target = new File(resourcesHome + File.separator + (mojangIndex.virtual
				? "virtual" + File.separator + object.originalName
				: "objects" + File.separator + objectHashPath));
			// Пропуск имеющихся файлов нужного размера
			if(target.isFile() && target.length() == object.size)
				continue;
			final String source = (object.sourceUrl == null || "".equals(object.sourceUrl)
				? "http://resources.download.minecraft.net/" + objectHashPath
				: object.sourceUrl);
			// Создаю новый параллельный поток для загрузки
			final Thread backgroundThread = new Thread(group, new Runnable()
			{
				@Override
				public void run()
				{
					BaseUpdater.downloadFile(source, target, new File(object.originalName).getName());
				}
			});
			// Ожидаю, чтобы было не более N активных потоков и запускаю
			while(group.activeCount() > GlobalConfig.MAX_DLOAD_THREADS) {}
			threads.add(backgroundThread);
			backgroundThread.start();
		}
		for(Thread backgroundThread : threads)
			try
			{
				backgroundThread.join();
			} catch(InterruptedException ex) {
			}
		threads.clear();
		group.destroy();
		Launcher.showGrant("Загрузка Assets завершена");
		// Сохранить новый временный индексный файл
		client.assetIndexFile = saveClientIndex(client, mojangIndex);
	}
	public static MojangAssetIndex parseMojangIndex(String json)
	{
		if(json == null || "".equals(json))
			return new MojangAssetIndex();
		final Gson gson = new Gson();
		try
		{
			final Type type = new TypeToken<MojangInternalIndex>(){}.getType();
			final MojangInternalIndex internal = gson.fromJson(json, type);
			final ArrayList<AssetObject> list = new ArrayList<>();
			for(Map.Entry<String, AssetObject> objectEntry : internal.objects.entrySet())
			{
				final AssetObject object = objectEntry.getValue();
				object.originalName = objectEntry.getKey();
				list.add(object);
			}
			final MojangAssetIndex result = new MojangAssetIndex();
			result.virtual = internal.virtual;
			result.objects = list.toArray(new AssetObject[list.size()]);
			return result;
		} catch(JsonSyntaxException ex) {
		}
		return null;
	}
	public File saveClientIndex(OfflineClient client, MojangAssetIndex index)
	{
		final File result = new File(resourcesHome + "indexes", client.project.code + "." + client.caption + ".json");
		result.getParentFile().mkdirs();
		final MojangInternalIndex internal = new MojangInternalIndex();
		internal.virtual = index.virtual;
		for(AssetObject object : index.objects)
			internal.objects.put(object.originalName, object);
		final String contents = new Gson().toJson(internal);
		try(FileWriter fw = new FileWriter(result))
		{
			fw.write(contents);
		} catch(IOException ex) {
			return null;
		}
		return result;
	}
	public boolean areClientNativesExist(OfflineClient client)
	{
		return new File(getNativesDirForClient(client)).isDirectory();
	}
	public void updateClientFiles(OfflineClient client) throws IOException
	{
		// Создать каталог клиента
		final String clientFolder = client.getClientHome() + File.separator;
		final File createFolder = new File(clientFolder);
		if(!createFolder.exists())
			createFolder.mkdirs();
		// Формирование списка файлов для скачивания
		final HashSet<DownloadTask> downloads = new HashSet<>();
		// Платформенно-зависимые бинарные файлы
		final String nativesURL = GlobalConfig.URL_LAUNCHER_BINS
			+ "natives/" + (client.nativesSubdir != null ? client.nativesSubdir : "1710")
			+ "/" + RuntimeConfig.RUNTIME_PLATFORM.name().toLowerCase()
			+ ".zip";
		final String nativesDIR = getNativesDirForClient(client);
		if(RuntimeConfig.RUNTIME_PLATFORM != Platform.UNKNOWN)
		{
			downloads.add(new DownloadTask(
				nativesURL,
				"Файлы платформы",
				new File(nativesDIR + "download.zip"),
				new File(nativesDIR)));
		} else {
			final String error = "OS (" + System.getProperty("os.name") + ") is not supported.";
			Launcher.showError(error);
			System.err.println(error);
			return;
		}
		// Файлы клиента
		downloads.add(new DownloadTask(
			GlobalConfig.URL_LAUNCHER_BINS + "clients/" + client.jarFile,
			client.jarFile,
			new File(clientFolder, client.jarFile)));
		downloads.add(new DownloadTask(
			GlobalConfig.URL_LAUNCHER_BINS + "clients/" + client.contentsFile,
			client.contentsFile,
			new File(clientFolder, client.contentsFile),
			new File(clientFolder)));
		// Начало всех загрузок
		for(DownloadTask task : downloads)
		{
			System.out.println("Загрузка файла " + task.downloadFrom);
			BaseUpdater.downloadTask(task);
		}
		Launcher.showGrant("Обновление клиента завершено");
		System.out.println("Игра успешно обновлена");
	}
}
