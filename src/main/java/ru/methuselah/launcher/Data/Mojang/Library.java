package ru.methuselah.launcher.Data.Mojang;

import java.net.URL;
import java.util.Map;

public class Library
{
	public String name;
	public Map<String, String> natives;
	public String url;
	public static class DownloadInfo
	{
		protected URL    url;
		protected String sha1;
		protected int    size;
	}
	public static class LibraryDownloadInfo
	{
		private DownloadInfo artifact;
		private Map<String, DownloadInfo> classifiers;
	}
	public LibraryDownloadInfo downloads;
}
