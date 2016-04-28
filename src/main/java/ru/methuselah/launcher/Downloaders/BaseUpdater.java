package ru.methuselah.launcher.Downloaders;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.PrivilegedActionException;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import ru.methuselah.launcher.Launcher;

public class BaseUpdater
{
	public static void downloadFile(String srcURL, File saveAs)
	{
		downloadFile(srcURL, saveAs, null);
	}
	public static void downloadFile(String srcURL, File saveAs, String showAs)
	{
		saveAs.getParentFile().mkdirs();
		try(FileOutputStream fos = new FileOutputStream(saveAs))
		{
			if(showAs != null)
				Launcher.showGrant("↓ " + showAs);
			else
				System.out.println("Загрузка файла " + saveAs.getName() + "...");
			final ReadableByteChannel rbc = Channels.newChannel(new URL(srcURL).openStream());
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.flush();
		} catch(MalformedURLException ex) {
			System.err.println(ex);
		} catch(IOException ex) {
			System.err.println(ex);
		}
	}
	public static void copyFile(File from, File to)
	{
		try(FileOutputStream fos = new FileOutputStream(to);
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(from)))
		{
			System.out.println("Копирование " + from + " -> " + to);
			final ReadableByteChannel rbc = Channels.newChannel(bis);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.flush();
		} catch(IOException | NullPointerException ex) {
			System.err.println(ex);
		}
	}
	public static void unZip(File fileZip, boolean annotate) throws PrivilegedActionException
	{
		if(fileZip.isFile())
		{
			System.out.println("Распаковка: " + fileZip);
			if(annotate)
				Launcher.showGrant("Распаковка " + fileZip.getName() + "...");
			try(ZipFile zf = new ZipFile(fileZip))
			{
				final String szExtractPath = fileZip.getParent();
				for(ZipEntry zipEntry : Collections.list(zf.entries()))
					extractFromZip(szExtractPath, zipEntry.getName(), zf, zipEntry);
				System.err.println("Удачно!");
				if(annotate)
					Launcher.showGrant("Распаковка " + fileZip.getName() + " завершена");
			} catch(IOException ex) {
				System.err.println(ex);
			} finally {
				fileZip.delete();
			}
		} else
			System.err.println((new StringBuilder()).append("\nNot found: ").append(fileZip).toString());
	}
	private static void extractFromZip(String szExtractPath, String szName, ZipFile zf, ZipEntry ze)
	{
		if(ze.isDirectory())
			return;
		final String targetFileName = szName.replace('\\', File.separatorChar).replace('/', File.separatorChar);
		final File   targetFile     = new File(szExtractPath + File.separator + targetFileName);
		final long   size           = ze.getSize();
		final long   compressedSize = ze.getCompressedSize();
		System.out.println("\tИзвлечение " + targetFile.getName() + " (" + compressedSize + " -> " + size + ")");
		try
		{
			targetFile.getParentFile().mkdirs();
			try(InputStream is = zf.getInputStream(ze);
				FileOutputStream fos = new FileOutputStream(targetFile))
			{
				final ReadableByteChannel rbc = Channels.newChannel(is);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.flush();
			}
		} catch(IOException ex) {
			System.err.println(ex);
		}
	}
}
