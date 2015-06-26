package ru.methuselah.launcher.GUI;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import ru.methuselah.launcher.Launcher;

public class SplashScreen extends JFrame
{
	private static final int splashTimeout = 4000;
	private static final Image image = Toolkit.getDefaultToolkit().getImage(Launcher.class.getResource("splash.png"));
	private static final ImageIcon imageIcon = new ImageIcon(image);
	private final Thread splashThread;
	public SplashScreen()
	{
		try
		{
			// Инициализация внешнего вида для Windows
			for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
				if("Windows".equalsIgnoreCase(info.getName()))
					UIManager.setLookAndFeel(info.getClassName());
		} catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			System.err.println(ex);
		}
		JFrame.setDefaultLookAndFeelDecorated(true);
		splashThread = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					setUndecorated(true);
					setSize(imageIcon.getIconWidth(), imageIcon.getIconHeight());
					setLocationRelativeTo(null);
					com.sun.awt.AWTUtilities.setWindowOpaque(SplashScreen.this, false);
					setVisible(true);
					Thread.sleep(splashTimeout);
				} catch(InterruptedException ex) {
				} finally {
					setVisible(false);
					dispose();
				}
			}
		};
		splashThread.start();
	}
	@Override
	public void paint(Graphics g)
	{
		g.drawImage(image, 0, 0, this);
	}
	public void join(boolean bInterrupt)
	{
		try
		{
			if(splashThread.isAlive())
				if(bInterrupt)
					splashThread.interrupt();
			splashThread.join();
		} catch(InterruptedException ex) {
		}
	}
}
