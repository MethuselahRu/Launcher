package ru.fourgotten.VoxileSecurity.Data;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.IOException;
import ru.fourgotten.VoxileLauncher.GlobalConfig;
import ru.fourgotten.VoxileSecurity.Data.MessagesLauncher.AnswerLauncherProjects.ProjectInfo;
import ru.fourgotten.VoxileSecurity.HashAndCipherUtilities;

public class OfflineProject extends ProjectInfo implements Comparable<OfflineProject>
{
	public boolean availableRemote = false;
	public OfflineProject()
	{
	}
	public OfflineProject(ProjectInfo info)
	{
		this.code = info.code;
		this.caption = info.caption;
		this.currentOnlinePlayers = info.currentOnlinePlayers;
		this.currentMaxPlayers = info.currentMaxPlayers;
		this.averageOnlineTime = info.averageOnlineTime;
		this.rating = info.rating;
	}
	public OfflineClient[] clients;
	public File getProjectHome()
	{
		return new File(GlobalConfig.launcherHomeDir, code);
	}
	public static OfflineProject loadFromDisk(String code)
	{
		if(code != null && !"".equals(code))
		{
			final File projectFolder = new File(GlobalConfig.launcherHomeDir, code.toUpperCase());
			if(projectFolder.isDirectory())
			{
				final File projectFile = new File(projectFolder, "project.bin");
				return HashAndCipherUtilities.loadEncrypted(projectFile, OfflineProject.class);
			}
		}
		return null;
	}
	public void saveToDisk()
	{
		final File projectFolder = new File(GlobalConfig.launcherHomeDir, code);
		if(!projectFolder.isDirectory())
			projectFolder.mkdir();
		final File projectFile = new File(projectFolder, "project.bin");
		final JsonWriter writer = new JsonWriter(HashAndCipherUtilities.createCipherWriter(projectFile));
		try
		{
			new Gson().toJson(this, OfflineProject.class, writer);
			writer.flush();
			writer.close();
		} catch(IOException | JsonParseException ex) {
		}
	}
	@Override
	public int compareTo(OfflineProject target)
	{
		return Math.round(100.0f * (target.rating - this.rating)) + (availableRemote ? 0 : 1000);
	}
}
