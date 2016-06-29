package ru.methuselah.launcher.Downloaders;

import java.io.File;

public class LibrariesManager
{
	public static class CachedLibraryInfo implements Comparable<CachedLibraryInfo>
	{
		public String nameGroupId;
		public String nameArtifactId;
		public String nameVersion;
		public long   timestampDownloaded;
		public long   fileSize;
		public String fileHash;
		public String fileUrl;
		@Override
		public int compareTo(CachedLibraryInfo other)
		{
			return Long.compare(this.timestampDownloaded, other.timestampDownloaded);
		}
		public String getFile()
		{
			// TO DO
			return "" + File.pathSeparator + fileHash;
		}
	}
	public LibrariesManager()
	{
	}
}
