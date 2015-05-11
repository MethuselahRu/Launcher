package ru.fourgotten.VoxileLauncher.GUI.Controls;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.JPasswordField;
import javax.swing.border.CompoundBorder;

public class OpaquePasswordField extends JPasswordField
{
	CompoundBorder border = new CompoundBorder(
		BorderFactory.createMatteBorder(0, 0, 1, 0, Color.white),
		BorderFactory.createMatteBorder(1, 1, 0, 0, Color.decode("#7e6c56")));
	public OpaquePasswordField(int cols, final String txt)
	{
		super(cols);
		setText(txt);
		setBackground(Color.decode("#968c6f"));
		setForeground(Color.white);
		border = new CompoundBorder(
			border, BorderFactory.createMatteBorder(1, 1, 0, 0, Color.decode("#8e8267")));
		setBorder(BorderFactory.createCompoundBorder(
			border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		setCaretColor(Color.white);
		setFont(new Font("Arial", 1, 12));
		setToolTipText(txt);
		addFocusListener(new FocusListener()
		{
			@Override
			public void focusLost(FocusEvent arg0)
			{
				 if(java.util.Arrays.equals(getPassword(), "".toCharArray()))
					setText("*******");
			}
			@Override
			public void focusGained(FocusEvent arg0)
			{
				if(java.util.Arrays.equals(getPassword(), txt.toCharArray()))
					setText("");
			}
		});
	}
}
