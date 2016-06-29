package ru.methuselah.launcher;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.methuselah.securitylibrary.MethuselahPrivate;

public class Utilities
{
	private static final Logger logger = LoggerFactory.getLogger(Utilities.class);
	private static final String[] ACTUAL_JAVA_VERSION  = { "1.7.0", "1.8.0" };
	private static final String[] FUN_CAPTION_SPLASHES = new String[]
	{
		"можно мне залить лавой креатив?",
		"The floor is a lava!",
		"а давай снимем видео и выложим на Ютуб?!",
		"we are the best of the best of the best!",
		"простите, я опять это сломал. Зато печеньки есть.",
		"позадирали свои Приоры, под ними ходить можно!",
		"модер, модер, парень работящий ...",
		"пвп или убежал?",
		"сколько стоит админка?",
		"да, именно, это случайные сплеши.",
		"почему в этом компьютере так пыльно?",
		"нет, я не стал работать быстрее.",
		"не читай меня, слышь!",
		"а как купить админку?!?",
		"админ, запривать мне остров!",
		"почему не работает /dupe ???",
		"семь раз отмерь, запривать криво.",
		"ололо, ололо",
		"shut up and take my money!",
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
		return !version.equals(Launcher.getInstance().properties.getData().allowAncientJavaVersion);
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
	public static boolean emptyString(String str)
	{
		return str == null || "".equals(str);
	}
	public static boolean nonEmptyString(String str)
	{
		return str != null && !"".equals(str);
	}
	public static String executePost(String targetURL)
	{
		return executePost(targetURL, null);
	}
	public static final String EXECUTE_POST_NO_CONNECTION = "NO CONNECTION";
	public static String executePost(String targetURL, String urlParameters)
	{
		final boolean doOutput = nonEmptyString(urlParameters);
		HttpURLConnection connection = null;
		try
		{
			connection = (HttpURLConnection)new URL(targetURL).openConnection();
			connection.setUseCaches(false);
			if(doOutput)
			{
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type",     "application/x-www-form-urlencoded");
				connection.setRequestProperty("Content-Length",   Integer.toString(urlParameters.getBytes().length));
				connection.setRequestProperty("Content-Language", "en-US");
			}
			connection.setDoInput(true);
			connection.setDoOutput(doOutput);
			connection.connect();
			// Send output
			if(doOutput)
				try(DataOutputStream wr = new DataOutputStream(connection.getOutputStream()))
				{
					wr.writeBytes(urlParameters);
					wr.flush();
				}
			// Receive input
			final StringBuilder response = new StringBuilder();
			try(BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream())))
			{
				for(String line = rd.readLine(); line != null; line = rd.readLine())
					response.append(line).append("\r\n");
			}
			return response.toString();
		} catch(IOException ex) {
			logger.error("Cannot execute POST request! {}", ex);
			return EXECUTE_POST_NO_CONNECTION;
		} finally {
			if(connection != null)
				connection.disconnect();
		}
	}
}
