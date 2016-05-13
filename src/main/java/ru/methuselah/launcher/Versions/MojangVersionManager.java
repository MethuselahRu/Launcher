package ru.methuselah.launcher.Versions;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import ru.methuselah.launcher.Data.Mojang.MojangManifestVersion;
import ru.methuselah.launcher.Data.Mojang.MojangVersionDetails;
import ru.methuselah.launcher.Data.Mojang.ReleaseType;
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
	public void loadManifest()
	{
		final Gson       gson = new Gson();
		final JsonParser jp   = new JsonParser();
		try
		{
			final String      result = Utilities.executePost(MANIFEST_URL, null);
			final JsonElement parsed = jp.parse(result);
			this.latest = gson.fromJson(parsed.getAsJsonObject().get("latest"), MojangManifestLatest.class);
			for(MojangManifestVersion version : gson.fromJson(parsed.getAsJsonObject().get("versions"), MojangManifestVersion[].class))
				this.versions.put(version.id, version);
			this.latestSnapshot = versions.get(latest.snapshot);
			this.latestRelease  = versions.get(latest.release);
		} catch(JsonParseException ex) {
		}
		System.out.println();
	}
	public Collection<String> loadVersionDetails(MojangManifestVersion version)
	{
		final HashSet<String> fields = new HashSet<>();
		final Gson       gson = new Gson();
		final JsonParser jp   = new JsonParser();
		try
		{
			final String      result = Utilities.executePost(version.url.toString(), null);
			final JsonElement parsed = jp.parse(result);
			final MojangVersionDetails details = gson.fromJson(result, MojangVersionDetails.class);
			final JsonArray libraries = parsed.getAsJsonObject().get("libraries").getAsJsonArray();
			for(JsonElement lib : libraries)
				for(Map.Entry<String, JsonElement> entry : lib.getAsJsonObject().entrySet())
				{
					fields.add(entry.getKey());
					System.out.println(entry.getKey() + " = " + entry.getValue());
				}
			System.out.println();
		} catch(JsonParseException ex) {
		}
		return fields;
	}
	public void test()
	{
		loadManifest();
		// loadVersionDetails(latestRelease);
		
		final HashSet<String> librariesFields = new HashSet<>();
		
		for(MojangManifestVersion v : versions.values())
			if(v.type == ReleaseType.release)
				librariesFields.addAll(loadVersionDetails(v));
		
		for(String lf : librariesFields)
			System.out.println(lf);
		
		System.exit(0);
	}
}
