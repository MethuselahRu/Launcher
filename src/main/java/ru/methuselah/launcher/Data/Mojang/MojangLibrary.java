package ru.methuselah.launcher.Data.Mojang;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MojangLibrary
{
	// name
	// downloads
	// natives
	// rules
	// extract
	public String name;
	public transient String name_groupId;
	public transient String name_artifactId;
	public transient String name_version;
	public void splitName()
	{
		final String[] splitted = name.split("\\:");
		this.name_groupId    = splitted[0];
		this.name_artifactId = splitted[1];
		this.name_version    = splitted[2];
	}
	public static class LibraryDownloads
	{
		public static class DownloadArtifact
		{
			protected URL    url;
			protected String path;
			protected String sha1;
			protected long   size;
		}
		private DownloadArtifact    artifact;
		private Map<String, Object> classifiers = new HashMap<>();
	}
	public LibraryDownloads    downloads;
	public Map<String, String> natives = new HashMap<>();
	public static class LibraryExtract
	{
		public String[] exclude;
	}
	public LibraryExtract extract;
}
