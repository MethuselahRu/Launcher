package ru.methuselah.launcher.GUI.Controls;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicCheckBoxUI;
import ru.methuselah.launcher.Launcher;

public class TransparentCheckbox extends JCheckBox
{
	public static final ArrayList<ImageIcon> BGRND_STATES = new ArrayList<>();
	public static final ArrayList<ImageIcon> CHECK_STATES = new ArrayList<>();
	static
	{
		// Иконки состояния фона
		for(int i = 1; i <= 4; i++)
			BGRND_STATES.add(new ImageIcon(
				Launcher.class.getResource("checkbox_u" + i + ".png")));
		// Дополнительное "пустое" состояние выделения
		CHECK_STATES.add(new ImageIcon(
			new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)));
		// Состояния выделения
		for(int i = 1; i <= 4; i++)
			CHECK_STATES.add(new ImageIcon(
				 Launcher.class.getResource("checkbox_c" + i + ".png")));
	}
	private int bgIcon = 0;
	private boolean in;
	private Timer bgTimer;
	private int checkIcon = 0;
	private boolean checking;
	private Timer checkTimer;
	private boolean animated = true;
	public TransparentCheckbox(String text)
	{
		super();
		setPreferredSize(new Dimension(16, 16));
		setBounds(0, 0, 16, 16);
		initializeUI();
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setText(text);
	}
	private void initializeUI()
	{
		// Меры предосторожности, чтобы нативный стиль не влиял на вид чекбокса
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		setMargin(new Insets(0, 0, 0, 0));
		setUI(new BasicCheckBoxUI());
		// Устанавливаем исходную иконку
		updateIcon();
		// Таймер для плавного изменения фона при наведении на чекбокс
		bgTimer = new Timer(40, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(in && bgIcon < BGRND_STATES.size() - 1)
				{
					bgIcon++;
					updateIcon();
				} else if(!in && bgIcon > 0)
				{
					bgIcon--;
					updateIcon();
				} else
					bgTimer.stop();
			}
		});
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				in = true;
				bgTimer.start();
			}
			@Override
			public void mouseExited(MouseEvent e)
			{
				in = false;
				bgTimer.start();
			}
		});
		// Таймер для плавного изменения состояния
		checkTimer = new Timer(40, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(checking && checkIcon < CHECK_STATES.size() - 1)
				{
					checkIcon++;
					updateIcon();
				} else if(!checking && checkIcon > 0) {
					checkIcon--;
					updateIcon();
				} else
					checkTimer.stop();
			}
		});
		addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(animated)
				{
					if(isSelected())
					{
						checking = true;
						checkTimer.start();
					} else {
						checking = false;
						checkTimer.start();
					}
				} else {
					checkIcon = isSelected() ? CHECK_STATES.size() - 1 : 0;
					updateIcon();
				}
			}
		});
	}
	/*
	 * Метод для обновления иконки чекбокса
	 */
	private final Map<String, ImageIcon> iconsCache = new HashMap<>();
	private synchronized void updateIcon()
	{
		// Обновляем иконку чекбокса
		final String key = bgIcon + "," + checkIcon;
		if(iconsCache.containsKey(key))
		{
			// Необходимая иконка уже была ранее использована
			setIcon(iconsCache.get(key));
		} else {
			// Создаем новую иконку совмещающую в себе фон и состояние поверх
			BufferedImage b = new BufferedImage(BGRND_STATES.get(0).getIconWidth(),
					  BGRND_STATES.get(0).getIconHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = b.createGraphics();
			g2d.drawImage(BGRND_STATES.get(bgIcon).getImage(), 0, 0,
					  BGRND_STATES.get(bgIcon).getImageObserver());
			g2d.drawImage(CHECK_STATES.get(checkIcon).getImage(), 0, 0,
					  CHECK_STATES.get(checkIcon).getImageObserver());
			g2d.dispose();
			ImageIcon icon = new ImageIcon(b);
			iconsCache.put(key, icon);
			setIcon(icon);
		}
	}
	/*
	 * Включение/отключение анимации
	 * Например при использовании в качестве редактора в ячейке таблицы анимацию стоит отключать
	 */
	public boolean isAnimated()
	{
		return animated;
	}
	public void setAnimated(boolean bAnimated)
	{
		this.animated = bAnimated;
	}
	@Override
	public boolean isOpaque()
	{
		return false;
	}
}
