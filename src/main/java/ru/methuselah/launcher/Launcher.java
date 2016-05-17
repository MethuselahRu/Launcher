package ru.methuselah.launcher;

import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.methuselah.authlib.methods.ResponseException;
import ru.methuselah.launcher.Configuration.RuntimeConfig;
import ru.methuselah.launcher.Data.OfflineClient;
import ru.methuselah.launcher.Data.OfflineProject;
import ru.methuselah.launcher.Data.Platform;
import ru.methuselah.launcher.Downloaders.LauncherUpdater;
import ru.methuselah.launcher.Downloaders.NativesManager;
import ru.methuselah.launcher.Downloaders.ResourceManager;
import ru.methuselah.launcher.GUI.Common.SplashScreen;
import ru.methuselah.launcher.GUI.FormProject.ProjectFrame;
import ru.methuselah.launcher.GUI.FormProjectList.ProjectListFrame;
import ru.methuselah.launcher.Game.GameLauncher;
import ru.methuselah.securitylibrary.Data.Launcher.ClientInfo;
import ru.methuselah.securitylibrary.Data.Launcher.LauncherAnswerClients;
import ru.methuselah.securitylibrary.Data.Launcher.LauncherAnswerDesign;
import ru.methuselah.securitylibrary.Data.Launcher.LauncherAnswerProjects;
import ru.methuselah.securitylibrary.Data.Launcher.LauncherMessageGetClients;
import ru.methuselah.securitylibrary.Data.Launcher.ProjectInfo;

public class Launcher implements Runnable
{
	// Синглтон
	private final static Launcher instance = new Launcher();
	private final SplashScreen splash = new SplashScreen();
	public  final static Launcher getInstance()
	{
		return Launcher.instance;
	}
	// Общие параметры
	public OfflineProject currentProject;
	public OfflineClient currentClient;
	// Описание
	public final Logger             logger         = LoggerFactory.getLogger(Launcher.class);
	public final LauncherProperties properties     = new LauncherProperties();
	public final Offline            offline        = new Offline(this);
	public final ResourceManager    resourceMan    = new ResourceManager();
	public final NativesManager     nativesMan     = new NativesManager(resourceMan);
	public final Authentication     authentication = new Authentication(this);
	public final GameLauncher       gameLauncher   = new GameLauncher(this);
	public final ProjectListFrame   projectsFrame  = new ProjectListFrame(this);
	public boolean checkboxDrivenStart = true;
	public ProjectFrame launcherFrame;
	@Override
	public void run()
	{
		logger.info ("Hello, logging world!");
		// Проверка наличия обновлений
		LauncherUpdater.checkLauncherUpdate(properties);
		final HashMap<String, OfflineProject> projectMap = new HashMap<>();
		// Dummy offline project
		final OfflineProject dummy = new OfflineProject();
		projectMap.put(dummy.code, dummy);
		// Load offline projects
		for(OfflineProject offlineProject : Offline.searchOfflineProjects())
			projectMap.put(offlineProject.code, offlineProject);
		// Load project list from methuselah.ru
		try
		{
			final LauncherAnswerProjects msgProjects = authentication.getCaller().listProjects();
			if(msgProjects.projects != null && msgProjects.projects.length > 0)
				for(ProjectInfo info : msgProjects.projects)
				{
					final OfflineProject online = new OfflineProject(info);
					online.availableRemote = true;
					projectMap.put(online.code, online);
				}
			final ArrayList<OfflineProject> projects = new ArrayList<>(projectMap.values());
			Collections.sort(projects);
			projectsFrame.setProjectsList(projects.toArray(new OfflineProject[projectMap.size()]));
			projectsFrame.selectProjectIfExist(properties.data.lastOpenedProject);
			final OfflineProject startupProject = projectsFrame.getSelectedProject();
			// Hide splash earlier and show main form
			splash.join();
			informAboutAncientJava();
			// Let's do something
			if(startupProject == null)
			{
				checkboxDrivenStart = false;
				projectsFrame.setVisible(true);
			} else
				onSwitchToProject(startupProject);
		} catch(ResponseException ex) {
			logger.info("{0}", ex);
		}
	}
	public void onSwitchToProject(OfflineProject project)
	{
		currentProject = project;
		currentProject.getProjectHome().mkdir();
		final LauncherAnswerDesign msgDesign = new LauncherAnswerDesign(); // connection.onLauncherLoadDesign(currentProject.code);
		ResourceManager.saveDesignFile(currentProject, msgDesign);
		launcherFrame = new ProjectFrame(this, currentProject, msgDesign);
		authentication.restoreSavedUsername(project, launcherFrame.panelLogin);
		showGrant(RuntimeConfig.VERSION + (RuntimeConfig.UNDER_IDE_DEBUGGING ? " (IDE)" : ""));
		launcherFrame.setVisible(true);
	}
	public boolean getProjectClients()
	{
		// Полученние списка доступных клиентов по SSL
		final LauncherMessageGetClients payload = new LauncherMessageGetClients();
		payload.uuid = authentication.getUUID();
		payload.accessToken = authentication.getAccessToken();
		payload.project = currentProject.code;
		try
		{
			final LauncherAnswerClients msgClients = authentication.getCaller().listProjectClients(payload);
			if(msgClients == null || msgClients.clients == null)
			{
				authentication.logout();
				showError("Ошибка при получении списка клиентов");
				return false;
			}
			final ArrayList<OfflineClient> clientList = new ArrayList<>();
			for(ClientInfo client : msgClients.clients)
			{
				final OfflineClient gameInfo = new OfflineClient(currentProject, client);
				// Ручная ещё пока работа, ёпт...
				if(gameInfo.jarFile.contains("v1.6.4"))
					gameInfo.libraries.add("minecraft_v1.6.4_libraries.jar");
				if(gameInfo.jarFile.contains("v1.7.2"))
					gameInfo.libraries.add("minecraft_v1.7.2_libraries.jar");
				if(gameInfo.jarFile.contains("v1.7.10"))
					gameInfo.libraries.add("minecraft_v1.7.10_libraries.jar");
				if(gameInfo.jarFile.contains("v1.8"))
					gameInfo.libraries.add("minecraft_v1.8_libraries.jar");
				if(gameInfo.jarFile.contains("v1.9"))
					gameInfo.libraries.add("minecraft_v1.9_libraries.jar");
				clientList.add(gameInfo);
			}
			currentProject.clients = clientList.toArray(new OfflineClient[clientList.size()]);
			// Инициализация данных в формах
			int lastUsedIndex = -1;
			for(OfflineClient gameInfo : currentProject.clients)
			{
				launcherFrame.panelClients.cbSelectClient.addItem(gameInfo.captionLocalized);
				if(gameInfo.caption.equalsIgnoreCase(properties.data.lastStartedClient))
					lastUsedIndex = launcherFrame.panelClients.cbSelectClient.getItemCount();
			}
			if(lastUsedIndex != -1)
				launcherFrame.panelClients.cbSelectClient.setSelectedIndex(lastUsedIndex - 1);
			return true;
		} catch(ResponseException ex) {
			logger.info("{0}", ex);
		}
		return false;
	}
	public void clearProjectClients()
	{
		if(launcherFrame != null)
			launcherFrame.panelClients.cbSelectClient.removeAllItems();
		currentClient = null;
		if(currentProject != null)
			currentProject.clients = new OfflineClient[] {};
	}
	public static synchronized void showGrant(String grant)
	{
		instance.logger.info(grant);
		if(instance.launcherFrame != null)
		{
			instance.launcherFrame.panelLinks.infoLabel.setFont(new Font("Segoe UI", 2, 16));
			instance.launcherFrame.panelLinks.infoLabel.setForeground(new Color(0x80, 0xFF, 0x80));
			instance.launcherFrame.panelLinks.infoLabel.setText(grant);
			instance.launcherFrame.invalidate();
			instance.launcherFrame.validate();
		}
	}
	public static synchronized void showError(String error)
	{
		instance.logger.error(error);
		if(instance.launcherFrame != null)
		{
			instance.launcherFrame.panelLinks.infoLabel.setFont(new Font("Segoe UI", 2, 16));
			instance.launcherFrame.panelLinks.infoLabel.setForeground(new Color(0xFF, 0x80, 0x80));
			instance.launcherFrame.panelLinks.infoLabel.setText(error);
			instance.launcherFrame.invalidate();
			instance.launcherFrame.validate();
		}
	}
	private boolean informAboutBlockedHardware()
	{
		return JOptionPane.showConfirmDialog(projectsFrame, new JLabel("<html>"
			+ "Ваша система внесена в чёрный список.<br />"
			+ "Традиционными причинами для блокировки являются:"
			+ "<ul>"
			+ "<li>Использование программ, нарушающих корректную работу сетевой игры (читов)</li>"
			+ "<li>Неподобающее поведение в отношении любых участников игрового сообщества</li>"
			+ "</ul>"
			+ "Информацию о других возможных причинах блокировке Вы можете найти в разделах правил.<br />"
			+ "Если это произошло по ошибке, обратитесь к администрации <a href='voxile.ru'>voxile.ru</a>.<br /><br />"
			+ "<b>OK</b> — продолжить работу в оффлайн режиме<br />"
			+ "<b>Cancel/Отмена</b> — завершить работу лаунчера<br />"
			+ "</html>"),
			"Вы заблокированы",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.OK_OPTION;
	}
	private void informAboutAncientJava()
	{
		if(Utilities.testJavaForUpdate() == false)
		{
			// Напоминать в будущем, когда текущая версия устареет
			properties.data.allowAncientJavaVersion = "";
			properties.saveToDisk();
			return;
		}
		switch(JOptionPane.showConfirmDialog(projectsFrame, new JLabel("<html>"
				+ "Было обнаружено, что Вы используете устаревшую версию виртуальной машины Java"
				+ " (<u>" + System.getProperty("java.version") + "</u>)<br />"
				+ "Мы настойчиво рекомендуем скачать и установить новую версию. Хотите сделать это сейчас?<br /><br />"
				+ "<b>Yes/Да</b> — перейти на сайт для загрузки последней версии<br />"
				+ "<b>No/Нет</b> — больше никогда не напоминать об этом<br />"
				+ "<b>Cancel/Отмена</b> — напомнить при следующем перезапуске<br />"
				+ "</html>"),
			"Устаревшая версия Java", JOptionPane.YES_NO_CANCEL_OPTION))
		{
		case JOptionPane.YES_OPTION:
			// Перейти на сайт
			try
			{
				java.awt.Desktop.getDesktop().browse(URI.create("http://www.oracle.com/technetwork/java/javase/downloads/index.html"));
				JOptionPane.showMessageDialog(projectsFrame, new String[]
				{
					"Сейчас лаунчер будет автоматически закрыт.",
					"Не запускайте его до завершения установки новой версии Java.",
				}, "Ожидание обновления Java", JOptionPane.INFORMATION_MESSAGE);
			} catch(IOException ex) {
			}
			// Напоминать в будущем
			properties.data.allowAncientJavaVersion = "";
			properties.saveToDisk();
			System.exit(0);
			return;
		case JOptionPane.NO_OPTION:
			properties.data.allowAncientJavaVersion = System.getProperty("java.version");
			properties.saveToDisk();
			return;
		case JOptionPane.CANCEL_OPTION:
		default:
			// Напоминать в будущем
			properties.data.allowAncientJavaVersion = "";
			properties.saveToDisk();
			break;
		}
	}
	public static void restart(File changeLauncherPath)
	{
		final int nMemoryAllocation = instance.properties.data.nMemoryAllocationMB;
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
			instance.logger.error("{}", ex);
		}
		System.exit(0);
	}
	public static void main(String[] args)
	{
		// new MojangVersionManager().test();
		instance.run();
	}
}
