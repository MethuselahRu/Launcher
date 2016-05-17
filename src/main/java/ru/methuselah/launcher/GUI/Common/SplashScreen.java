package ru.methuselah.launcher.GUI.Common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import ru.methuselah.launcher.Configuration.GlobalConfig;
import ru.methuselah.launcher.Configuration.RuntimeConfig;
import ru.methuselah.launcher.Launcher;

public class SplashScreen extends JFrame implements Runnable
{
	private final Image     image     = Toolkit.getDefaultToolkit().getImage(Launcher.class.getResource("splash.png"));
	private final ImageIcon imageIcon = new ImageIcon(image);
	private final Thread    thread    = new Thread(this);
	public SplashScreen()
	{
		try
		{
			// Инициализация внешнего вида для Windows
			for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
				if("Windows".equalsIgnoreCase(info.getName()))
					UIManager.setLookAndFeel(info.getClassName());
		} catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			Launcher.getInstance().logger.error("{}", ex);
		}
		JFrame.setDefaultLookAndFeelDecorated(true);
		thread.start();
	}
	@Override
	public void run()
	{
		if(RuntimeConfig.UNDER_IDE_DEBUGGING && GlobalConfig.HIDE_SPLASH_IN_DBG)
			return;
		// If translucent windows aren't supported, exit.
		final boolean isTranslucencySupported = GraphicsEnvironment
			.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice()
			.isWindowTranslucencySupported(WindowTranslucency.PERPIXEL_TRANSLUCENT);
		if(isTranslucencySupported == false)
			return;
		setUndecorated(true);
		setSize(imageIcon.getIconWidth(), imageIcon.getIconHeight());
		setLocationRelativeTo(null);
		setBackground(new Color(0, 0, 0, 0));
		setVisible(true);
		try
		{
			Thread.sleep(GlobalConfig.SPLASH_TIMEOUT_MS);
		} catch(InterruptedException ex) {
		}
		setVisible(false);
		dispose();
	}
	@Override
	public void paint(Graphics g)
	{
		g.drawImage(image, 0, 0, this);
	}
	public void join()
	{
		try
		{
			thread.interrupt();
			thread.join();
		} catch(InterruptedException ex) {
		}
	}
}
