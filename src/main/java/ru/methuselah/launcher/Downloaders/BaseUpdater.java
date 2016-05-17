package ru.methuselah.launcher.Downloaders;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import ru.methuselah.launcher.Launcher;

public abstract class BaseUpdater
{
	public static void executeParallelDownloads(Collection<DownloadTask> tasks)
	{
		final List<Thread> threads = new ArrayList<>();
		// Start threads
		for(final DownloadTask task : tasks)
		{
			final Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					Launcher.getInstance().logger.info("Загрузка файла " + task.downloadFrom);
					downloadTask(task);
				}
			};
			threads.add(thread);
			thread.start();
		}
		// Wait for threads
		for(Thread thread : threads)
			try
			{
				thread.join();
			} catch(InterruptedException ex) {
			}
	}
	public static void downloadTask(DownloadTask task)
	{
		downloadFile(task.downloadFrom, task.saveAs, task.showAs);
		if(task.saveAs.isFile())
		{
			if(task.unzipInto != null)
			{
				try
				{
					unZip(task.saveAs, task.unzipInto, task.showAs != null);
				} catch(PrivilegedActionException ex) {
					Launcher.getInstance().logger.error("{}", ex);
				}
				task.saveAs.delete();
			}
		}
	}
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
				Launcher.getInstance().logger.info("Загрузка файла " + saveAs.getName() + "...");
			final ReadableByteChannel rbc = Channels.newChannel(new URL(srcURL).openStream());
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.flush();
		} catch(MalformedURLException ex) {
			Launcher.getInstance().logger.error("{}", ex);
		} catch(IOException ex) {
			Launcher.getInstance().logger.error("{}", ex);
		}
	}
	public static void copyFile(File from, File to)
	{
		try(FileOutputStream fos = new FileOutputStream(to);
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(from)))
		{
			Launcher.getInstance().logger.info("Копирование " + from + " -> " + to);
			final ReadableByteChannel rbc = Channels.newChannel(bis);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.flush();
		} catch(IOException | NullPointerException ex) {
			Launcher.getInstance().logger.error("{}", ex);
		}
	}
	public static void unZip(File fileZip, File destDir, boolean annotate) throws PrivilegedActionException
	{
		if(fileZip.isFile())
		{
			Launcher.getInstance().logger.info("Распаковка: " + fileZip);
			if(annotate)
				Launcher.showGrant("Распаковка " + fileZip.getName() + "...");
			try(ZipFile zf = new ZipFile(fileZip))
			{
				String szExtractPath = fileZip.getParent();
				if(destDir != null)
				{
					destDir.mkdirs();
					szExtractPath = destDir.getAbsolutePath();
				}
				for(ZipEntry zipEntry : Collections.list(zf.entries()))
					extractFromZip(szExtractPath, zipEntry.getName(), zf, zipEntry);
				Launcher.getInstance().logger.error("Удачно!");
				if(annotate)
					Launcher.showGrant("Распаковка " + fileZip.getName() + " завершена");
			} catch(IOException ex) {
				Launcher.getInstance().logger.error("{}", ex);
			} finally {
				fileZip.delete();
			}
		} else
			Launcher.getInstance().logger.error((new StringBuilder()).append("\nNot found: ").append(fileZip).toString());
	}
	private static void extractFromZip(String szExtractPath, String szName, ZipFile zf, ZipEntry ze)
	{
		if(ze.isDirectory())
			return;
		final String targetFileName = szName.replace('\\', File.separatorChar).replace('/', File.separatorChar);
		final File   targetFile     = new File(szExtractPath + File.separator + targetFileName);
		final long   size           = ze.getSize();
		final long   compressedSize = ze.getCompressedSize();
		Launcher.getInstance().logger.info("\tИзвлечение " + targetFile.getName() + " (" + compressedSize + " -> " + size + ")");
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
			Launcher.getInstance().logger.error("{}", ex);
		}
	}
}
