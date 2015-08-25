package ru.methuselah.launcher;

import java.io.BufferedInputStream;
import java.io.EOFException;
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

public class BaseUpdater
{
	public static void downloadFile(String srcURL, File saveAs)
	{
		downloadFile(srcURL, saveAs, null);
	}
	public static void downloadFile(String srcURL, File saveAs, String showAs)
	{
		saveAs.getParentFile().mkdirs();
		try(final FileOutputStream fos = new FileOutputStream(saveAs))
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
	public static void downloadFileOLD(String srcURL, File saveAs, String showAs)
	{
		try
		{
			saveAs.getParentFile().mkdirs();
			HttpURLConnection connection = (HttpURLConnection)new URL(srcURL).openConnection();
			connection.setUseCaches(false);
			connection.setDefaultUseCaches(false);
			connection.setRequestProperty("Cache-Control", "no-store,no-cache,max-age=0");
			connection.setRequestProperty("Expires", "0");
			connection.setRequestProperty("Pragma", "no-cache");
			connection.connect();
			final InputStream cis = connection.getInputStream();
			final FileOutputStream fos = new FileOutputStream(saveAs);
			final int currentFileSize = connection.getContentLength();
			final byte[] buffer = new byte[65536];
			for(int totalDataRead, cnt = 0; cnt >= 0; cnt = cis.read(buffer))
			{
				fos.write(buffer, 0, cnt);
				totalDataRead =+ cnt;
				if(showAs != null)
					Launcher.showGrant(String.format("%s, %3.1f%%...", showAs, (totalDataRead * 100.0f / currentFileSize)));
			}
			cis.close();
			fos.close();
			System.out.println("File download complete: " + saveAs);
		} catch(IOException ex) {
			System.err.println(ex);
		}
	}
	public static void unZip(File fileZip, boolean annotate) throws PrivilegedActionException
	{
		if(fileZip.isFile())
		{
			System.out.println("Распаковка: " + fileZip);
			if(annotate)
				Launcher.showGrant("Извлечение " + fileZip.getName() + "...");
			try(final ZipFile zf = new ZipFile(fileZip))
			{
				final String szExtractPath = fileZip.getParent();
				for(ZipEntry zipEntry : Collections.list(zf.entries()))
					extractFromZip(szExtractPath, zipEntry.getName(), zf, zipEntry);
				System.err.println("Удачно!");
				if(annotate)
					Launcher.showGrant("Извлечение " + fileZip.getName() + " завершено");
			} catch(IOException ex) {
				System.err.println(ex.toString());
			} finally {
				fileZip.delete();
			}
		} else
			System.err.println((new StringBuilder()).append("\nNot found: ").append(fileZip).toString());
	}
	public static void extractFromZip(String szExtractPath, String szName, ZipFile zf, ZipEntry ze)
	{
		if(ze.isDirectory())
			return;
		final String szDstName = szName.replace('\\', File.separatorChar).replace('/', File.separatorChar);
		final String szEntryDir = (szDstName.lastIndexOf(File.separator) != -1) ? szDstName.substring(0, szDstName.lastIndexOf(File.separator)) : "";
		long nSize = ze.getSize();
		long nCompressedSize = ze.getCompressedSize();
		System.err.println((new StringBuilder()).append(" ").append(nSize).append(" (").append(nCompressedSize).append(")").toString());
		try
		{
			File newDir = new File((new StringBuilder()).append(szExtractPath).append(File.separator).append(szEntryDir).toString());
			newDir.mkdirs();
			FileOutputStream fos = new FileOutputStream((new StringBuilder()).append(szExtractPath).append(File.separator).append(szDstName).toString());
			InputStream is = zf.getInputStream(ze);
			byte[] buf = new byte[1024];
			do
			{
				int nLength;
				try
				{
					nLength = is.read(buf);
				} catch(EOFException ex) {
					break;
				}
				if(nLength < 0)
					break;
				fos.write(buf, 0, nLength);
			} while(true);
			is.close();
			fos.close();
		} catch(IOException ex) {
			System.err.println(ex.toString());
		}
	}
	protected static void copyFile(File from, File to)
	{
		try
		{
			final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(from));
			final FileOutputStream fos = new FileOutputStream(to);
			byte[] buffer = new byte[65536];
			for(int bytesRead = bis.read(buffer); bytesRead != -1; bytesRead = bis.read(buffer))
				fos.write(buffer, 0, bytesRead);
			fos.flush();
			fos.close();
			System.err.println(from + " -> " + to + ": OK");
		} catch(IOException ex) {
		} catch(NullPointerException ex) {
			System.err.println(ex);
		}
	}
	/*
	public static interface DownloadNotificable
	{
		public abstract void downloadProgress(
			String source,
			int completeBytes,
			int totalBytes
		);
		public abstract void downloadComplete(
			String source,
			String target,
			int totalBytes
		);
	}
	private String downloadResourceFile(String sourceUrl, File saveAs, DownloadNotificable notificable)
	{
		File tempDirectory = new File(resourcesHome, catalogTemp);
		if(tempDirectory.exists() == false)
			if(tempDirectory.mkdirs() == false)
				tempDirectory = new File(resourcesHome);
		Random rnd = new Random();
		File randomFile;
		do
			randomFile = new File(tempDirectory, Integer.toHexString(rnd.nextInt()) + downloadingExtension);
		while(randomFile.exists());
		try
		{
			// Загрузка данных
			final URL downloadFrom = new URL(sourceUrl);
			final HttpURLConnection connection = (HttpURLConnection)downloadFrom.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			final InputStream iis = connection.getInputStream();
			final OutputStream fos = new FileOutputStream(randomFile);
			byte[] buffer = new byte[65536];
			MessageDigest hashCalc = MessageDigest.getInstance("MD5");
			int sourceSize = connection.getContentLength();
			int targetSize = 0;
			for(int bytesRead = iis.read(buffer); bytesRead > 0; bytesRead = iis.read(buffer))
			{
				fos.write(buffer, 0, bytesRead);
				hashCalc.update(buffer, 0, bytesRead);
				targetSize += bytesRead;
				if(notificable != null)
					notificable.downloadProgress(sourceUrl, targetSize, sourceSize);
			}
			fos.flush();
			fos.close();
			iis.close();
			// Нахождение результирующего имени файла
			StringBuilder sb = new StringBuilder();
			for(byte b : hashCalc.digest())
				sb.append(String.format("%02x", b));
			final String sourceFilename = sourceUrl.substring(sourceUrl.lastIndexOf("/"));
			final String sourceExtension = sourceFilename.substring(sourceFilename.indexOf(".")).toLowerCase();
			String targetFilename = sb.toString() + sourceExtension;
			randomFile.renameTo(new File(resourcesHome, targetFilename));
			if(notificable != null)
				notificable.downloadComplete(sourceUrl, targetFilename, sourceSize);
			return targetFilename;
		} catch(IOException ex) {
			System.err.println(ex);
		} catch(NoSuchAlgorithmException ex) {
			System.err.println(ex);
		}
		return null;
	}
	*/
}
