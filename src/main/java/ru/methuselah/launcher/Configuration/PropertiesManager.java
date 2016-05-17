package ru.methuselah.launcher.Configuration;

import java.io.File;
import ru.simsonic.rscCommonsLibrary.HashAndCipherUtilities;

public final class PropertiesManager
{
	private static final File PROPERTIES_FILE = new File(RuntimeConfig.LAUNCHER_HOME, GlobalConfig.CONFIGURATION_FILE);
	private PropertiesField data = new PropertiesField();
	public PropertiesManager()
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
	public PropertiesField getData()
	{
		return data;
	}
	public void reloadFromDisk()
	{
		if(PROPERTIES_FILE.isFile())
			data = HashAndCipherUtilities.loadEncryptedObject(PROPERTIES_FILE, PropertiesField.class);
		if(data == null)
			data = new PropertiesField();
	}
	public void saveToDisk()
	{
		HashAndCipherUtilities.saveEncryptedObject(PROPERTIES_FILE, data, PropertiesField.class);
	}
}
