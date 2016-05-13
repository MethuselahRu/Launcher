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
import java.util.HashSet;
import ru.methuselah.launcher.Data.OfflineClient;
import ru.methuselah.launcher.Utilities;
import ru.methuselah.securitylibrary.Data.Launcher.ServerInfo;

public class GameLaunchHelper
{
	public static void setServersDatTopAddresses(OfflineClient client, ServerInfo[] entries)
	{
		final File serversDat = new File(client.getClientHome(), "servers.dat");
		final ArrayList<ServerInfo> serverListNew = new ArrayList<>();
		final HashSet<String> addressesToRemove = new HashSet<>();
		// Проверяем сервера в топе на корректность данных
		if(entries != null)
			for(ServerInfo entry : entries)
			{
				if(Utilities.emptyString(entry.address))
					continue;
				if(Utilities.emptyString(entry.caption))
				{
					addressesToRemove.add(entry.address.toLowerCase());
					continue;
				}
				serverListNew.add(entry);
			}
		// Парсинг имеющегося списка
		final ArrayList<ServerInfo> serverListOld = readServersDat(serversDat);
		// Вырезаем из нового списка адреса, которые должны быть в топе, и объединяем в один список
		for(ServerInfo serverOld : serverListOld)
		{
			// Удалим полностью записи, которые имеют пустое имя
			if(serverOld.address != null && addressesToRemove.contains(serverOld.address.toLowerCase()))
				continue;
			// Удалим дубликаты, имевшие другое название
			boolean duplicate = false;
			for(ServerInfo serverNew : serverListNew)
				if(serverNew.address.equalsIgnoreCase(serverOld.address))
					duplicate = true;
			// Остальные записи будут располагаться ниже
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
			try(BufferedReader br = new BufferedReader(new FileReader(file)))
			{
				// Чтение текстового файла
				final ArrayList<String> lines = new ArrayList<>();
				for(String line = br.readLine(); line != null; line = br.readLine())
					lines.add(line);
				// Изменение нужных ключей
				for(String line : lines)
					for(TextProperty prop : props)
						if(line.contains(prop.getSeparator()))
						{
							String[] parts = line.split("\\s*" + prop.getSeparator() + "\\s*");
							if(parts.length != 2)
								continue;
							if(parts[0].equals(prop.key))
								parts[1] = prop.value;
						}
				// Запись изменений
				// -- TO DO HERE --
			} catch(FileNotFoundException ex) {
			} catch(IOException ex) {
			}
		} else {
			try
			{
				file.getParentFile().mkdirs();
				file.createNewFile();
				try(FileWriter fw = new FileWriter(file))
				{
					for(TextProperty prop : props)
						fw.write(prop.getKey() + prop.getSeparator() + prop.getValue() + System.getProperty("line.separator"));
				}
			} catch(IOException ex) {
			}
		}
	}
	private static ArrayList<ServerInfo> readServersDat(File nbtFile)
	{
		final ArrayList<ServerInfo> result = new ArrayList<>();
		if(nbtFile.isFile())
		{
			try(NbtInputStream nbtIn = new NbtInputStream(new FileInputStream(nbtFile)))
			{
				// Нужно открыть servers.dat и прочитать все сервера в нём
				final ITag rootTag = nbtIn.readTag();
				if(rootTag != null)
				{
					final TagList list = (TagList)((TagCompound)rootTag).getTag("servers");
					if(list != null)
					{
						for(ITag serverTag : list.getTags())
						{
							final TagCompound serverInfo     = (TagCompound)serverTag;
							final TagString   tagCaption     = (TagString)serverInfo.getTag("name");
							final TagString   tagAddress     = (TagString)serverInfo.getTag("ip");
							final TagByte     tagHideAddress = (TagByte)  serverInfo.getTag("hideAddress");
							if(tagCaption == null || tagAddress == null)
								continue;
							final ServerInfo server = new ServerInfo(
								tagCaption.getValue(),
								tagAddress.getValue());
							// Корректность
							if(Utilities.emptyString(server.caption) || Utilities.emptyString(server.address))
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
		}
		return result;
	}
	private static void saveServersDat(File nbtFile, ArrayList<ServerInfo> serverList)
	{
		try
		{
			// Сохранение нового списка
			TagCompound newRoot = new TagCompound("");
			TagList newList = new TagList("servers");
			newRoot.setTag(newList);
			for(ServerInfo entry : serverList)
				newList.addTag(toTag(entry));
			nbtFile.createNewFile();
			try(NbtOutputStream nbtOut = new NbtOutputStream(new FileOutputStream(nbtFile)))
			{
				nbtOut.write(newRoot);
			}
		} catch(IOException | RuntimeException ex) {
			System.err.println(ex);
		}
	}
	private static TagCompound toTag(ServerInfo entry)
	{
		final TagCompound result = new TagCompound("");
		result.setTag(new TagByte("hideAddress", (byte)(entry.hideAddress ? 1 : 0)));
		result.setTag(new TagString("name", entry.caption));
		result.setTag(new TagString("ip", entry.address));
		return result;
	}
}
