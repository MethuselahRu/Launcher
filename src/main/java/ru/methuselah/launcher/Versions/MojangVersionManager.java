package ru.methuselah.launcher.Versions;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import ru.methuselah.launcher.Launcher;
import ru.methuselah.launcher.Utilities;

public class MojangVersionManager
{
	public static final String MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
	public static class MojangManifestLatest
	{
		public String snapshot;
		public String release;
	}
	public MojangManifestLatest  latest;
	public MojangManifestVersion latestSnapshot;
	public MojangManifestVersion latestRelease;
	public LinkedHashMap<String, MojangManifestVersion> versions = new LinkedHashMap<>();
	public MojangVersionManager()
	{
	}
	public void loadManifest() throws IOException
	{
		final Gson       gson = new Gson();
		final JsonParser jp   = new JsonParser();
		try
		{
			final String      result = Utilities.executePost(MANIFEST_URL);
			final JsonElement parsed = jp.parse(result);
			this.latest = gson.fromJson(parsed.getAsJsonObject().get("latest"), MojangManifestLatest.class);
			for(MojangManifestVersion version : gson.fromJson(parsed.getAsJsonObject().get("versions"), MojangManifestVersion[].class))
				this.versions.put(version.id, version);
			this.latestSnapshot = versions.get(latest.snapshot);
			this.latestRelease  = versions.get(latest.release);
		} catch(JsonParseException ex) {
			throw new IOException("Не удалось загрузить список версий Minecraft!", ex);
		}
	}
	public boolean isManifestLoaded()
	{
		return this.latest != null;
	}
	public Collection<String> loadVersionDetails(MojangManifestVersion version)
	{
		final HashSet<String> fields = new HashSet<>();
		final Gson       gson = new Gson();
		final JsonParser jp   = new JsonParser();
		try
		{
			final String      result = Utilities.executePost(version.url.toString());
			final JsonElement parsed = jp.parse(result);
			final MojangVersionDetails details = gson.fromJson(result, MojangVersionDetails.class);
			final JsonArray libraries = parsed.getAsJsonObject().get("libraries").getAsJsonArray();
			for(JsonElement lib : libraries)
				for(Map.Entry<String, JsonElement> entry : lib.getAsJsonObject().entrySet())
				{
					fields.add(entry.getKey());
					Launcher.getInstance().logger.info(entry.getKey() + " = " + entry.getValue());
				}
		} catch(JsonParseException ex) {
		}
		return fields;
	}
	public void test()
	{
		try
		{
			loadManifest();
			loadVersionDetails(latestRelease);
			
			/*
			final HashSet<String> librariesFields = new HashSet<>();

			for(MojangManifestVersion v : versions.values())
				if(v.type == ReleaseType.release)
					librariesFields.addAll(loadVersionDetails(v));

			for(String lf : librariesFields)
				Launcher.getInstance().logger.info(lf);
			*/
			
		} catch(IOException ex) {
			Launcher.getInstance().logger.info("Произошла ошибка: ", ex);
		}
		System.exit(0);
	}
}
