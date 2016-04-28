package ru.methuselah.launcher;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import ru.methuselah.securitylibrary.MethuselahPrivate;

public class Utilities
{
	private static final String[] ACTUAL_JAVA_VERSION  = { "1.7.0", "1.8.0" };
	private static final String[] FUN_CAPTION_SPLASHES = new String[]
	{
		"а давай снимем видео и выложим на Ютуб?!",
		"we are the best of the best of the best!",
		"простите, я опять это сломал. Зато печеньки есть.",
		"позадирали свои Приоры, под ними ходить можно!",
		"модер, модер, парень работящий...",
		"пвп или убежал?",
		"сколько стоит админка?",
		"да, именно, это случайные сплеши.",
		"почему в этом компьютере так пыльно?",
		"нет, я не стал работать быстрее.",
		"не читай меня, слышь!",
		"админ, запривать мне остров!",
		"почему не работает /dupe ???",
	};
	public static String createMainFrameCaption(boolean includeSplash)
	{
		return "VOXILE" + (includeSplash ? " — " + FUN_CAPTION_SPLASHES[new Random().nextInt(FUN_CAPTION_SPLASHES.length)] : "");
	}
 	public static void sleep(int seconds)
	{
		try
		{
			Thread.sleep(1000 * seconds);
		} catch(InterruptedException ex) {
		}
	}
	public static boolean testJavaForUpdate()
	{
		final String version = System.getProperty("java.version");
		for(String actual : ACTUAL_JAVA_VERSION)
			if(version.contains(actual))
				return false;
		return !version.equals(Launcher.getInstance().properties.data.allowAncientJavaVersion);
	}
	public static boolean pingURL(String url, int timeout)
	{
		url = url.replaceFirst("https", "http");
		try
		{
			HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			return 200 <= responseCode && responseCode <= 399;
		} catch(IOException ex) {
			return false;
		}
	}
	public static boolean pingURL(String url)
	{
		return pingURL(url, 1000);
	}
	public static String executePost(String targetURL, String urlParameters)
	{
		if(targetURL.startsWith("https"))
			MethuselahPrivate.hackSSL();
		final boolean doOutput = !(urlParameters == null || "".equals(urlParameters));
		HttpURLConnection connection = null;
		try
		{
			connection = (HttpURLConnection)new URL(targetURL).openConnection();
			connection.setUseCaches(false);
			if(doOutput)
			{
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
				connection.setRequestProperty("Content-Language", "en-US");
			}
			connection.setDoInput(true);
			connection.setDoOutput(doOutput);
			connection.connect();
			if(doOutput)
				try(DataOutputStream wr = new DataOutputStream(connection.getOutputStream()))
				{
					wr.writeBytes(urlParameters);
					wr.flush();
				}
			try(BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream())))
			{
				final StringBuilder response = new StringBuilder();
				for(String line = rd.readLine(); line != null; line = rd.readLine())
					response.append(line).append('\r');
				return response.toString();
			}
		} catch(IOException ex) {
			System.err.println(ex);
			return "NO CONNECTION";
		} finally {
			if(connection != null)
				connection.disconnect();
		}
	}
}
