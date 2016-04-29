package ru.methuselah.launcher.GUI.FormProject;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPasswordField;
import ru.methuselah.launcher.GUI.Controls.OpaquePasswordField;
import ru.methuselah.launcher.GUI.Controls.OpaqueTextField;
import ru.methuselah.launcher.GUI.Controls.TransparentButton;
import ru.methuselah.launcher.GUI.Controls.TransparentCheckbox;
import ru.methuselah.launcher.GUI.Controls.TransparentPanel;

public final class PanelLogin extends TransparentPanel
{
	public final OpaqueTextField txtUsername = new OpaqueTextField(10, "Введите Ваш логин");
	public final JPasswordField txtPassword = new OpaquePasswordField(10, "*******");
	public final TransparentCheckbox chkSavePassword = new TransparentCheckbox("Сохранить пароль");
	public final TransparentCheckbox chkAutoLogin = new TransparentCheckbox("Автоматический вход");
	public final TransparentButton btnLogIn = new TransparentButton("Вход", 136, 32);
	public final TransparentButton btnGuest = new TransparentButton("Гостевой вход", 136, 32);
	public final TransparentButton btnOffline = new TransparentButton("Оффлайн режим", 136, 32);
	public PanelLogin()
	{
		super(new BorderLayout(0, 0));
		final TransparentPanel panelAccountFields = new TransparentPanel(new GridLayout(0, 1, 0, 5));
		panelAccountFields.setInsets(20, 20, 20, 10);
		panelAccountFields.add(txtUsername);
		panelAccountFields.add(txtPassword);
		panelAccountFields.add(chkSavePassword);
		chkAutoLogin.setSelected(false);
		panelAccountFields.add(chkAutoLogin);
		chkSavePassword.setBorder(null);
		chkAutoLogin.setBorder(null);
		add(panelAccountFields);
		final TransparentPanel panelAccountButtons = new TransparentPanel(new GridLayout(0, 1, 0, 5));
		panelAccountButtons.setInsets(20, 10, 60, 20);
		panelAccountButtons.add(btnLogIn);
		panelAccountButtons.add(btnGuest);
		panelAccountButtons.add(btnOffline);
		add(panelAccountButtons, "East");
	}
}
