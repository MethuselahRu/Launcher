package ru.methuselah.launcher;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import ru.methuselah.securitylibrary.Data.Mojang.MojangAssetIndex;
import ru.methuselah.securitylibrary.Data.Mojang.MojangAssetIndex.AssetObject;
import ru.methuselah.securitylibrary.Data.MessagesLauncher.AnswerLauncherDesign;
import ru.methuselah.launcher.Data.MojangInternalIndex;
import ru.methuselah.launcher.Data.OfflineClient;
import ru.methuselah.launcher.Data.OfflineProject;
import ru.simsonic.rscUtilityLibrary.HashAndCipherUtilities;

public class ResourceManager
{
	private static final String catalogName = "launcher-resources";
	private static final String catalogTemp = "temp";
	private static final String downloadingExtension = ".download";
	private final String resourcesHome;
	public ResourceManager(File launcherHome)
	{
		// Create resources directory
		final File resourcesHomeTry = new File(launcherHome, catalogName);
		resourcesHome = resourcesHomeTry.getPath() + File.separator;
		resourcesHomeTry.mkdirs();
	}
	public String getGlobalAssetsDir()
	{
		return resourcesHome;
	}
	public void saveDesignFile(OfflineProject project, AnswerLauncherDesign design)
	{
		final File designFile = new File(project.getProjectHome(), "design.bin");
		HashAndCipherUtilities.saveEncrypted(designFile, design, AnswerLauncherDesign.class);
	}
	public AnswerLauncherDesign loadDesignFile(OfflineProject project)
	{
		final File designFile = new File(project.getProjectHome(), "design.bin");
		return HashAndCipherUtilities.loadEncrypted(designFile, AnswerLauncherDesign.class);
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
		for(AssetObject object : mojangIndex.objects)
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
			BaseUpdater.downloadFile(source, target, new File(object.originalName).getName());
		}
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
		final Gson gson = new Gson();
		final String contents = gson.toJson(internal);
		try
		{
			final FileWriter fw = new FileWriter(result);
			fw.write(contents);
			fw.close();
		} catch(IOException ex) {
			return null;
		}
		return result;
	}
	public void updateClientFiles(OfflineClient client) throws IOException
	{
		// Создать каталог клиента
		final String clientFolder = client.getClientHome() + File.separator;
		File createFolder = new File(clientFolder);
		if(!createFolder.exists())
			createFolder.mkdir();
		// Построить списки для скачивания
		final HashMap<String, String> gameFiles = new HashMap<>();
		// Добавить скачивание native файлов
		switch(GlobalConfig.platform)
		{
			case LINUX:
			case SOLARIS:
				gameFiles.put("1710_linux_natives.zip", GlobalConfig.urlNatives);
				break;
			case WINDOWS:
				gameFiles.put("1710_windows_natives.zip", GlobalConfig.urlNatives);
				break;
			case MACOSX:
				gameFiles.put("1710_macosx_natives.zip", GlobalConfig.urlNatives);
				break;
			default:
				System.err.println("OS (" + System.getProperty("os.name") + ") is not supported.");
				return;
		}
		gameFiles.put(client.jarFile, GlobalConfig.urlArchive);
		gameFiles.put(client.contentsFile, GlobalConfig.urlArchive);
		// Скачивание файлов
		try
		{
			for(String fileName : gameFiles.keySet())
			{
				System.err.println("Requesting " + fileName);
				BaseUpdater.downloadFile(gameFiles.get(fileName) + fileName, new File(clientFolder, fileName), fileName);
				if(fileName.endsWith(".zip"))
					BaseUpdater.unZip(new File(clientFolder, fileName), true);
			}
			Launcher.showGrant("Обновление клиента завершено");
			System.err.println("Game is updated");
		} catch(PrivilegedActionException ex) {
			Launcher.showError("Не удалось обновить клиент");
			System.err.println("Update failed: " + ex.getLocalizedMessage());
		}
	}
}
