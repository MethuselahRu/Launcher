package ru.methuselah.launcher.GUI;
import ru.methuselah.launcher.GUI.Controls.TransparentButton;
import ru.methuselah.launcher.GUI.Controls.TransparentLabel;
import ru.methuselah.launcher.GUI.Controls.TransparentPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

public class PanelLinks extends TransparentPanel
{
	public final TransparentButton btnSetup  = new TransparentButton("<html><font color='black'>Настройки</font></html>");
	public final TransparentButton btnDonate = new TransparentButton("<html><font color='yellow'>Пожертвовать на развитие</font></html>");
	public final TransparentButton btnVote1  = new TransparentButton("<html><font color='black'>Голосовать @ mctop <sup>i</sup></font></html>");
	public final TransparentButton btnVote2  = new TransparentButton("<html><font color='black'>Голосовать @ topcraft <sup>i</sup></font></html>");
	public final TransparentButton btnVote3  = new TransparentButton("<html><font color='black'>Голосовать @ mc-servera</font></html>");
	public final TransparentButton btnVote4  = new TransparentButton("<html><font color='black'>Страничка @ monitoringminecraft</font></html>");
	public final TransparentButton btnSite1  = new TransparentButton("<html><font color='black'>Группа ВКонтакте</font></html>");
	public final TransparentButton btnSite2  = new TransparentButton("<html><font color='black'>Наш форум</font></html>");
	public final TransparentLabel infoLabel  = new TransparentLabel("", 0);
	public PanelLinks()
	{
		super(new GridLayout(3, 1, 0, 5));
		setPreferredSize(new Dimension(435, 180));
		setBackground(new Color(0, 0, 0, 127));
		infoLabel.setMaximumSize(new Dimension(435, 40));
		add(infoLabel);
		setInsets(0, 20, 10, 20);
		add(new TransparentPanel(new GridLayout(0, 2, 5, 5))
		{
			{
				add(new TransparentPanel(new BorderLayout())
				{
					{
						add(btnVote1, "North");
						add(btnVote2, "South");
					}
				}, "West");
				add(new TransparentPanel(new BorderLayout())
				{
					{
						add(btnSetup,  "North");
						add(btnDonate, "South");
					}
				}, "East");
			}
		});
		add(new TransparentPanel(new GridLayout(0, 2, 5, 5))
		{
			{
				add(new TransparentPanel(new BorderLayout())
				{
					{
						add(btnVote3, "North");
						add(btnVote4, "South");
					}
				}, "West");
				add(new TransparentPanel(new BorderLayout(0, 0))
				{
					{
						add(btnSite1, "North");
						add(btnSite2, "South");
					}
				}, "East");
			}
		});
		btnVote1.setToolTipText("<html>За голосование на этом сайте можно получить вознаграждение в 100 игровых монет</html>");
		btnVote2.setToolTipText("<html>За голосование на этом сайте можно получить вознаграждение в 100 игровых монет</html>");
		btnDonate.setToolTipText("<html>"
			+ "Если Вам нравится наш сервер, пожалуйста, помогите нам сделать его лучше!<br />"
			+ "Мы находимся онлайн уже более полутора лет, и всё это время оплачиваем хостинги<br />"
			+ "самостоятельно. Это не является слишком критичным для нас, но время от времени<br />"
			+ "влияет прямо или косвенно на скорость разработки.<br /><br />"
			+ "Если мы сможем собрать достаточную сумму, она придаст нам значительный импульс в развитии:<br />"
			+ "приглашение новых игроков к нам на различных площадках, новые мощности, услуги профессионалов<br />"
			+ "в разных областях.<br/><br />"
			+ "Вне зависимости от результата, <b>спасибо</b> за то, что Вы остаётесь с нами!</html>");
	}
}
