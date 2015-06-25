package ru.methuselah.launcher.Data;
import java.io.File;
import java.util.ArrayList;
import ru.methuselah.securitylibrary.Data.MessagesLauncher.AnswerLauncherClients.ClientInfo;

public class OfflineClient extends ClientInfo
{
	public transient final ArrayList<String> libraries = new ArrayList<>();
	public transient final OfflineProject project;
	public transient File assetIndexFile;
	public OfflineClient()
	{
		project = null;
	}
	public OfflineClient(OfflineProject project, ClientInfo client)
	{
		this.project = project;
		this.caption = client.caption;
		this.captionLocalized = client.captionLocalized;
		this.folder = client.folder;
		this.baseVersion = client.baseVersion;
		this.jarFile = client.jarFile;
		this.mainClass = client.mainClass;
		this.additionalJavaArguments = client.additionalJavaArguments;
		this.additionalGameArguments = client.additionalGameArguments;
		this.contentsFile = client.contentsFile;
	}
	public File getClientHome()
	{
		return new File(project.getProjectHome(), folder);
	}
	public File getClientJar()
	{
		return new File(getClientHome(), jarFile);
	}
	public static boolean canPlayOffline(OfflineClient client)
	{
		return new File(client.getClientHome(), client.jarFile).isFile();
	}
	public void clean()
	{
		final File clientHome = getClientHome();
		if(clientHome.isDirectory())
			for(File assets : getClientHome().listFiles())
				if(assets.isDirectory() && assets.getName().startsWith("assets"))
					recursiveDeleteDir(assets);
	}
	private static void recursiveDeleteDir(File dir)
	{
		for(File sub : dir.listFiles())
		{
			if(sub.isFile())
				sub.delete();
			if(sub.isDirectory())
				recursiveDeleteDir(sub);
		}
		dir.delete();
	}
}
