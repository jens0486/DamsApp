package de.seideman.dams;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.seideman.dams.R;
import de.seideman.dams.network.NetworkManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Dams extends Activity implements OnClickListener,
		OnLongClickListener, OnItemSelectedListener {
	/** Called when the activity is first created. */

	private NetworkManager net;
	private Dialog dia;
	private EditText textSearchValue;
	private Button btnLogin;
	private Button btnSearch;
	private EditText textUser;
	private EditText textPass;
	private Spinner spin1;
	private Spinner spin2;
	private ExpandableListView listView1;
	private ExpandableListView listView2;
	private final String[] SPINNER1 = { "Serverinformation", "Kabelverbindung" };
	private final String[] SPINNER2 = { "InventarNr", "Seriennummer",
			"IP-Adresse", "Hostname", "Mac-Adresse", "freie Suche" };

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Delete the TitleBar on App-Start
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);

		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(this);

		textUser = (EditText) findViewById(R.id.textUser);
		textUser.setOnClickListener(this);
		textPass = (EditText) findViewById(R.id.textPass);
		textPass.setOnClickListener(this);

		net = new NetworkManager(
				(ConnectivityManager) this
						.getSystemService(CONNECTIVITY_SERVICE));

		// ueberprueft ob eine Netzwerkverbindung besteht, ansonsten wird
		// Programm beendet

		if (!net.tryNetwork()) {
			Toast.makeText(this,
					"Bitte stellen sie eine Netzwerkverbindung her!!!", 10)
					.show();
			this.finish();
		} else {
			Toast.makeText(this, "Netzwerkverbindung hergestellt", 10).show();
		}

	}

	public void onClick(View v) {

		// Login and initiate
		if (v.equals(btnLogin)) {
			String user = textUser.getText().toString();
			String pass = textPass.getText().toString();

			if (user.equals("") || user.equals("")) {
				Toast.makeText(this, "Username oder Passwort nicht eigegeben!",
						10).show();
			} else {
				
				if (net.tryLogin(user, pass)) {
					this.setContentView(R.layout.main_menu);
					textSearchValue = (EditText) findViewById(R.id.textSearchValue);
					textSearchValue.setOnLongClickListener(this);
					Toast.makeText(this, "Eingeloggt: " + user, 10).show();

					// initiate spin1
					spin1 = (Spinner) findViewById(R.id.spinner1);
					spin1.setOnItemSelectedListener(this);
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
							android.R.layout.simple_spinner_item, SPINNER1);
					spin1.setAdapter(adapter);

					// initiate spin2
					spin2 = (Spinner) findViewById(R.id.spinner2);
					// inititate btnSearch
					btnSearch = (Button) findViewById(R.id.btnSearch);
					btnSearch.setOnClickListener(this);
				} else {
					//textUser.setText("");
					//textPass.setText("");
					Toast.makeText(this, "Username oder Passwort ist falsch",
							10).show();
				}
			}

		}
		if (v.equals(btnSearch)) {
			if (textSearchValue.getText().toString().contentEquals("")) {
				Toast.makeText(this, "Bitte geben Sie ein Suchkriterium ein!",
						10).show();
				textSearchValue.setBackgroundColor(Color.RED);
			} else {
				controlSearch();
			}
		}
	}

	private void controlSearch() {
		JSONObject json = new JSONObject();
		if (spin1.getSelectedItemPosition() == 0) {
			json = net.getObjectInfo(spin2.getSelectedItemPosition(),
					textSearchValue.getText().toString());
			fillObjectDialog(json, textSearchValue.getText().toString());

		}
		if (spin1.getSelectedItemPosition() == 1) {
			json = net.getCableConnection(textSearchValue.getText().toString());
			fillConnectionDialog(json);
			//Toast.makeText(this, json.toString(), 10).show();
		}

	}
	
	
	
	private void fillConnectionDialog(JSONObject json) {
		listView1 = (ExpandableListView) findViewById(R.id.listView1);
		listView2 = (ExpandableListView) findViewById(R.id.listView2);
		ArrayList<JSONObject> connections = new ArrayList<JSONObject>();
		ArrayList<String[]> endConnections = new ArrayList<String[]>();
		ArrayList<String[]> betConnections = new ArrayList<String[]>();
		
		
		try {
			JSONArray list = json.getJSONArray("connections");
			
			for (int i=0;i < list.length();i++){
				JSONObject r = list.getJSONObject(i);
				
				if(!r.getString("cable").contains("I-")){
					connections.add(r);
				}			
			}
			
			for (JSONObject j:connections){
				JSONArray  interfaces = j.getJSONArray("interfaces");
				dia = new Dialog(this);
				dia.setContentView(R.layout.connection_info_dialog);				
				String[] con = new String[15];
				
				switch(interfaces.length()){
				
				case 0:
					con[0] = "Kein Interfaces für das Kabel "+j.getString("cable")+" gefunden.";
				case 1:
					con[0] = j.getString("cable");
					//interface1
					con[0]= interfaces.getJSONObject(0).getString("intPanel");
					con[1]= interfaces.getJSONObject(0).getString("intPort");
					con[2]= interfaces.getJSONObject(0).getString("intName");
					//Object-Data, which includes the Interface
					
					
					//json object auswerten???? WS umschreiben!!!!!!
					JSONObject js = (JSONObject)interfaces.getString("intObject");
					con[3]= interfaces.get("intObject").getString("hostname");
					con[4]= interfaces.getJSONObject(0).getString("location");
					con[5]= interfaces.getJSONObject(0).getString("type");
					endConnections.add(con);					
				case 2:
					con[0] = j.getString("cable");
					//interface1
					con[0]= interfaces.getJSONObject(0).getString("intPanel");
					con[1]= interfaces.getJSONObject(0).getString("intPort");
					con[2]= interfaces.getJSONObject(0).getString("intName");
					//Object-Data, which includes the Interface1
					con[3]= interfaces.getJSONObject(0).getString("hostname");
					con[4]= interfaces.getJSONObject(0).getString("location");
					con[5]= interfaces.getJSONObject(0).getString("type");
					//interface2
					con[6]= interfaces.getJSONObject(0).getString("intPanel");
					con[7]= interfaces.getJSONObject(0).getString("intPort");
					con[8]= interfaces.getJSONObject(0).getString("intName");
					//Object-Data, which includes the Interface2
					con[9]= interfaces.getJSONObject(0).getString("hostname");
					con[10]= interfaces.getJSONObject(0).getString("location");
					con[11]= interfaces.getJSONObject(0).getString("type");
					
					if(con[5].equals("Patchpanel") && con[11].equals("Patchpanel")){
						betConnections.add(con);}
					else{
						endConnections.add(con);
						
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		switch(endConnections.size()){
		case 1:
			listView1.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , endConnections.get(0)));
			Toast.makeText(this, betConnections.get(0).toString(), 10)
			.show();
		case 2:
			Toast.makeText(this, betConnections.get(0).toString(), 10)
			.show();
			listView1.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , endConnections.get(0)));
			listView2.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_2 , endConnections.get(1)));
		}
	
		
		dia.show();
	}

	private void fillObjectDialog(JSONObject json, String searchParam) {
		dia = new Dialog(this);
		dia.setContentView(R.layout.server_info_dialog);
		dia.setOwnerActivity(this);
		dia.setTitle("Serverinformation für: \"" + searchParam + "\"");

		TextView inventar = (TextView) dia.findViewById(R.id.textInventar);
		TextView hostname = (TextView) dia.findViewById(R.id.textHostname);
		TextView ip = (TextView) dia.findViewById(R.id.textIP);
		TextView location = (TextView) dia.findViewById(R.id.textLocation);
		TextView manu = (TextView) dia.findViewById(R.id.textManufactor);
		TextView modell = (TextView) dia.findViewById(R.id.textModell);
		TextView rack = (TextView) dia.findViewById(R.id.textRack);
		TextView serial = (TextView) dia.findViewById(R.id.textSerial);
		TextView status = (TextView) dia.findViewById(R.id.textStatus);
		TextView type = (TextView) dia.findViewById(R.id.textTyp);
		TextView height = (TextView) dia.findViewById(R.id.textHeight);

		try {

			if (json.getBoolean("result")) {
				hostname.setText(json.getString("hostname"));
				height.setText(json.getString("height"));
				inventar.setText(json.getString("inventory"));
				location.setText(json.getString("location"));
				rack.setText(json.getString("locationRack"));
				manu.setText(json.getString("manufacturer"));
				modell.setText(json.getString("modell"));
				serial.setText(json.getString("serial"));
				status.setText(json.getString("status"));
				type.setText(json.getString("type"));
				ip.setText("hier folgt die IP");
				dia.show();
			} else {
				Toast.makeText(this, "Erg: " + json.getString("result"), 10)
						.show();
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(json.getString("failure"))
						.setCancelable(true)
						.setNegativeButton("Schliessen",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				;
				AlertDialog alert = builder.create();

				alert.show();
			}
		} catch (JSONException e) {

			e.printStackTrace();
		}

	}

	public boolean onLongClick(View v) {

		String pack = "com.google.zxing.client.android";
		String zxing = "com.google.zxing.client.android.SCAN";
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "ONE_D_MODE");

		if (v.equals(textSearchValue)) {
			if (isIntentAvailable(this, zxing)) {
				Toast.makeText(this, "Barcode Scanner ist installiert!", 10)
						.show();
				startActivityForResult(intent, 0);
			}
		}

		return false;
	}

	public void onActivityResult(int i, int u, Intent intent) {

		Toast.makeText(this, "Return!", 10).show();
		if (i == 0) {
			if (u == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				// Handle successful scan
				textSearchValue.setText(contents);
			} else if (u == RESULT_CANCELED) {
				// handle cancel
			}
		}

	}

	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {

		if (arg0.equals(spin1)) {
			if (arg3 == 0) {
				ArrayAdapter adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_spinner_item, SPINNER2);
				spin2.setAdapter(adapter);
				spin2.setEnabled(true);
			} else {
				spin2.setEnabled(false);
			}
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}