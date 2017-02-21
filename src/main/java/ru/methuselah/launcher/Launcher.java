package ru.methuselah.launcher;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.methuselah.authlib.methods.ResponseException;
import ru.methuselah.launcher.Authentication.Authentication;
import ru.methuselah.launcher.Configuration.PropertiesManager;
import ru.methuselah.launcher.Configuration.RuntimeConfig;
import ru.methuselah.launcher.Data.Offline;
import ru.methuselah.launcher.Data.OfflineClient;
import ru.methuselah.launcher.Data.OfflineProject;
import ru.methuselah.launcher.Downloaders.BootUpdater;
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
	public  final static Launcher getInstance()
	{
		return Launcher.instance;
	}
	// Описание
	public  final Logger             logger         = LoggerFactory.getLogger(Launcher.class);
	private final SplashScreen       splash         = new SplashScreen();
	public  final PropertiesManager  properties     = new PropertiesManager();
	public  final Offline            offline        = new Offline(this);
	public  final ResourceManager    resourceMan    = new ResourceManager();
	public  final NativesManager     nativesMan     = new NativesManager(resourceMan);
	public  final Authentication     authentication = new Authentication(this);
	public  final GameLauncher       gameLauncher   = new GameLauncher(this);
	public  final ProjectListFrame   projectsFrame  = new ProjectListFrame(this);
	// Общие параметры
	public OfflineProject currentProject;
	public OfflineClient  currentClient;
	public ProjectFrame   projectFrame;
	public boolean        checkboxDrivenStart;
	@Override
	public void run()
	{
		// Проверка наличия обновлений лаунчера
		BootUpdater.checkForUpdates(properties);
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
			projectsFrame.selectProjectIfExist(properties.getData().lastOpenedProject);
			final OfflineProject startupProject = projectsFrame.getSelectedProject();
			// Hide splash earlier and show main form
			splash.join();
			informAboutAncientJava();
			// Let's do something
			checkboxDrivenStart = true;
			if(startupProject == null)
			{
				checkboxDrivenStart = false;
				projectsFrame.setVisible(true);
				projectsFrame.toFront();
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
		projectFrame = new ProjectFrame(this, currentProject, msgDesign);
		authentication.restoreSavedUsername(project, projectFrame.panelLogin);
		showGrant("Версия " + RuntimeConfig.VERSION + (RuntimeConfig.UNDER_IDE_DEBUGGING ? " (запуск под контролем IDE)" : ""));
		projectFrame.setVisible(true);
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
				// Ручная ещё пока работа, ёпт ...
				if(gameInfo.jarFile.contains("v1.6.4"))
					gameInfo.libraries.add("minecraft_v1.6.4_libraries.jar");
				if(gameInfo.jarFile.contains("v1.7.2"))
					gameInfo.libraries.add("minecraft_v1.7.2_libraries.jar");
				if(gameInfo.jarFile.contains("v1.7.10"))
					gameInfo.libraries.add("minecraft_v1.7.10_libraries.jar");
				if(gameInfo.jarFile.contains("v1.8"))
					gameInfo.libraries.add("minecraft_v1.8_libraries.jar");
				if(gameInfo.jarFile.contains("v1.9"))
				{
					if(gameInfo.jarFile.contains("v1.9.4"))
						gameInfo.libraries.add("minecraft_v1.9.4_libraries.jar");
					else
						gameInfo.libraries.add("minecraft_v1.9_libraries.jar");
				}
				if(gameInfo.jarFile.contains("v1.10"))
					gameInfo.libraries.add("minecraft_v1.10_libraries.jar");
				clientList.add(gameInfo);
			}
			currentProject.clients = clientList.toArray(new OfflineClient[clientList.size()]);
			// Инициализация данных в формах
			int lastUsedIndex = -1;
			for(OfflineClient gameInfo : currentProject.clients)
			{
				projectFrame.panelClients.cbSelectClient.addItem(gameInfo.captionLocalized);
				if(gameInfo.caption.equalsIgnoreCase(properties.getData().lastStartedClient))
					lastUsedIndex = projectFrame.panelClients.cbSelectClient.getItemCount();
			}
			if(lastUsedIndex != -1)
				projectFrame.panelClients.cbSelectClient.setSelectedIndex(lastUsedIndex - 1);
			return true;
		} catch(ResponseException ex) {
			logger.info("{0}", ex);
		}
		return false;
	}
	public void clearProjectClients()
	{
		if(projectFrame != null)
			projectFrame.panelClients.cbSelectClient.removeAllItems();
		currentClient = null;
		if(currentProject != null)
			currentProject.clients = new OfflineClient[] {};
	}
	public static synchronized void showGrant(String grant)
	{
		instance.logger.info(grant);
		if(instance.projectFrame != null)
		{
			instance.projectFrame.panelLinks.infoLabel.setFont(new Font("Segoe UI", 2, 16));
			instance.projectFrame.panelLinks.infoLabel.setForeground(new Color(0x80, 0xFF, 0x80));
			instance.projectFrame.panelLinks.infoLabel.setText(grant);
			instance.projectFrame.invalidate();
			instance.projectFrame.validate();
		}
	}
	public static synchronized void showError(String error)
	{
		instance.logger.error(error);
		if(instance.projectFrame != null)
		{
			instance.projectFrame.panelLinks.infoLabel.setFont(new Font("Segoe UI", 2, 16));
			instance.projectFrame.panelLinks.infoLabel.setForeground(new Color(0xFF, 0x80, 0x80));
			instance.projectFrame.panelLinks.infoLabel.setText(error);
			instance.projectFrame.invalidate();
			instance.projectFrame.validate();
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
			properties.getData().allowAncientJavaVersion = "";
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
			properties.getData().allowAncientJavaVersion = "";
			properties.saveToDisk();
			System.exit(0);
			return;
		case JOptionPane.NO_OPTION:
			properties.getData().allowAncientJavaVersion = System.getProperty("java.version");
			properties.saveToDisk();
			return;
		case JOptionPane.CANCEL_OPTION:
		default:
			// Напоминать в будущем
			properties.getData().allowAncientJavaVersion = "";
			properties.saveToDisk();
			break;
		}
	}
	public static void main(String[] args)
	{
		// new MojangVersionManager().test();
		instance.run();
	}
}
