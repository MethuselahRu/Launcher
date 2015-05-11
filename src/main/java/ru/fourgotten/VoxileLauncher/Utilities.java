package ru.fourgotten.VoxileLauncher;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import ru.fourgotten.VoxileSecurity.Methuselah.MethuselahPrivate;

public class Utilities
{
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
		for(String actual : GlobalConfig.actualJavaVersions)
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
