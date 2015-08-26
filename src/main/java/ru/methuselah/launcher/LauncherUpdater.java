package ru.methuselah.launcher;

import java.io.File;
import java.io.IOException;
import ru.methuselah.launcher.Data.Platform;
import ru.methuselah.launcher.Data.RunType;
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
			final String launcherHash = GlobalConfig.bUnderIDE
				? GlobalConfig.devHashIDE
				: (GlobalConfig.runPath.endsWith(".jar") || GlobalConfig.runPath.endsWith(".exe"))
					? HashAndCipherUtilities.fileToMD5(GlobalConfig.runPath)
					: "";
			final String launcherCheckResult = Utilities.executePost(
				GlobalConfig.urlScripts + "legacy/launcher.php",
				"launcherHash=" + launcherHash).trim();
			if("OK".equalsIgnoreCase(launcherCheckResult) || "NO CONNECTION".equals(launcherCheckResult))
			{
				System.out.println("Обновлений не обнаружено.");
				final File runFile    = new File(GlobalConfig.runPath);
				final String nameJar = GlobalConfig.executableName + ".jar";
				final String nameExe = GlobalConfig.executableName + ".exe";
				final File correctJar = new File(GlobalConfig.launcherHomeDir, nameJar);
				final File correctExe = new File(GlobalConfig.launcherHomeDir, nameExe);
				if(GlobalConfig.runType == RunType.JAR && !runFile.equals(correctJar))
				{
					copyFile(runFile, correctJar);
					if(!correctExe.isFile())
						downloadFile(GlobalConfig.urlBinaries + "launcher/" + nameExe, correctExe, correctExe.getAbsolutePath());
				}
				if(GlobalConfig.runType == RunType.EXE && !runFile.equals(correctExe))
				{
					copyFile(runFile, correctExe);
					if(!correctJar.isFile())
						downloadFile(GlobalConfig.urlBinaries + "launcher/" + nameJar, correctJar, correctJar.getAbsolutePath());
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
		final File runFile = new File(GlobalConfig.runPath);
		final String nameJar = GlobalConfig.executableName + ".jar";
		final File correctJar = new File(GlobalConfig.launcherHomeDir, nameJar);
		if(GlobalConfig.runType == RunType.JAR && correctJar.compareTo(runFile) != 0)
		{
			downloadFile(GlobalConfig.urlBinaries + "launcher/" + nameJar, runFile);
			copyFile(runFile, correctJar);
		} else
			downloadFile(GlobalConfig.urlBinaries + "launcher/" + nameJar, correctJar);
		File restartFile = correctJar;
		if(GlobalConfig.platform == Platform.WINDOWS)
		{
			final String nameExe = GlobalConfig.executableName + ".exe";
			final File correctExe = new File(GlobalConfig.launcherHomeDir, nameExe);
			if(GlobalConfig.runType == RunType.EXE && correctExe.compareTo(runFile) != 0)
			{
				downloadFile(GlobalConfig.urlBinaries + "launcher/" + nameExe, runFile);
				copyFile(runFile, correctExe);
			} else
				downloadFile(GlobalConfig.urlBinaries + "launcher/" + nameExe, correctExe);
			restartFile = correctExe;
		}
		System.out.println("Применение обновлений ...");
		Launcher.restart(restartFile);
	}
}
