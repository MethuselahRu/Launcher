package ru.fourgotten.VoxileLauncher.Stability;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class VoxileReporting
{
	private final Exception thrown;
	private final ArrayList<String> report = new ArrayList<>();
	public VoxileReporting(GameStartFailedException ex)
	{
		thrown = ex;
	}
	public void report()
	{
		// Собрать общую информацию о падении
		gatherGenericInfo();
		// Описать произошедшее исключение
		for(StackTraceElement element : thrown.getStackTrace())
		{
			String text = element.toString();
		}
		// Сохранить в локальный файл
		// Оправить к нам
	}
	private void gatherGenericInfo()
	{
	}
	private void compressReport(ArrayList<String> report, OutputStream os)
	{
		try(ZipOutputStream zos = new ZipOutputStream(os))
		{
			zos.putNextEntry(new ZipEntry("report.txt"));
			try(FileInputStream in = new FileInputStream("C:\\spy.log"))
			{
				int len;
				byte[] buffer = new byte[1024];
				while((len = in.read(buffer)) > 0)
					zos.write(buffer, 0, len);
			}
			zos.closeEntry();
		} catch(IOException ex) {
		}
		try
		{
			final ZipOutputStream zos = new ZipOutputStream(os);
			final ZipEntry entry = new ZipEntry("report.txt");
			zos.putNextEntry(entry);
			zos.closeEntry();
		} catch(IOException ex) {
			Logger.getLogger(VoxileReporting.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
