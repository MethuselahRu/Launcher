package ru.methuselah.launcher.Authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.methuselah.authlib.UserProvider;
import ru.methuselah.authlib.UserRole;
import ru.methuselah.authlib.data.AuthenticatePayload;
import ru.methuselah.authlib.data.AuthenticateResponse;
import ru.methuselah.authlib.links.Links;
import ru.methuselah.authlib.links.LinksMethuselah;
import ru.methuselah.authlib.methods.ResponseException;
import ru.methuselah.launcher.Data.OfflineProject;
import ru.methuselah.launcher.GUI.FormProject.PanelLogin;
import ru.methuselah.launcher.Launcher;
import ru.methuselah.launcher.Utilities;
import ru.methuselah.securitylibrary.Data.MessagesWrapper.MessageWrappedGame;
import ru.methuselah.securitylibrary.MethuselahPrivate;

public class Authentication
{
	private final Launcher launcher;
	private OfflineProject project;
	private String login = "Player";
	private String playername = "Player";
	private String uuid;
	private String accessToken;
	private String clientToken;
	private UserRole role = UserRole.nonauth;
	private UserProvider provider = UserProvider.nonauth;
	private volatile Thread authenticationThread;
	private final Links links = new LinksMethuselah();
	private final MethuselahPrivate caller = new MethuselahPrivate(links);
	private final Logger            logger = LoggerFactory.getLogger(Launcher.class);
	public Authentication(Launcher launcher)
	{
		this.launcher = launcher;
	}
	public Links getLinks()
	{
		return links;
	}
	public MethuselahPrivate getCaller()
	{
		return caller;
	}
	public void setSecurityHashCode(String uhash)
	{
	}
	public void restoreSavedUsername(OfflineProject project, PanelLogin panelLogin)
	{
		launcher.properties.reloadFromDisk();
		playername  = launcher.properties.getData().lastUsedPlayerName;
		clientToken = launcher.properties.getData().lastAuthClientToken;
		final String password = launcher.properties.getData().lastEnteredPassword;
		panelLogin.txtUsername.setText(playername);
		panelLogin.txtPassword.setText(password);
		panelLogin.chkSavePassword.setSelected(password.length() > 0);
		if(launcher.properties.getData().bAutoAuthenticate)
		{
			panelLogin.chkAutoLogin.setSelected(true);
			if(launcher.checkboxDrivenStart)
				authenticateNormal(project, playername, password);
		} else
			launcher.checkboxDrivenStart = false;
	}
	public boolean isAuthenticated()
	{
		return role != UserRole.nonauth;
	}
	public boolean isAuthenticatedStrongly()
	{
		switch(role)
		{
		case developer:
		case administrator:
		case player:
			return true;
		}
		return false;
	}
	public boolean isAuthenticatedOnProject(OfflineProject project)
	{
		if(this.project == null || project == null)
			return false;
		if(this.project.caption == null || project.caption == null)
			return false;
		if(this.project.caption.equalsIgnoreCase(project.caption))
			return isAuthenticatedStrongly();
		return false;
	}
	public String getUsedLogin()
	{
		return login;
	}
	public String getPlayerName()
	{
		return playername;
	}
	public String getUUID()
	{
		return uuid;
	}
	public String getAccessToken()
	{
		return accessToken;
	}
	public UserRole getUserRole()
	{
		return role;
	}
	public UserProvider getAuthProvider()
	{
		return provider;
	}
	public void authenticateNormal(OfflineProject project, String authUsername, String authPassword)
	{
		logout();
		beginAuthenticationProcedure(project, authUsername, authPassword, false);
	}
	public void authenticateGuest(OfflineProject project)
	{
		logout();
		beginAuthenticationProcedure(project, "", null, true);
	}
	public void authenticateOffline(String offlineUsername)
	{
		logout();
		playername = offlineUsername;
		uuid = "00000000-0000-0000-0000-000000000000";
		loginSucceeded();
	}
	public void logout()
	{
		cancelAuthenticationProcedure();
		playername = "Player";
		uuid = "";
		accessToken = "";
		clientToken = "";
		role = UserRole.nonauth;
		provider = UserProvider.nonauth;
		launcher.clearProjectClients();
	}
	public boolean isBlocked()
	{
		return authenticationThread != null && authenticationThread.isAlive();
	}
	private void beginAuthenticationProcedure(final OfflineProject project, final String authUsername, final String authPassword, final boolean asGuest)
	{
		Launcher.showGrant("Авторизация ...");
		cancelAuthenticationProcedure();
		if((Utilities.emptyString(authUsername) || Utilities.emptyString(authPassword)) && !asGuest)
		{
			Launcher.showError("Введите имя пользователя и пароль");
			return;
		}
		authenticationThread = new Thread()
		{
			@Override
			public void run()
			{
				launcher.projectFrame.updateOnlineMode();
				uuid = null;
				accessToken = null;
				clientToken = null;
				if(doAuthYggdrasil(authUsername, authPassword, asGuest))
				{
					loginSucceeded();
					/*
					if(!asGuest)
						launcher.connection.onLauncherAuthenticate(uuid, playername, securityHashCode);
					*/
				} else
					loginFailed();
				authenticationThread = null;
				launcher.projectFrame.updateOnlineMode();
			}
		};
		authenticationThread.start();
	}
	private void cancelAuthenticationProcedure()
	{
		if(authenticationThread != null)
		{
			try
			{
				authenticationThread.interrupt();
				authenticationThread.join();
			} catch(InterruptedException ex) {
			}
			authenticationThread = null;
		}
		launcher.projectFrame.updateOnlineMode();
	}
	private boolean doAuthYggdrasil(String tryUsername, String tryPassword, boolean guest)
	{
		final AuthenticatePayload authenticate = new AuthenticatePayload();
		try
		{
			authenticate.project = launcher.currentProject.code;
			authenticate.guest = guest;
			if(guest)
			{
				authenticate.username = "username";
				authenticate.password = "password";
			} else {
				this.login = tryUsername;
				authenticate.username = tryUsername;
				authenticate.password = tryPassword;
			}
			final AuthenticateResponse response = caller.authenticate(authenticate);
			this.playername = response.selectedProfile.name;
			this.uuid = response.selectedProfile.id;
			this.accessToken = response.accessToken;
			this.clientToken = response.clientToken;
			this.role = guest ? UserRole.guest : response.role;
			this.provider = response.provider;
			return true;
		} catch(ResponseException ex) {
			Launcher.showError("Ошибка: " + ex.errorResponse.error);
			Utilities.sleep(1);
		} catch(RuntimeException ex) {
			Launcher.showError("Ошибка обращения к реализации Yggdrasil");
			Utilities.sleep(1);
		}
		return false;
	}
	public void loginSucceeded()
	{
		launcher.projectFrame.authenticated();
		// Сохранение конфигурации
		if(role != UserRole.guest && role != UserRole.nonauth)
		{
			launcher.projectFrame.panelLogin.txtUsername.setText(login);
			launcher.properties.getData().lastUsedPlayerName = login;
			launcher.properties.getData().lastEnteredPassword =
				launcher.projectFrame.panelLogin.chkSavePassword.isSelected() ?
				String.copyValueOf(launcher.projectFrame.panelLogin.txtPassword.getPassword()) : null;
			launcher.properties.getData().lastAuthClientToken = clientToken;
			if(launcher.projectFrame.panelLogin.chkSavePassword.isSelected())
				launcher.properties.getData().bAutoAuthenticate = launcher.projectFrame.panelLogin.chkAutoLogin.isSelected();
			launcher.properties.saveToDisk();
		}
	}
	public void loginFailed()
	{
		launcher.projectFrame.deauthenticated();
		Launcher.showError("Не удалось авторизоваться");
	}
	public MessageWrappedGame createWrapperMessage()
	{
		final MessageWrappedGame result = new MessageWrappedGame();
		result.username = playername;
		if(isAuthenticated())
		{
			result.uuid = uuid;
			result.accessToken = accessToken;
		} else {
			result.uuid = "00000000-0000-0000-0000-000000000000";
			result.accessToken = "00000000000000000000000000000000";
		}
		return result;
	}
}
