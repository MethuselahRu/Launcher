package ru.methuselah.launcher.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import ru.methuselah.launcher.Configuration.RuntimeConfig;
import ru.methuselah.launcher.Launcher;
import ru.simsonic.rscCommonsLibrary.HashAndCipherUtilities;

public class Offline
{
	private final Launcher launcher;
	public Offline(Launcher launcher)
	{
		this.launcher = launcher;
	}
	public final HashSet<String>         offlineProjects = new HashSet<>();
	public final HashMap<String, String> offlineClients  = new HashMap<>();
	public static OfflineProject[] searchOfflineProjects()
	{
		final ArrayList<OfflineProject> result = new ArrayList<>();
		for(File dir : RuntimeConfig.LAUNCHER_HOME.listFiles())
			if(dir.isDirectory())
			{
				final File projectFile = new File(dir, "project.bin");
				if(projectFile.isFile())
				{
					OfflineProject loaded = HashAndCipherUtilities.loadEncryptedObject(projectFile, OfflineProject.class);
					if(loaded != null)
						result.add(loaded);
				}
			}
		return result.toArray(new OfflineProject[result.size()]);
	}
}
