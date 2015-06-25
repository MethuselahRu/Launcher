package ru.methuselah.launcher.GUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import ru.methuselah.launcher.Launcher;

/*
 *		Поле "Имя пользователя"
 *		Поле "Пароль"
 *			Галочка "Сохранять пароль"
 *		Кнопка "Вход"
 *			Галочка "Автоматический вход при запуске"
 *		Кнопка "Гостевой вход"
 *		Кнопка "Оффлайн режим"

 *		Выпадающий список доступных клиентов
 *			Галочка "Автоматически запускать выбранный клиент"
 *		Кнопка "Играть"
 *		Кнопка/ссылка "Сменить учётную запись"

 *		Кнопка открытия настроек
 *		Ссылка "Вернуться к списку проектов"
 *		Ссылка "Вы используете устаревшую версию Java. Щелкните здесь, чтобы обновить."
 *    Кнопка/ссылка "Информация о платных услугах"
 */

public class InterfaceActions
{
	private final FrameLauncherMain frame;
	public InterfaceActions(FrameLauncherMain frame)
	{
		this.frame = frame;
	}
	public void setOnLogin(final Runnable action)
	{
		final ActionListener listener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				action.run();
			}
		};
		frame.panelLogin.btnLogIn.addActionListener(listener);
		frame.panelLogin.txtUsername.addActionListener(listener);
		frame.panelLogin.txtPassword.addActionListener(listener);
	}
	public void setOnGuest(final Runnable action)
	{
		frame.panelLogin.btnGuest.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				action.run();
			}
		});
	}
	public void setOnOffline(final Runnable action)
	{
		frame.panelLogin.btnOffline.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				action.run();
			}
		});
	}
	public void setOnCheckSavePassword(final Runnable action)
	{
		frame.panelLogin.chkSavePassword.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				action.run();
			}
		});
	}
	public void setOnCheckAutoLogin(final Runnable action)
	{
		frame.panelLogin.chkAutoLogin.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				action.run();
			}
		});
	}
	public void setOnChangeCurrentClient(final Runnable action)
	{
		frame.panelClients.cbSelectClient.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				action.run();
			}
		});
	}
	public void setOnPlay(final Runnable action)
	{
		frame.panelClients.btnPlay.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				action.run();
			}
		});
	}
	public void setOnLogout(final Runnable action)
	{
		frame.panelClients.btnLogout.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				action.run();
			}
		});
	}
	public void setOnChangeProject(final Runnable action)
	{
		frame.lnkSwitchToOtherProject.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent arg0)
			{
				frame.setVisible(false);
				Launcher.getInstance().launcherFrame = null;
				Launcher.getInstance().projectsFrame.setVisible(true);
				frame.dispose();
			}
		});
	}
	public void setOnCheckAutoStartGame(final Runnable action)
	{
		frame.panelClients.chkAutoStartGame.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				action.run();
			}
		});
	}
	public void setOnClose(final Runnable action)
	{
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				action.run();
			}
		});
	}
}
