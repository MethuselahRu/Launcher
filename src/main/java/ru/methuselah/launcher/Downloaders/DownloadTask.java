package ru.methuselah.launcher.Downloaders;

import java.io.File;

public class DownloadTask
{
	public String downloadFrom;
	public String showAs;
	public File   saveAs;
	public File   unzipInto;
	public DownloadTask(String downloadFrom, String showAs, File saveAs, File unzipInto)
	{
		this.downloadFrom = downloadFrom;
		this.showAs       = showAs;
		this.saveAs       = saveAs;
		this.unzipInto    = unzipInto;
	}
	public DownloadTask(String downloadFrom, String showAs, File saveAs)
	{
		this.downloadFrom = downloadFrom;
		this.showAs       = showAs;
		this.saveAs       = saveAs;
		this.unzipInto    = null;
	}
}
