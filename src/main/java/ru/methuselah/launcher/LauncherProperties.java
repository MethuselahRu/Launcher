package ru.methuselah.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import ru.methuselah.launcher.Data.LauncherPropertiesFields;
import ru.simsonic.rscCommonsLibrary.HashAndCipherUtilities;

public final class LauncherProperties
{
	private static final File propertiesFile = new File(RuntimeConfig.LAUNCHER_HOME, GlobalConfig.CONFIGURATION_FILE);
	public LauncherPropertiesFields data = new LauncherPropertiesFields();
	protected LauncherProperties()
	{
		try
		{
			new File(RuntimeConfig.LAUNCHER_HOME, "cl.xml").delete();
			new File(RuntimeConfig.LAUNCHER_HOME, "clientlist.xml").delete();
			new File(RuntimeConfig.LAUNCHER_HOME, "lastlogin").delete();
			new File(RuntimeConfig.LAUNCHER_HOME, "launcher.properties").delete();
		} catch(RuntimeException ex) {
		}
		reloadFromDisk();
		saveToDisk();
	}
	public void reloadFromDisk()
	{
		if(propertiesFile.isFile())
			data = HashAndCipherUtilities.loadEncryptedObject(propertiesFile, LauncherPropertiesFields.class);
		if(data == null)
			data = new LauncherPropertiesFields();
	}
	public void saveToDisk()
	{
		HashAndCipherUtilities.saveEncryptedObject(propertiesFile, data, LauncherPropertiesFields.class);
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
