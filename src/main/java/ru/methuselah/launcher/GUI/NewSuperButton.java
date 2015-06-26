package ru.methuselah.launcher.GUI;

import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

public class NewSuperButton extends JButton
{
	private final ImageIcon imageNormal;
	private final ImageIcon imageHovered;
	private final ImageIcon imagePressed;
	private final ImageIcon imageDisabled;
	public NewSuperButton(ImageIcon n, ImageIcon h, ImageIcon c, ImageIcon d, String text)
	{
		super("<html><b>" + text + "</b></html>");
		setForeground(Color.WHITE);
		this.imageNormal = n;
		this.imageHovered = h;
		this.imagePressed = c;
		this.imageDisabled = d;
		//setBorderPainted(false);
		//setBorder(null);
		//setFocusable(false);
		//setMargin(new Insets(0, 0, 0, 0));
		setOpaque(false);
		setContentAreaFilled(false);
		setIcon(imageNormal);
		setRolloverIcon(imageHovered);
		setPressedIcon(imagePressed);
		setDisabledIcon(imageDisabled);
		setVerticalTextPosition(SwingConstants.CENTER);
		setHorizontalTextPosition(SwingConstants.CENTER);
		setSize(n.getIconWidth(), n.getIconHeight());
	}
}
