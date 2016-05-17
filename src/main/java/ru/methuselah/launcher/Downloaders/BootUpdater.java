package ru.methuselah.launcher.Downloaders;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.methuselah.launcher.Configuration.GlobalConfig;
import ru.methuselah.launcher.Configuration.PropertiesManager;
import ru.methuselah.launcher.Configuration.RuntimeConfig;
import ru.methuselah.launcher.Data.Platform;
import ru.methuselah.launcher.Data.RunType;
import ru.methuselah.launcher.Launcher;
import ru.methuselah.launcher.Utilities;
import ru.simsonic.rscCommonsLibrary.HashAndCipherUtilities;

public final class BootUpdater extends BaseUpdater
{
	private final static String URL_LAUNCHER_DOWNLOADS = GlobalConfig.URL_LAUNCHER_BINS + "launcher/";
	private final static Logger logger = LoggerFactory.getLogger(BootUpdater.class);
	public static void checkForUpdates(PropertiesManager properties)
	{
		final String firstRunCode = getFirstRunProjectCode();
		if(!"".equals(firstRunCode))
		{
			logger.info("Установка кода начального проекта: " + firstRunCode);
			properties.reloadFromDisk();
			properties.saveToDisk();
			properties.getData().lastOpenedProject = firstRunCode;
			properties.saveToDisk();
		}
		logger.info("Поиск доступных обновлений ...");
		try
		{
			final File   runFile = new File(RuntimeConfig.RUNTIME_PATH);
			final String runHash = RuntimeConfig.UNDER_IDE_DEBUGGING
				? RuntimeConfig.DEVELOPER_IDE_HASH
				: (runFile.isFile() ? HashAndCipherUtilities.fileToMD5(runFile) : "");
			final String result = Utilities.executePost(
				GlobalConfig.URL_LAUNCHER_PHPS + "legacy/launcher.php",
				"launcherHash=" + runHash).trim();
			switch(result)
			{
				case "NO CONNECTION":
					logger.info("Нет соединения с проверяющим сайтом!");
				case "OK":
					logger.info("Обновлений не обнаружено.");
					final String nameJar    = GlobalConfig.EXECUTABLE_NAME + ".jar";
					final String nameExe    = GlobalConfig.EXECUTABLE_NAME + ".exe";
					final File   correctJar = new File(RuntimeConfig.LAUNCHER_HOME, nameJar);
					final File   correctExe = new File(RuntimeConfig.LAUNCHER_HOME, nameExe);
					if(RuntimeConfig.RUNTIME_PACKAGE == RunType.JAR && !runFile.equals(correctJar))
					{
						copyFile(runFile, correctJar);
						if(!correctExe.isFile())
							downloadFile(URL_LAUNCHER_DOWNLOADS + nameExe, correctExe, correctExe.getAbsolutePath());
					}
					if(RuntimeConfig.RUNTIME_PACKAGE == RunType.EXE && !runFile.equals(correctExe))
					{
						copyFile(runFile, correctExe);
						if(!correctJar.isFile())
							downloadFile(URL_LAUNCHER_DOWNLOADS + nameJar, correctJar, correctJar.getAbsolutePath());
					}
					break;
				default:
					updateLauncher(properties);
					break;
			}
		} catch(IOException ex) {
			Launcher.showError(ex.toString());
		}
	}
	private static String getFirstRunProjectCode()
	{
		try(InputStreamReader isr = new InputStreamReader(PropertiesManager.class.getResourceAsStream("/firstrun.txt"), "UTF-8"))
		{
			final char[] buffer = new char[5];
			if(isr.read(buffer) == 5)
				return new String(buffer).toUpperCase();
		} catch(NullPointerException | IOException ex) {
		}
		return "";
	}
	private static void updateLauncher(PropertiesManager properties)
	{
		logger.info("Загрузка и применение обновлений ...");
		final File   runFile    = new File(RuntimeConfig.RUNTIME_PATH);
		final String nameJar    = GlobalConfig.EXECUTABLE_NAME + ".jar";
		final File   correctJar = new File(RuntimeConfig.LAUNCHER_HOME, nameJar);
		if(RuntimeConfig.RUNTIME_PACKAGE == RunType.JAR && correctJar.compareTo(runFile) != 0)
		{
			downloadFile(URL_LAUNCHER_DOWNLOADS + nameJar, runFile);
			copyFile(runFile, correctJar);
		} else
			downloadFile(URL_LAUNCHER_DOWNLOADS + nameJar, correctJar);
		File restartFile = correctJar;
		if(RuntimeConfig.RUNTIME_PLATFORM == Platform.WINDOWS)
		{
			final String nameExe    = GlobalConfig.EXECUTABLE_NAME + ".exe";
			final File   correctExe = new File(RuntimeConfig.LAUNCHER_HOME, nameExe);
			if(RuntimeConfig.RUNTIME_PACKAGE == RunType.EXE && correctExe.compareTo(runFile) != 0)
			{
				downloadFile(URL_LAUNCHER_DOWNLOADS + nameExe, runFile);
				copyFile(runFile, correctExe);
			} else
				downloadFile(URL_LAUNCHER_DOWNLOADS + nameExe, correctExe);
			restartFile = correctExe;
		}
		restart(properties, restartFile);
	}
	public static void restart()
	{
		restart(Launcher.getInstance().properties, null);
	}
	private static void restart(PropertiesManager properties, File changeLauncherPath)
	{
		logger.info("Перезапуск лаунчера ...");
		final int nMemoryAllocation = properties.getData().nMemoryAllocationMB;
		final ArrayList<String> params = new ArrayList<>(32);
		params.add((RuntimeConfig.RUNTIME_PLATFORM == Platform.WINDOWS) ? "javaw" : "java");
		params.add(new StringBuilder().append("-Xmx").append(nMemoryAllocation).append("m").toString());
		params.add("-Dsun.java2d.noddraw=true");
		params.add("-Dsun.java2d.d3d=false");
		params.add("-Dsun.java2d.opengl=false");
		params.add("-Dsun.java2d.pmoffscreen=false");
		if(changeLauncherPath == null)
			changeLauncherPath = new File(RuntimeConfig.RUNTIME_PATH);
		params.add("-classpath");
		params.add(changeLauncherPath.getAbsolutePath());
		params.add(Launcher.class.getCanonicalName());
		params.add("--securityfeature");
		params.add(Integer.toHexString(new Random().nextInt(nMemoryAllocation)));
		try
		{
			new ProcessBuilder(params).directory(changeLauncherPath.getParentFile()).start();
		} catch(IOException | HeadlessException ex) {
			logger.error("Exception during process restart: ", ex);
		}
		System.exit(0);
	}
}
