package ru.fourgotten.VoxileLauncher;

import ru.fourgotten.VoxileSecurity.Data.RunType;
import ru.fourgotten.VoxileSecurity.Data.Platform;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.Scanner;
import ru.fourgotten.VoxileSecurity.HashAndCipherUtilities;
import sun.misc.BASE64Decoder;

public final class GlobalConfig
{
	public static final String versionStringPrefix = "v2.20.3b";
	public static final String launcherCaption     = "VOXILE";
	public static final String urlSiteHome1        = "https://vk.com/VoxileRu";
	public static final String urlSiteHome2        = "http://voxile.ru/forum/";
	public static final String urlSiteVote1        = "http://mctop.su/server/966";
	public static final String urlSiteVote2        = "http://topcraft.ru/servers/4311";
	public static final String urlSiteVote3        = "http://mc-servera.ru/37591";
	public static final String urlSiteVote4        = "http://monitoringminecraft.ru/server/71036";
	public static final String urlSiteDonate       = "http://voxile.ru/yandex-money.php?uuid=";
	public static final String urlScripts          = "https://auth.methuselah.ru/";
	public static final String urlArchive          = "https://data.methuselah.ru/clients/";
	public static final String urlNatives          = "https://data.methuselah.ru/clients/";
	public static final String executableName      = "Launcher";
	public static final String propertiesFilename  = "launcher-properties.bin";
	public static final boolean  separateGameProcess = true;
	public static final File     launcherHomeDir = getWorkingDirectory();
	public static final boolean  bUnderIDE;
	public static final String   devHashIDE;
	public static final String[] actualJavaVersions = { "1.7.0", "1.8.0" };
	public static final Platform platform = getPlatform();
	public static final String   runPath = getRunPath();
	public static final RunType  runType = getRunType();
	public static final String   hardwareSN = getHardwareSerialNumber();
	private static final String  clientFolder = ".voxile";
	private static final boolean ALLOW_IDE_DETECTING = true;
	static
	{
		final String runtimeArguments = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString();
		bUnderIDE = ALLOW_IDE_DETECTING && (getRunType() == RunType.CLASSES) && (runtimeArguments.contains("-Xrunjdwp:transport="));
		String decodeBase64ToHex = "";
		try
		{
			// Have Launcher been started under IDE?
			decodeBase64ToHex = HashAndCipherUtilities.binToHex(
				new String(new BASE64Decoder().decodeBuffer("SGF2ZSBMYXVuY2hlciBiZWVuIHN0YXJ0ZWQgdW5kZXIgSURFPw==")).getBytes());
		} catch(IOException ex) {
		}
		devHashIDE = decodeBase64ToHex;
	}
	public static final String versionString = versionStringPrefix + (bUnderIDE ? " (IDE)" : "");
	private static Platform getPlatform()
	{
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("win"))
			return Platform.WINDOWS;
		if(osName.contains("mac"))
			return Platform.MACOSX;
		if(osName.contains("solaris"))
			return Platform.SOLARIS;
		if(osName.contains("sunos"))
			return Platform.SOLARIS;
		if(osName.contains("linux"))
			return Platform.LINUX;
		if(osName.contains("unix"))
			return Platform.LINUX;
		return Platform.UNKNOWN;
	}
	private static String getRunPath()
	{
		try
		{
			return Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch(URISyntaxException ex) {
			System.err.println(ex);
		}
		return "";
	}
	private static RunType getRunType()
	{
		try
		{
			if(runPath.toLowerCase().endsWith(".exe"))
				return RunType.EXE;
			if(runPath.toLowerCase().endsWith(".jar"))
				return RunType.JAR;
		} catch(NullPointerException ex) {
		}
		return RunType.CLASSES;
	}
	private static File getWorkingDirectory()
	{
		// Если в текущем каталоге существует файл свойств лаунчера, то это портируемая версия.
		final File userWorkingDir = new File(System.getProperty("user.dir", "."));
		if(new File(userWorkingDir, propertiesFilename).isFile())
			return userWorkingDir;
		// Значит рабочим каталогом должен стать определённый в папке пользователя
		final String userHomeDir = System.getProperty("user.home", ".");
		final File workingDirectory;
		switch(getPlatform())
		{
			case LINUX:
			case SOLARIS:
				workingDirectory = new File(userHomeDir, clientFolder);
				break;
			case WINDOWS:
				final String applicationData = System.getenv("APPDATA");
				workingDirectory = new File((applicationData != null) ? applicationData : userHomeDir, clientFolder);
				break;
			case MACOSX:
				workingDirectory = new File(userHomeDir, "Library/Application Support/" + clientFolder);
				break;
			default:
				workingDirectory = new File(userHomeDir, clientFolder);
				break;
		}
		if(!workingDirectory.isDirectory() && !workingDirectory.mkdirs())
			throw new RuntimeException("The working directory could not be created: " + workingDirectory);
		return workingDirectory;
	}
	private static String getHardwareSerialNumber()
	{
		final String ParameterName_Windows = "SMBIOSBIOSVersion";
		String serialNumber = "unknown";
		final Runtime runtime = Runtime.getRuntime();
		Process process;
		try
		{
			switch(platform)
			{
				case LINUX:
				case SOLARIS:
					process = runtime.exec(new String[] { "dmidecode", "-t", "system" });
					break;
				case WINDOWS:
					process = runtime.exec(new String[] { "wmic", "BIOS", "get", ParameterName_Windows });
					break;
				case MACOSX:
					process = runtime.exec(new String[] { "/usr/sbin/system_profiler", "SPHardwareDataType" });
					break;
				default:
					return serialNumber;
			}
		} catch(IOException ex) {
			return serialNumber;
		}
		try
		{
			process.getOutputStream().close();
		} catch(IOException e) {
			return serialNumber;
		}
		final InputStream is = process.getInputStream();
		try
		{
			switch(platform)
			{
				case WINDOWS:
					final Scanner sc = new Scanner(is);
					while(sc.hasNext())
						if(ParameterName_Windows.equals(sc.next()))
						{
							serialNumber = sc.next().trim();
							break;
						}
					break;
				case LINUX:
				case SOLARIS:
				case MACOSX:
					final BufferedReader br = new BufferedReader(new InputStreamReader(is));
					final String marker = "Serial Number:";
					String line;
					while((line = br.readLine()) != null)
						if(line.contains(marker))
						{
							serialNumber = line.split(marker)[1].trim();
							break;
						}
					break;
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		} finally {
			try
			{
				is.close();
			} catch(IOException e) {
				return serialNumber;
			}
		}
		return serialNumber;
	}
	private static final String[] splashes = new String[]
	{
		"а давай снимем видео и выложим на Ютуб?!",
		"we are the best of the best of the best!",
		"простите, я опять это сломал. Зато печеньки есть.",
		"позадирали свои Приоры, под ними ходить можно!",
		"модер, модер, парень работящий...",
		"пвп или убежал?",
		"сколько стоит админка?",
		"да, именно, это случайные сплеши.",
		"почему в этом компьютере так пыльно?",
		"нет, я не стал работать быстрее.",
		"не читай меня, слышь!",
		"админ, запривать мне остров!",
		"почему не работает /dupe ???",
	};
	public static String createMainFrameCaption(boolean includeSplash)
	{
		String result = launcherCaption;
		if(includeSplash)
		{
			Random rnd = new Random();
			result += " — " + splashes[rnd.nextInt(splashes.length)];
		}
		return result;
	}
}
