package ru.methuselah.launcher.GUI.Controls;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.border.CompoundBorder;

public class TransparentButton extends JButton
{
	CompoundBorder border = new CompoundBorder(
		BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#9dfc6e")), // #8dec5e
		BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#0e8d00"))); // #000000
	public TransparentButton(String txt)
	{
		super();
		setBorder(border);
		setText(txt);
		setBorderPainted(true);
		setContentAreaFilled(false);
		setOpaque(false);
		setContentAreaFilled(false);
		setForeground(Color.decode("#e1e8da"));
		setPreferredSize(new Dimension(130, 25));
		setCursor(Cursor.getPredefinedCursor(12));
	}
	public TransparentButton(String txt, int width, int height)
	{
		super();
		setText(txt);
		setBorderPainted(true);
		setContentAreaFilled(false);
		setFocusPainted(false);
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(true);
		setBorder(border);
		setForeground(Color.decode("#e1e8da"));
		setPreferredSize(new Dimension(width, height));
		setCursor(Cursor.getPredefinedCursor(12));
	}
	@Override
	protected void paintComponent(Graphics g)
	{
		ButtonModel buttonModel = getModel();
		Graphics2D gd = (Graphics2D) g.create();
		gd.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gd.setPaint(new GradientPaint(0, 0, Color.decode("#6dc550"), 0, getHeight(), Color.decode("#5fb544")));
		if(buttonModel.isRollover())
		{
			gd.setPaint(new GradientPaint(0, 0, Color.decode("#5fb544"), 0, getHeight(), Color.decode("#6dc550")));
			if(buttonModel.isPressed())
				gd.setPaint(new GradientPaint(0, 0, Color.decode("#1e7d00"), 0, getHeight(), Color.decode("#91e53c")));
			else
				setForeground(Color.white);
		}
		gd.fillRect(0, 0, getWidth(), getHeight());
		gd.dispose();
		super.paintComponent(g);
	}
}
