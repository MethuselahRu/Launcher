package ru.methuselah.launcher.Game;

import com.evilco.mc.nbt.stream.NbtInputStream;
import com.evilco.mc.nbt.stream.NbtOutputStream;
import com.evilco.mc.nbt.tag.ITag;
import com.evilco.mc.nbt.tag.TagByte;
import com.evilco.mc.nbt.tag.TagCompound;
import com.evilco.mc.nbt.tag.TagList;
import com.evilco.mc.nbt.tag.TagString;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import ru.methuselah.launcher.Data.OfflineClient;

public class GameLaunchHelper
{
	public static class ServersDatEntry
	{
		public String caption;
		public String address;
		public boolean hideAddress;
		public ServersDatEntry(String name, String ip)
		{
			this(name, ip, false);
		}
		public ServersDatEntry(String name, String ip, boolean hideAddress)
		{
			this.caption = name;
			this.address = ip;
			this.hideAddress = hideAddress;
		}
		public TagCompound toTag()
		{
			final TagCompound result = new TagCompound("");
			result.setTag(new TagByte("hideAddress", (byte)(hideAddress ? 1 : 0)));
			result.setTag(new TagString("name", caption));
			result.setTag(new TagString("ip", address));
			return result;
		}
	}
	public static void setServersDatTopAddresses(OfflineClient client, ServersDatEntry[] entries)
	{
		final File serversDat = new File(client.getClientHome(), "servers.dat");
		final ArrayList<ServersDatEntry> serverListNew = new ArrayList<>();
		// Проверяем сервера в топе на корректность данных
		if(entries != null)
			for(ServersDatEntry entry : entries)
			{
				if(entry.caption == null || "".equals(entry.caption))
					continue;
				if(entry.address == null || "".equals(entry.address))
					continue;
				serverListNew.add(entry);
			}
		// Парсинг имеющегося списка
		final ArrayList<ServersDatEntry> serverListOld = readServersDat(serversDat);
		// Вырезаем из нового списка адреса, которые должны быть в топе, и объединяем в один список
		for(ServersDatEntry serverOld : serverListOld)
		{
			boolean duplicate = false;
			for(ServersDatEntry serverNew : serverListNew)
				if(serverNew.address.equalsIgnoreCase(serverOld.address))
					duplicate = true;
			if(duplicate == false)
				serverListNew.add(serverOld);
		}
		// Сохраняем новый список
		saveServersDat(serversDat, serverListNew);
	}
	public static abstract class TextProperty
	{
		protected final String key;
		protected final String value;
		protected TextProperty(String key, String value)
		{
			this.key = key;
			this.value = value;
		}
		protected final String getKey()
		{
			return key;
		}
		protected final String getValue()
		{
			return value;
		}
		protected abstract String getSeparator();
	}
	public static final class TextPropertyEqualSep extends TextProperty
	{
		public TextPropertyEqualSep(String key, String value)
		{
			super(key, value);
		}
		@Override
		protected String getSeparator()
		{
			return "=";
		}
	}
	public static final class TextPropertyColonSep extends TextProperty
	{
		public TextPropertyColonSep(String key, String value)
		{
			super(key, value);
		}
		@Override
		protected String getSeparator()
		{
			return ":";
		}
	}
	public static void processTextFile(OfflineClient client, String filename, TextProperty[] props)
	{
		final File file = new File(client.getClientHome(), filename);
		if(file.isFile())
		{
			try
			{
				final ArrayList<String> lines = new ArrayList<>();
				final BufferedReader br = new BufferedReader(new FileReader(file));
				for(String line = br.readLine(); line != null; line = br.readLine())
					lines.add(line);
				br.close();
				for(String line : lines)
					for(TextProperty prop : props)
						if(line.contains(prop.getSeparator()))
						{
							String[] parts = line.split("\\s*" + prop.getSeparator() + "\\s*");
							if(parts.length != 2)
								continue;
							if(parts[0].equals(prop.key))
							{
								
							}
						}
			} catch(FileNotFoundException ex) {
			} catch(IOException ex) {
			}
		} else {
			try
			{
				file.getParentFile().mkdirs();
				file.createNewFile();
				final FileWriter fw = new FileWriter(file);
				for(TextProperty prop : props)
					fw.write(prop.getKey() + prop.getSeparator() + prop.getValue() + System.getProperty("line.separator"));
				fw.close();
			} catch(IOException ex) {
			}
		}
	}
	private static ArrayList<ServersDatEntry> readServersDat(File nbtFile)
	{
		final ArrayList<ServersDatEntry> result = new ArrayList<>();
		try
		{
			// Нужно открыть servers.dat и прочитать все сервера в нём
			final NbtInputStream nbtIn = new NbtInputStream(new FileInputStream(nbtFile));
			final ITag rootTag = nbtIn.readTag();
			nbtIn.close();
			if(rootTag != null)
			{
				final TagList list = (TagList)((TagCompound)rootTag).getTag("servers");
				if(list != null)
				{
					for(ITag serverTag : list.getTags())
					{
						final TagCompound serverInfo = (TagCompound)serverTag;
						final TagString tagCaption = (TagString)serverInfo.getTag("name");
						final TagString tagAddress = (TagString)serverInfo.getTag("ip");
						final TagByte   tagHideAddress = (TagByte)serverInfo.getTag("hideAddress");
						if(tagCaption == null || tagAddress == null)
							continue;
						final ServersDatEntry server = new ServersDatEntry(
							tagCaption.getValue(),
							tagAddress.getValue());
						// Корректность
						if(server.caption == null || "".equals(server.caption))
							continue;
						if(server.address == null || "".equals(server.address))
							continue;
						if(tagHideAddress != null)
							server.hideAddress = (tagHideAddress.getValue() != 0);
						result.add(server);
					}
				}
			}
		} catch(IOException | RuntimeException ex) {
			System.err.println(ex);
		}
		return result;
	}
	private static void saveServersDat(File nbtFile, ArrayList<ServersDatEntry> serverList)
	{
		try
		{
			// Сохранение нового списка
			TagCompound newRoot = new TagCompound("");
			TagList newList = new TagList("servers");
			newRoot.setTag(newList);
			for(ServersDatEntry entry : serverList)
				newList.addTag(entry.toTag());
			final NbtOutputStream nbtOut = new NbtOutputStream(new FileOutputStream(nbtFile));
			nbtOut.write(newRoot);
			nbtOut.close();
		} catch(IOException | RuntimeException ex) {
			System.err.println(ex);
		}
	}
}
