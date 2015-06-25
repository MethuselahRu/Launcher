package ru.methuselah.launcher.GUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import ru.methuselah.launcher.GUI.Controls.TransparentButton;
import ru.methuselah.launcher.GUI.Controls.TransparentCheckbox;
import ru.methuselah.launcher.GUI.Controls.TransparentLabel;
import ru.methuselah.launcher.GUI.Controls.TransparentPanel;

public class PanelClients extends TransparentPanel
{
	public final TransparentLabel lblSelectYourClient = new TransparentLabel("Выберите клиент из предложенного списка:");
	public final JComboBox cbSelectClient = new JComboBox();
	public final TransparentCheckbox chkAutoStartGame = new TransparentCheckbox("Запускать игру автоматически");
	public final TransparentButton btnPlay = new TransparentButton("Начать игру!", 136, 32);
	public final TransparentButton btnLogout = new TransparentButton("Сменить пользователя", 136, 32);
	public final TransparentLabel lblName = new TransparentLabel("");
	public final TransparentLabel lblUUID = new TransparentLabel("");
	public PanelClients()
	{
		super(new BorderLayout(0, 5));
		final TransparentPanel panelButtons = new TransparentPanel(new GridLayout(0, 1, 0, 5));
		panelButtons.setInsets(20, 10, 35, 20);
		panelButtons.add(btnPlay);
		panelButtons.add(btnLogout);
		final TransparentPanel panelСlientList = new TransparentPanel(new GridLayout(0, 1, 0, 5));
		lblSelectYourClient.setForeground(Color.black);
		cbSelectClient.setBorder(null);
		panelСlientList.setInsets(20, 20, 20, 20);
		panelСlientList.add(lblSelectYourClient, "North");
		panelСlientList.add(cbSelectClient);
		chkAutoStartGame.setSelected(false);
		panelСlientList.add(chkAutoStartGame, "South");
		final TransparentPanel panelTextInfo = new TransparentPanel(new GridLayout(0, 1, 0, 5));
		panelTextInfo.setInsets(10, 10, 10, 10);
		lblName.setForeground(Color.BLACK);
		lblUUID.setForeground(Color.BLACK);
		panelTextInfo.add(lblName);
		panelTextInfo.add(lblUUID);
		add(panelButtons, "East");
		add(panelСlientList);
		add(panelTextInfo, "South");
	}
}
