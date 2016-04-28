package ru.methuselah.launcher.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Scanner;
import ru.methuselah.launcher.Data.Platform;
import ru.methuselah.launcher.Data.RunType;
import ru.methuselah.launcher.Launcher;
import ru.simsonic.rscCommonsLibrary.HashAndCipherUtilities;
import sun.misc.BASE64Decoder;

public final class RuntimeConfig
{
	public static final Platform RUNTIME_PLATFORM    = getPlatform();
	public static final String   RUNTIME_PATH        = getRuntimePath();
	public static final RunType  RUNTIME_PACKAGE     = getRuntimePackage();
	public static final boolean  UNDER_IDE_DEBUGGING = isUnderIDE();
	public static final String   DEVELOPER_IDE_HASH  = getSecretHashForIDE();
	public static final File     LAUNCHER_HOME       = getWorkingDirectory();
	public static final String   HARDWARE_SERIAL     = getHardwareSerialNumber();
	private static Platform getPlatform()
	{
		final String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("win"))
			return Platform.WINDOWS;
		if(osName.contains("mac"))
			return Platform.MACOSX;
		if(osName.contains("solaris"))
			return Platform.LINUX;
		if(osName.contains("sunos"))
			return Platform.LINUX;
		if(osName.contains("linux"))
			return Platform.LINUX;
		if(osName.contains("unix"))
			return Platform.LINUX;
		return Platform.UNKNOWN;
	}
	private static String getRuntimePath()
	{
		try
		{
			return Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch(URISyntaxException ex) {
			System.err.println(ex);
		}
		return "";
	}
	private static RunType getRuntimePackage()
	{
		try
		{
			if(RUNTIME_PATH.toLowerCase().endsWith(".exe"))
				return RunType.EXE;
			if(RUNTIME_PATH.toLowerCase().endsWith(".jar"))
				return RunType.JAR;
		} catch(NullPointerException ex) {
		}
		return RunType.CLASSES;
	}
	private static File getWorkingDirectory()
	{
		// Если в текущем каталоге существует файл свойств лаунчера, то это портируемая версия
		final File processWorkingDir = new File(System.getProperty("user.dir"));
		if(new File(processWorkingDir, GlobalConfig.CONFIGURATION_FILE).isFile())
			return processWorkingDir;
		// Значит рабочим каталогом должен стать определённый в папке пользователя
		final String userHomeDir = System.getProperty("user.home", ".");
		final File workingDirectory;
		switch(getPlatform())
		{
			case LINUX:
				workingDirectory = new File(userHomeDir, GlobalConfig.HOME_SUBDIRECTORY);
				break;
			case WINDOWS:
				final String applicationData = System.getenv("APPDATA");
				workingDirectory = new File((applicationData != null
					? applicationData
					: userHomeDir), GlobalConfig.HOME_SUBDIRECTORY);
				break;
			case MACOSX:
				workingDirectory = new File(userHomeDir, "Library/Application Support/" + GlobalConfig.HOME_SUBDIRECTORY);
				break;
			default:
				workingDirectory = new File(userHomeDir, GlobalConfig.HOME_SUBDIRECTORY);
				break;
		}
		if(!workingDirectory.isDirectory() && !workingDirectory.mkdirs())
		{
			final String error = "The working directory could not be created: " + workingDirectory;
			System.err.println(error);
			throw new RuntimeException(error);
		}
		return workingDirectory;
	}
	private static boolean isUnderIDE()
	{
		final String args = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString();
		return GlobalConfig.ALLOW_IDE_DETECTING && RUNTIME_PACKAGE == RunType.CLASSES && args.contains("-Xrunjdwp:transport=");
	}
	private static String getSecretHashForIDE()
	{
		try
		{
			// Have Launcher been started under IDE?
			final byte[] decodeBuffer = new BASE64Decoder().decodeBuffer("SGF2ZSBMYXVuY2hlciBiZWVuIHN0YXJ0ZWQgdW5kZXIgSURFPw==");
			return HashAndCipherUtilities.binToHex(new String(decodeBuffer).getBytes());
		} catch(IOException ex) {
		}
		return "";
	}
	private static String getHardwareSerialNumber()
	{
		try
		{
			boolean isLinux = false;
			switch(RUNTIME_PLATFORM)
			{
				case WINDOWS:
					try
					{
						final String parameterName = "SMBIOSBIOSVersion";
						final Process process = Runtime.getRuntime().exec(new String[] { "wmic", "BIOS", "get", parameterName });
						process.getOutputStream().close();
						try(final InputStream is = process.getInputStream())
						{
							final Scanner sc = new Scanner(is);
							while(sc.hasNext())
								if(parameterName.equals(sc.next()))
									return sc.next().trim();
						}
					} catch(IOException ex) {
						throw ex;
					}
					break;
				case LINUX:
					isLinux = true;
				case MACOSX:
					try
					{
						final String[] args = isLinux
							? new String[] { "dmidecode", "-t", "system" }
							: new String[] { "/usr/sbin/system_profiler", "SPHardwareDataType" };
						final Process process = Runtime.getRuntime().exec(args);
						process.getOutputStream().close();
						try(final InputStream is = process.getInputStream())
						{
							final BufferedReader br = new BufferedReader(new InputStreamReader(is));
							final String marker = "Serial Number:";
							String line;
							while((line = br.readLine()) != null)
								if(line.contains(marker))
									return line.split(marker)[1].trim();
						}
					} catch(IOException ex) {
						throw ex;
					}
					break;
				default:
					break;
			}
		} catch(IOException ex) {
			System.err.println(ex);
		}
		return "unknown";
	}
}
