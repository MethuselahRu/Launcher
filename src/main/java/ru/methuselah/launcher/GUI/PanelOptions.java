package ru.methuselah.launcher.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import ru.methuselah.launcher.GUI.Controls.OpaqueTextField;
import ru.methuselah.launcher.GUI.Controls.TransparentButton;
import ru.methuselah.launcher.GUI.Controls.TransparentLabel;
import ru.methuselah.launcher.GUI.Controls.TransparentPanel;
import ru.methuselah.launcher.GlobalConfig;
import ru.methuselah.launcher.Launcher;

public class PanelOptions extends TransparentPanel
{
	public final TransparentLabel lblMemory = new TransparentLabel("Выделяемая память: ");
	public final TransparentLabel lblUpdate = new TransparentLabel("Обновление клиента: ");
	public final TransparentLabel lblPath = new TransparentLabel("Путь к папке с игрой: ");
	public final TransparentLabel lnkDirectory = new TransparentLabel("");
	public final OpaqueTextField txtMemory = new OpaqueTextField(20, "");
	public final TransparentButton btnForceUpdate = new TransparentButton("Обновить клиент");
	public final TransparentButton btnDone = new TransparentButton("Готово");
	public PanelOptions()
	{
		super(new BorderLayout(0, 0));
		lblMemory.setForeground(Color.black);
		lblUpdate.setForeground(Color.black);
		lblPath.setForeground(Color.black);
		final TransparentPanel paneloptions_left = new TransparentPanel(new GridLayout(0, 1, 0, 5));
		paneloptions_left.add(lblMemory);
		paneloptions_left.add(lblUpdate);
		paneloptions_left.add(lblPath);
		final TransparentPanel paneloptions_right = new TransparentPanel(new GridLayout(0, 1, 0, 5));
		paneloptions_right.add(txtMemory);
		paneloptions_right.add(btnForceUpdate);
		paneloptions_right.add(lnkDirectory);
		setInsets(20, 20, 20, 20);
		add(paneloptions_left);
		add(paneloptions_right, "East");
		add(btnDone, "South");
		lnkDirectory.setText(GlobalConfig.launcherHomeDir.toString());
		lnkDirectory.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lnkDirectory.setForeground(Color.blue);
		txtMemory.setText(Integer.toString(Launcher.getInstance().properties.data.nMemoryAllocationMB));
	}
}
