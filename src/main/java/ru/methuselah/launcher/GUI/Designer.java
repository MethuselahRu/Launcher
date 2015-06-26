package ru.methuselah.launcher.GUI;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import ru.methuselah.launcher.Data.OfflineProject;
import ru.methuselah.launcher.GUI.Controls.TransparentLabel;
import ru.methuselah.launcher.GUI.Controls.TransparentPanel;
import ru.methuselah.launcher.GlobalConfig;
import ru.methuselah.launcher.Launcher;
import ru.methuselah.securitylibrary.Data.MessagesLauncher.AnswerLauncherDesign;

/*
 * Структура GUI:
 *
 * ПАНЕЛЬ "Учётная запись":
 *		Поле "Имя пользователя"
 *		Поле "Пароль"
 *			Галочка "Сохранять пароль"
 *		Кнопка "Вход"
 *			Галочка "Автоматический вход при запуске"
 *		Кнопка "Гостевой вход"
 *		Кнопка "Оффлайн режим"
 * ПАНЕЛЬ "Доступные клиенты":
 *		Выпадающий список доступных клиентов
 *			Галочка "Автоматически запускать выбранный клиент"
 *		Кнопка "Играть"
 *		Кнопка/ссылка "Сменить учётную запись"
 * ОСНОВНОЙ РОДИТЕЛЬСКИЙ ФРЕЙМ:
 *		Кнопка открытия настроек
 *		Ссылка "Вернуться к списку проектов"
 *		Ссылка "Вы используете устаревшую версию Java. Щелкните здесь, чтобы обновить."
 *    Кнопка/ссылка "Информация о платных услугах"
 * СВОБОДНЫЕ (ПРОИЗВОЛЬНЫЕ) ЭЛЕМЕНТЫ:
 *		Произвольные PNG
 *		Кнопки с нажатиями, ведущими на URL
 *		Ссылки с нажатиями, ведущими на URL
 * ОТДЕЛЬНЫЙ ФРЕЙМ "Список проектов" (ЕДИНЫЙ ДИЗАЙН):
 *		Список доступных проектов, отсортированных по рейтингу:
 *			Название проекта
 *			Строка с описанием
 *			5 звёзд для выставления рейтинга (с шагом в 1/2 звезды)
 *			Галочка "Автоматически подключаться к выбранному проекту при следующем запуске"
 *		Кнопка "Подключиться к проекту"
 *		Кнопка открытия настроек
 * ОТДЕЛЬНЫЙ ФРЕЙМ "Настройки" (ЕДИНЫЙ ДИЗАЙН):
 *		Выделяемая игре память
 *		Ссылка на папку с игрой
 */

/*
 * Кнопка - это набор из 3х картинок (нормальная, с наведённой мышкой и кликнутая).
 * Картинки с поддержкой прозрачности (png?)
 * Текст поверх опционален. Настраивается шрифт, размер, цвет. Возможно использовать html (поддержка самой java).
 * Тексту доступны все выравнивания по горизонтали/вертикали.
 * Доступен hint box с поддержкой html.
 * Для свободных элементов вводится URL.
*/

public class Designer extends JFrame
{
	protected final Launcher launcher;
	protected final OfflineProject currentProject;
	public static enum PANELS
	{
		login, clients, options;
	}
	protected void setFavicon(String resource)
	{
		try
		{
			setIconImage(ImageIO.read(Launcher.class.getResource(resource)));
		} catch(IOException ex) {
		}
	}
	public final PanelLinks panelLinks = new PanelLinks();
	public final PanelLogin panelLogin = new PanelLogin();
	public final PanelClients panelClients = new PanelClients();
	public final PanelOptions panelOptions = new PanelOptions();
	protected final TransparentPanel superPanelMain    = new TransparentPanel(new GridBagLayout());
	protected final TransparentPanel superPanelLogin   = new TransparentPanel(new BorderLayout());
	protected final TransparentPanel superPanelClients = new TransparentPanel(new BorderLayout());
	protected final TransparentPanel superPanelOptions = new TransparentPanel(new BorderLayout());
	protected final TransparentPanel superPanelLinks   = new TransparentPanel(new BorderLayout());
	protected final TransparentLabel linkUpdateJava    = new TransparentLabel(" Вы используете устаревшую версию Java. Щелкните здесь, чтобы обновить. ");
	protected Designer(Launcher launcher, OfflineProject project, AnswerLauncherDesign designDesc)
	{
		super(GlobalConfig.createMainFrameCaption(true));
		this.launcher = launcher;
		this.currentProject = project;
	}
	public static void openLink(String url)
	{
		try
		{
			java.awt.Desktop.getDesktop().browse(new URL(url).toURI());
		} catch(IOException | URISyntaxException ex) {
			System.err.println("Failed to open link: " + url);
		}
	}
	public String htmlText(String source)
	{
		return "<html>" + source
			.replace("%project%", currentProject.caption)
			+ "</html>";
	}
	protected void setTooltips()
	{
		panelLogin.chkSavePassword.setToolTipText(htmlText(
			"Если отметить эту галочку, то при следующем запуске поле <b>Пароль</b><br />"
			+ "будет автоматически заполнено."));
		panelLogin.chkAutoLogin.setToolTipText(htmlText(
			"Если отметить эту галочку, то при следующем запуске произойдёт автоматическая<br />"
			+ "попытка входа в введённую учётную запись."));
		panelLogin.btnLogIn.setToolTipText(htmlText(
			"Вход в игру с использованием лицензионного аккаунта <i>Mojang</i>,<br />"
			+ "учётных данных с сайта <b>%project%</b> или форума <b>voxile.ru</b><br />"
			+ "(в зависимости от решения администрации выбранного проекта)"));
		panelLogin.btnGuest.setToolTipText(htmlText(
			"Вход в игру от имени новой временной<br />"
			+ "беспарольной учётной записи.<br />"
			+ "<b>Не требует регистрации</b>"));
		panelLogin.btnOffline.setToolTipText(htmlText(
			"Запуск клиентской сборки с несуществующими учётными данными.<br />"
			+ "Это позволит Вам играть в локальный мир или подключаться на<br />"
			+ "сервера с выключенной проверкой учётных записей (<i>online-mode: false</i>).<br />"
			+ "<b>Не требует регистрации</b>"));
		panelClients.btnPlay.setToolTipText(htmlText(
			"Запуск выбранной клиентской сборки"));
		panelClients.btnLogout.setToolTipText(htmlText(
			"Выход из текущей учётной записи и переход к вводу логина и пароля."));
		panelClients.chkAutoStartGame.setToolTipText(htmlText(
			"Если у Вас настроен <b>автоматический вход</b> под какой-либо учётной<br />"
			+ "записью, то установка этой галочки позволит Вам запускать выбранную сборку<br />"
			+ "каждый раз автоматически при запуске лаунчера."));
		panelClients.lblName.setToolTipText(htmlText(
			"Именно так будет выглядеть Ваш ник во время игры.<br />"
			+ "Чтобы изменить его, воспользуйтесь помощью Администрации<br />"
			+ "(напишите заявку на форуме voxile.ru).<br />"
			+ "В будущем данная операция будет доступна в Личном Кабинете."));
		panelClients.lblUUID.setToolTipText(htmlText(
			"Ваш уникальный идентификатор позволяет отличить Ваш персонаж от всех<br />"
			+ "остальных игроков даже в том случае, если Вы смените свой ник или воспользуетесь<br />"
			+ "другим методов аутентификации."));
	}
}
