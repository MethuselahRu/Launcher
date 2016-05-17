package ru.methuselah.launcher.Configuration;

public class PropertiesField
{
	public String   lastOpenedProject       = "";
	public String   lastUsedPlayerName      = "Player";
	public String   lastEnteredPassword     = "";
	public String   lastAuthClientToken     = "";
	public boolean  bSavePassword;
	public boolean  bAutoAuthenticate;
	public String   lastStartedClient       = "";
	public boolean  bAutoStartGame;
	public int      nMemoryAllocationMB     = 1024;
	public String   allowAncientJavaVersion = "";
	public String[] offlineProjects;
}
