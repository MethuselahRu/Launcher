package ru.methuselah.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import ru.methuselah.launcher.Data.LauncherPropertiesFields;
import ru.simsonic.rscUtilityLibrary.HashAndCipherUtilities;

public final class LauncherProperties
{
	private static final File propertiesFile = new File(GlobalConfig.launcherHomeDir, GlobalConfig.propertiesFilename);
	public LauncherPropertiesFields data = new LauncherPropertiesFields();
	protected LauncherProperties()
	{
		try
		{
			new File(GlobalConfig.launcherHomeDir, "cl.xml").delete();
			new File(GlobalConfig.launcherHomeDir, "clientlist.xml").delete();
			new File(GlobalConfig.launcherHomeDir, "lastlogin").delete();
			new File(GlobalConfig.launcherHomeDir, "launcher.properties").delete();
		} catch(RuntimeException ex) {
		}
		reloadFromDisk();
		saveToDisk();
	}
	public void reloadFromDisk()
	{
		data = HashAndCipherUtilities.loadEncrypted(propertiesFile, LauncherPropertiesFields.class);
		if(data == null)
			data = new LauncherPropertiesFields();
	}
	public void saveToDisk()
	{
		HashAndCipherUtilities.saveEncrypted(propertiesFile, data, LauncherPropertiesFields.class);
	}
	public static String getFirstRunProjectCode()
	{
		try(InputStreamReader isr = new InputStreamReader(LauncherProperties.class.getResourceAsStream("/firstrun.txt"), "UTF-8"))
		{
			final char[] buffer = new char[5];
			if(isr.read(buffer) == 5)
				return new String(buffer).toUpperCase();
		} catch(NullPointerException | IOException ex) {
		}
		return "";
	}
}
