package com.gpack.pay.paylib.util;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// Http request class
public class HttpUtils
{

	private static final int TIMEOUT_IN_MILLIONS = 5000;

	public interface CallBack
	{
		void onRequestComplete(String result);
	}


	/**
	 * synchronize Get request
	 * 
	 * @param urlStr
	 * @param callBack
	 */
	public static void doGetAsyn(final String urlStr, final CallBack callBack)
	{
		new Thread()
		{
			public void run()
			{
				try
				{
					String result = doGet(urlStr);
					if (callBack != null)
					{
						callBack.onRequestComplete(result);
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}

			};
		}.start();
	}

	/**
	 * synchronize Post request
	 * @param urlStr
	 * @param params
	 * @param callBack
	 * @throws Exception
	 */
	public static void doPostAsyn(final String urlStr, final String params,
			final CallBack callBack) throws Exception
	{
		new Thread()
		{
			public void run()
			{
				try
				{
					String result = doPost(urlStr, params);
					if (callBack != null)
					{
						callBack.onRequestComplete(result);
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}

			};
		}.start();

	}


	/**
	 * Get request，get data
	 * 
	 * @param urlStr
	 * @return
	 * @throws Exception
	 */
	public static String doGet(String urlStr) {
		return  doGet(urlStr, "");
	}

	/**
	 * Get request，get data
	 *
	 * @param urlStr
	 * @return
	 * @throws Exception
	 */
	public static String doGet(String urlStr, String token)
	{
		HashMap<String, String> map = null;
		if (!TextUtils.isEmpty(token)) {
			map = new HashMap<>();
			map.put("Authorization","Bearer "+ token);
		}
		return doGet(urlStr, map);
	}

	public static String doGet(String urlStr, HashMap<String, String> mapRequestProperty)
	{
		URL url = null;
		HttpURLConnection conn = null;
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		try
		{
			url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(TIMEOUT_IN_MILLIONS);
			conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			if (mapRequestProperty != null && mapRequestProperty.size() > 0) {
				Iterator iterator = mapRequestProperty.entrySet().iterator();
				while(iterator.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator.next();
					String key = (String) entry.getKey();
					String value = (String) entry.getValue();
					conn.setRequestProperty(key, value);
				}
			}

			if (conn.getResponseCode() == 200)
			{
				is = conn.getInputStream();
				baos = new ByteArrayOutputStream();
				int len = -1;
				byte[] buf = new byte[128];

				while ((len = is.read(buf)) != -1)
				{
					baos.write(buf, 0, len);
				}
				baos.flush();

//				return baos.toString();

				// delete \r \n of the string head and tail
				// add on 2016年2月18日18:03:47 by-KyleCe
				return baos.toString() != null ? baos.toString().trim() : null;

			} else
			{
				throw new RuntimeException(" responseCode is not 200 ... ");
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (is != null)
					is.close();
			} catch (IOException e)
			{
			}
			try
			{
				if (baos != null)
					baos.close();
			} catch (IOException e)
			{
			}
			if (conn !=null)
				conn.disconnect();
		}

		return null ;

	}

	/**
	 * Get request，get data
	 *
	 * @param urlStr
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String> doGet(String urlStr,String token,int action)
	{
		ArrayList<String> result = new ArrayList<String>();
		URL url = null;
		HttpURLConnection conn = null;
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		try
		{
			url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(TIMEOUT_IN_MILLIONS);
			conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("Authorization","Bearer "+token);
			if (conn.getResponseCode() == 200)
			{
				System.out.println("header = " + conn.getHeaderFields());
				System.out.println("header = " + "X-Pagination-Current-Page = "+conn.getHeaderField("X-Pagination-Current-Page"));
				result.add(conn.getHeaderField("X-Pagination-Current-Page"));
				result.add(conn.getHeaderField("X-Pagination-Page-Count"));
				result.add(conn.getHeaderField("X-Pagination-Per-Page"));
				result.add(conn.getHeaderField("X-Pagination-Total-Count"));
				is = conn.getInputStream();
				baos = new ByteArrayOutputStream();
				int len = -1;
				byte[] buf = new byte[128];

				while ((len = is.read(buf)) != -1)
				{
					baos.write(buf, 0, len);
				}
				baos.flush();
				result.add(baos.toString());
				return result;
			} else
			{
				System.out.println("code = " + conn.getResponseCode());
				throw new RuntimeException(" responseCode is not 200 ... ");
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (is != null)
					is.close();
			} catch (IOException e)
			{
			}
			try
			{
				if (baos != null)
					baos.close();
			} catch (IOException e)
			{
			}
			conn.disconnect();
		}

		return null ;

	}
	public static String doPost(String url, String param) {
		return doPost(url, param, null);
	}

	public static String doPost(String url, String param, HashMap<String,String> requestProperty)
	{
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try
		{
			URL realUrl = new URL(url);
			// open connection with URL
			HttpURLConnection conn = (HttpURLConnection) realUrl
					.openConnection();
			// set common connection
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			if (requestProperty != null && requestProperty.size() > 0) {
				Iterator iterator = requestProperty.entrySet().iterator();
				while(iterator.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator.next();
					String key = (String) entry.getKey();
					String value = (String) entry.getValue();
					conn.setRequestProperty(key, value);
				}
			}
			//conn.setRequestProperty("Authorization", "Bearer " + SPUtils.get(mContext, CData.TOKEN, "unknown"));

			conn.setUseCaches(false);
			// send POST must setting as below
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setReadTimeout(TIMEOUT_IN_MILLIONS);
			conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);

			if (param != null && !param.trim().equals(""))
			{
				// get URLConnection outputStream
				out = new PrintWriter(conn.getOutputStream());
				// send parameter
				out.print(param);
				// flush outputStream buffer
				out.flush();
			}
			// modify BufferedReader URL response
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null)
			{
				result += line;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		// finally close stream
		finally
		{
			try
			{
				if (out != null)
				{
					out.close();
				}
				if (in != null)
				{
					in.close();
				}
			} catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		return result;
	}


	public static String doPost(String url, HashMap<String,String> params, HashMap<String,String> requestProperty) {
		String param = null;
		if (params != null && params.size() > 0) {
			param = "";
			Iterator iterator = params.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				param += "&" + key + "=" + value;
			}
		}
		if (param != null && param.length() > 0) {
			param = param.substring(1);
		}
		System.out.println("param = " + param);
		return doPost(url, param, requestProperty);
	}

	public static String doPut(String url, HashMap<String,String> params, HashMap<String,String> requestProperty) {
		String param = null;
		if (params != null && params.size() > 0) {
			param = "";
			Iterator iterator = params.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				param += "&" + key + "=" + value;
			}
		}
		if (param != null && param.length() > 0) {
			param = param.substring(1);
		}
		System.out.println("param = " + param);
		return doPut(url, param, requestProperty);
	}

	public static String doPut(String url, String param, HashMap<String,String> requestProperty) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try
		{
			URL realUrl = new URL(url);
			// open connection with URL
			HttpURLConnection conn = (HttpURLConnection) realUrl
					.openConnection();
			// set common connection
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			if (requestProperty != null && requestProperty.size() > 0) {
				Iterator iterator = requestProperty.entrySet().iterator();
				while(iterator.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator.next();
					String key = (String) entry.getKey();
					String value = (String) entry.getValue();
					conn.setRequestProperty(key, value);
				}
			}
			//conn.setRequestProperty("Authorization", "Bearer " + SPUtils.get(mContext, CData.TOKEN, "unknown"));

			conn.setUseCaches(false);
			// send POST must setting as below
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setReadTimeout(TIMEOUT_IN_MILLIONS);
			conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);

			if (param != null && !param.trim().equals(""))
			{
				// get URLConnection outputStream
				out = new PrintWriter(conn.getOutputStream());
				// send parameter
				out.print(param);
				// flush outputStream buffer
				out.flush();
			}
			// modify BufferedReader URL response
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null)
			{
				result += line;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		// finally close stream
		finally
		{
			try
			{
				if (out != null)
				{
					out.close();
				}
				if (in != null)
				{
					in.close();
				}
			} catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		return result;
	}
}
