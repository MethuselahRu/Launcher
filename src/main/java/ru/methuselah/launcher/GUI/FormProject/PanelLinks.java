package ru.methuselah.launcher.GUI.FormProject;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import ru.methuselah.launcher.GUI.Controls.TransparentButton;
import ru.methuselah.launcher.GUI.Controls.TransparentLabel;
import ru.methuselah.launcher.GUI.Controls.TransparentPanel;

public class PanelLinks extends TransparentPanel
{
	public final TransparentLabel  infoLabel = new TransparentLabel ("", 0);
	public final TransparentButton btnSetup  = new TransparentButton("<html><font color='black'><b>Настройки ...</b></font></html>");
	public final TransparentButton btnDonate = new TransparentButton("<html><font color='yellow'>Пожертвовать на развитие <sup><b>i</b></sup></font></html>");
	public final TransparentButton btnSite1  = new TransparentButton("<html><font color='black'>Группа ВКонтакте</font></html>");
	public final TransparentButton btnSite2  = new TransparentButton("<html><font color='black'>Наш форум</font></html>");
	public final TransparentButton btnVideo1 = new TransparentButton("<html><font color='black'>YouTube</font></html>");
	public final TransparentButton btnVideo2 = new TransparentButton("<html><font color='black'>Twitch</font></html>");
	public final TransparentButton btnVote1  = new TransparentButton("<html><font color='black'>@ fairtop</font></html>");
	public final TransparentButton btnVote2a = new TransparentButton("<html><font color='black'>@ mctop</font></html>");
	public final TransparentButton btnVote2b = new TransparentButton("<html><font color='black'>@ mcrate</font></html>");
	public final TransparentButton btnVote3a = new TransparentButton("<html><font color='black'><b>Primary</b> @ mcs</font></html>");
	public final TransparentButton btnVote3b = new TransparentButton("<html><font color='black'><b>Simple</b> @ mcs</font></html>");
	public final TransparentButton btnVote4a = new TransparentButton("<html><font color='black'><b>Primary</b> @ mm</font></html>");
	public final TransparentButton btnVote4b = new TransparentButton("<html><font color='black'><b>Simple</b> @ mm</font></html>");
	public PanelLinks()
	{
		super();
		setBackground(new Color(0, 0, 0, 127));
		setPreferredSize(new Dimension(435, 180));
		setInsets(0, 20, 10, 20);
		final GridBagLayout grid = new GridBagLayout();
		setLayout(grid);
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets.set(1, 1, 1, 1);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill    = GridBagConstraints.BOTH;
		// Informational label
		infoLabel.setMaximumSize(new Dimension(435, 40)); // Было раньше setPrefferedSize
		gbc.gridwidth = 4;
		gbc.gridx     = 0;
		gbc.gridy     = 0;
		add(infoLabel, gbc);
		// Settings button
		gbc.gridwidth = 2;
		gbc.gridx     = 2;
		gbc.gridy     = 1;
		add(btnSetup, gbc);
		// Donate button
		gbc.gridwidth = 2;
		gbc.gridx     = 2;
		gbc.gridy     = 2;
		add(btnDonate, gbc);
		// Main vote #1
		gbc.gridwidth = 2;
		gbc.gridx     = 0;
		gbc.gridy     = 1;
		add(btnVote1, gbc);
		// Main votes #2
		gbc.gridwidth = 1;
		gbc.gridx     = 0;
		gbc.gridy     = 2;
		add(btnVote2a, gbc);
		gbc.gridx     = 1;
		add(btnVote2b, gbc);
		// Secondary votes #1
		gbc.gridwidth = 1;
		gbc.gridx     = 0;
		gbc.gridy     = 3;
		add(btnVote3a, gbc);
		gbc.gridx     = 1;
		add(btnVote3b, gbc);
		// Secondary votes #2
		gbc.gridwidth = 1;
		gbc.gridx     = 0;
		gbc.gridy     = 4;
		add(btnVote4a, gbc);
		gbc.gridx     = 1;
		add(btnVote4b, gbc);
		// Websites #1
		gbc.gridwidth = 1;
		gbc.gridx     = 2;
		gbc.gridy     = 3;
		add(btnSite1, gbc);
		gbc.gridx     = 3;
		add(btnSite2, gbc);
		// Websites #2
		gbc.gridwidth = 1;
		gbc.gridx     = 2;
		gbc.gridy     = 4;
		add(btnVideo1, gbc);
		gbc.gridx     = 3;
		add(btnVideo2, gbc);
		// btnVote1.setToolTipText("<html>За голосование на этом сайте можно получить вознаграждение в 100 игровых монет</html>");
		// btnVote2a.setToolTipText("<html>За голосование на этом сайте можно получить вознаграждение в 100 игровых монет</html>");
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
