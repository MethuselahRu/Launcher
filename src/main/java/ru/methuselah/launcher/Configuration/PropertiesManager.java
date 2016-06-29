package ru.methuselah.launcher.Configuration;

import java.io.File;
import ru.simsonic.rscCommonsLibrary.HashAndCipherUtilities;

public final class PropertiesManager
{
	private static final File PROPERTIES_FILE = new File(RuntimeConfig.LAUNCHER_HOME, GlobalConfig.CONFIGURATION_FILE);
	private PropertiesField data = new PropertiesField();
	public PropertiesManager()
	{
		final String[] filesToBeDeleted = new String[]
		{
			"cl.xml", "clientlist.xml", "lastlogin", "launcher.properties",
		};
		for(String file : filesToBeDeleted)
			try
			{
				new File(RuntimeConfig.LAUNCHER_HOME, file).delete();
			} catch(RuntimeException ex) {
			}
		reloadFromDisk();
		saveToDisk();
	}
	public PropertiesField getData()
	{
		return data;
	}
	public synchronized void reloadFromDisk()
	{
		if(PROPERTIES_FILE.isFile())
			data = HashAndCipherUtilities.loadEncryptedObject(PROPERTIES_FILE, PropertiesField.class);
		if(data == null)
			data = new PropertiesField();
	}
	public synchronized void saveToDisk()
	{
		HashAndCipherUtilities.saveEncryptedObject(PROPERTIES_FILE, data, PropertiesField.class);
	}
}
