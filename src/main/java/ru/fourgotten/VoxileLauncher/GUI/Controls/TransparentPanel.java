package ru.fourgotten.VoxileLauncher.GUI.Controls;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.JPanel;

public class TransparentPanel extends JPanel
{
	private Insets insets;
	public TransparentPanel(LayoutManager layout)
	{
		super(layout);
		setOpaque(false);
		setDoubleBuffered(true);
		setBackground(new Color(255, 255, 255, 0));
	}
	@Override
	public boolean isOpaque()
	{
		return false;
	}
	public void setInsets(int top, int left, int bottom, int right)
	{
		insets = new Insets(top, left, bottom, right);
	}
	@Override
	public Insets getInsets()
	{
		return (insets == null) ? super.getInsets() : insets;
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
