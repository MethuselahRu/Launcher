package ru.methuselah.launcher.GUI.Controls;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JLabel;

public class TransparentLabel extends JLabel
{
	public TransparentLabel(String string, int swingConstants)
	{
		super(string, swingConstants);
		setForeground(Color.WHITE);
		setBackground(new Color(255, 255, 255, 0));
	}
	public TransparentLabel(String string)
	{
		super(string);
		setForeground(Color.WHITE);
		setBackground(new Color(255, 255, 255, 0));
	}
	@Override
	public boolean isOpaque()
	{
		return false;
	}
	@Override
	protected void paintComponent(Graphics g)
	{
		getParent().repaint();
		g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
		super.paintComponent(g);
	}
}
