package ru.methuselah.launcher.Downloaders;

import java.io.File;
import java.io.IOException;
import ru.methuselah.launcher.Configuration.GlobalConfig;
import ru.methuselah.launcher.Configuration.RuntimeConfig;
import ru.methuselah.launcher.Data.Platform;
import ru.methuselah.launcher.Data.RunType;
import ru.methuselah.launcher.Launcher;
import ru.methuselah.launcher.LauncherProperties;
import ru.methuselah.launcher.Utilities;
import ru.simsonic.rscCommonsLibrary.HashAndCipherUtilities;

public class LauncherUpdater extends BaseUpdater
{
	public static void checkLauncherUpdate(LauncherProperties properties)
	{
		final String firstRunCode = LauncherProperties.getFirstRunProjectCode();
		if(!"".equals(firstRunCode))
		{
			System.out.println("Установка начального проекта: " + firstRunCode);
			properties.reloadFromDisk();
			properties.saveToDisk();
			properties.data.lastOpenedProject = firstRunCode;
			properties.saveToDisk();
		}
		System.out.println("Поиск доступных обновлений ...");
		try
		{
			final String launcherHash = RuntimeConfig.UNDER_IDE_DEBUGGING
				? RuntimeConfig.DEVELOPER_IDE_HASH
				: (RuntimeConfig.RUNTIME_PATH.endsWith(".jar") || RuntimeConfig.RUNTIME_PATH.endsWith(".exe"))
					? HashAndCipherUtilities.fileToMD5(RuntimeConfig.RUNTIME_PATH)
					: "";
			final String launcherCheckResult = Utilities.executePost(
				GlobalConfig.URL_LAUNCHER_PHPS + "legacy/launcher.php",
				"launcherHash=" + launcherHash).trim();
			if("OK".equalsIgnoreCase(launcherCheckResult) || "NO CONNECTION".equals(launcherCheckResult))
			{
				System.out.println("Обновлений не обнаружено.");
				final File runFile    = new File(RuntimeConfig.RUNTIME_PATH);
				final String nameJar = GlobalConfig.EXECUTABLE_NAME + ".jar";
				final String nameExe = GlobalConfig.EXECUTABLE_NAME + ".exe";
				final File correctJar = new File(RuntimeConfig.LAUNCHER_HOME, nameJar);
				final File correctExe = new File(RuntimeConfig.LAUNCHER_HOME, nameExe);
				if(RuntimeConfig.RUNTIME_PACKAGE == RunType.JAR && !runFile.equals(correctJar))
				{
					copyFile(runFile, correctJar);
					if(!correctExe.isFile())
						downloadFile(GlobalConfig.URL_LAUNCHER_BINS + "launcher/" + nameExe, correctExe, correctExe.getAbsolutePath());
				}
				if(RuntimeConfig.RUNTIME_PACKAGE == RunType.EXE && !runFile.equals(correctExe))
				{
					copyFile(runFile, correctExe);
					if(!correctJar.isFile())
						downloadFile(GlobalConfig.URL_LAUNCHER_BINS + "launcher/" + nameJar, correctJar, correctJar.getAbsolutePath());
				}
			} else
				updateLauncher();
		} catch(IOException ex) {
			Launcher.showError(ex.toString());
		}
	}
	private static void updateLauncher()
	{
		System.out.println("Загрузка обновлений ...");
		final File runFile = new File(RuntimeConfig.RUNTIME_PATH);
		final String nameJar = GlobalConfig.EXECUTABLE_NAME + ".jar";
		final File correctJar = new File(RuntimeConfig.LAUNCHER_HOME, nameJar);
		if(RuntimeConfig.RUNTIME_PACKAGE == RunType.JAR && correctJar.compareTo(runFile) != 0)
		{
			downloadFile(GlobalConfig.URL_LAUNCHER_BINS + "launcher/" + nameJar, runFile);
			copyFile(runFile, correctJar);
		} else
			downloadFile(GlobalConfig.URL_LAUNCHER_BINS + "launcher/" + nameJar, correctJar);
		File restartFile = correctJar;
		if(RuntimeConfig.RUNTIME_PLATFORM == Platform.WINDOWS)
		{
			final String nameExe = GlobalConfig.EXECUTABLE_NAME + ".exe";
			final File correctExe = new File(RuntimeConfig.LAUNCHER_HOME, nameExe);
			if(RuntimeConfig.RUNTIME_PACKAGE == RunType.EXE && correctExe.compareTo(runFile) != 0)
			{
				downloadFile(GlobalConfig.URL_LAUNCHER_BINS + "launcher/" + nameExe, runFile);
				copyFile(runFile, correctExe);
			} else
				downloadFile(GlobalConfig.URL_LAUNCHER_BINS + "launcher/" + nameExe, correctExe);
			restartFile = correctExe;
		}
		System.out.println("Применение обновлений ...");
		Launcher.restart(restartFile);
	}
}
