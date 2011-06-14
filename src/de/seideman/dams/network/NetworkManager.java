package de.seideman.dams.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkManager {

	private ConnectivityManager con;
	private final String IP="192.168.2.105";
	
	public NetworkManager(ConnectivityManager _con) {
		con = _con;
	}
	
	public boolean tryNetwork() {
		boolean bool = false;
		NetworkInfo inf = con.getActiveNetworkInfo();

		try {
			if (inf.isAvailable() && inf.isConnected()) {
				bool = true;
			}
		} catch (Exception ex) {
			bool = false;
		}

		return bool;

	}
	
	public JSONObject getObjectInfo(int searchType, String searchParam){
		JSONObject json = null;
		
		HttpClient cl = new DefaultHttpClient();
		HttpPost post = new HttpPost("http://"+IP+":8080/DAMS02/api/android/objectinfo");
		
		try {

			ArrayList<NameValuePair> data = new ArrayList<NameValuePair>(2);
			data.add(new BasicNameValuePair("paramType", String.valueOf(searchType)));
			data.add(new BasicNameValuePair("searchParam", searchParam));
			
			post.setEntity(new UrlEncodedFormEntity(data));
		}catch(Exception ex) {
		}
		try{
			HttpResponse resp = cl.execute(post);
			HttpEntity entity = resp.getEntity();

			json = readStream(resp.getEntity().getContent());
			

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	public Boolean tryLogin(String user, String pass) {
		String passHash = makeHash(pass);
		Boolean result = false;
		JSONObject json = null;
		
		HttpClient cl = new DefaultHttpClient();
		HttpPost post = new HttpPost("http://"+IP+":8080/DAMS02/api/android/login");
		
		try {

			ArrayList<NameValuePair> data = new ArrayList<NameValuePair>(1);
			data.add(new BasicNameValuePair("user", user));
			data.add(new BasicNameValuePair("pass", passHash));
			
			post.setEntity(new UrlEncodedFormEntity(data));
		
		
			HttpResponse resp = cl.execute(post);
			HttpEntity entity = resp.getEntity();

			json = readStream(resp.getEntity().getContent());
			result = json.getBoolean("login");

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return result;
	}
	
	private String makeHash(String pass) {
		String passHash=pass;

		StringBuffer hexString = new StringBuffer();
		byte[] bytes;
		
		try{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(pass.getBytes());
						
			BigInteger hash = new BigInteger(1, md5.digest());
			passHash = hash.toString(16);
			
			if ((passHash.length() % 2) !=0){
				passHash = "0" + passHash;
			}
		}
		catch(Exception e){
			passHash = e.getMessage();	
		}

		
		return passHash;
	}
	
	private JSONObject readStream(InputStream in) {
		JSONObject json = null;

		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			StringBuilder builder = new StringBuilder();
			String line = null;

			while ((line = reader.readLine()) != null) {
				builder.append(line + "\n");
			}
			in.close();
			json = new JSONObject(builder.toString());
			json.put("result_read", true);
		} catch (Exception ex) {
			json = new JSONObject();
			try {
				json.put("result_read", false);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return json;
	}
	

	public JSONObject getCableConnection(String cableName) {
		JSONObject json = null;
		
		HttpClient cl = new DefaultHttpClient();
		HttpPost post = new HttpPost("http://"+IP+":8080/DAMS02/api/android/connection");
		
		try {

			ArrayList<NameValuePair> data = new ArrayList<NameValuePair>(1);
			data.add(new BasicNameValuePair("cableName", cableName));
			
			post.setEntity(new UrlEncodedFormEntity(data));
		
		
			HttpResponse resp = cl.execute(post);
			HttpEntity entity = resp.getEntity();

			json = readStream(resp.getEntity().getContent());
			

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return json;
		
	}
}
