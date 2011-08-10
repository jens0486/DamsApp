package de.seideman.dams.activities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.seideman.dams.manager.NetworkManager;

public class Dams extends Activity implements OnClickListener,
		OnLongClickListener, OnItemSelectedListener,OnItemClickListener {
	/** Called when the activity is first created. */

	private NetworkManager net;
	private Dialog dia;
	private EditText textSearch;
	
	private Button btnSearch;
	
	private Spinner spin1;
	private Spinner spin2;
	private int spin1Pos;
	private int spin2Pos;
	private String searchTextActValue;
	private ListView listView1;
	private JSONArray actListViewValues;
	private int actListViewState; //0, Server 1,Cable
	private ExpandableListView listView2;
	
	private final String[] SPINNER1 = { "Serverinformation", "Kabelverbindung" };
	private final String[] SPINNER2 = { "InventarNr", "Seriennummer",
			"IP-Adresse", "Hostname", "Mac-Adresse", "freie Suche" };

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Delete the TitleBar on App-Start
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_menu);
	
		listView1 = (ListView) findViewById(R.id.listView1);
		listView1.setOnItemClickListener(this);
			
		
		net = new NetworkManager(
				(ConnectivityManager) this
						.getSystemService(CONNECTIVITY_SERVICE));

		spin1 = (Spinner) findViewById(R.id.spinner1);
		spin1.setOnItemSelectedListener(this);
		fillSpinner(spin1, SPINNER1);
		
		
		spin2 = (Spinner) findViewById(R.id.spinner2);
		spin2.setOnItemSelectedListener(this);
		fillSpinner(spin2, SPINNER2);
		
		textSearch = (EditText) findViewById(R.id.textSearchValue);
		textSearch.setOnLongClickListener(this);
		
		btnSearch = (Button) findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(this);
}
	
	public void onPause() {

        super.onPause();
        
        spin1Pos = spin1.getSelectedItemPosition();
        spin2Pos = spin2.getSelectedItemPosition();
        searchTextActValue = textSearch.getText().toString();

    }

    public void onResume() {

        super.onResume();
       
        Spinner restoreSpin1 = (Spinner)findViewById(R.id.spinner1);
        restoreSpin1.setSelection(getSpin1Pos());
        
        Spinner restoreSpin2 = (Spinner)findViewById(R.id.spinner2);
        restoreSpin2.setSelection(getSpin2Pos());
        
        EditText restoreText = (EditText)findViewById(R.id.textSearchValue);
        restoreText.setText(getSearchActTextValue());
    }
	
	public void onClick(View v) {

	
		if (v.equals(btnSearch)) {
			if(net.tryNetwork()){
				if (textSearch.getText().toString().contentEquals("")) {
					Toast.makeText(this, "Bitte geben Sie ein Suchkriterium ein!",
							10).show();
					textSearch.setBackgroundColor(Color.RED);
				} else {
					textSearch.setBackgroundColor(Color.WHITE);
					listView1.setAdapter(null);
					controlSearch();
				}
			} else{
				
			}
			
		}
	}
	
	private void fillSpinner(Spinner s, String[] array){
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, array);
		s.setAdapter(adapter);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}
	
	private void controlSearch() {
		JSONObject json = new JSONObject();
		
		switch(spin1.getSelectedItemPosition()){
			case 0:
				json = net.getObjectInfo(spin2.getSelectedItemPosition(),textSearch.getText().toString());
				fillListView(json,0);
				break;
			case 1:
				json = net.getCableConnection(textSearch.getText().toString());
				fillListView(json,1);
				break;
		}
	}
	
	private void fillListView(JSONObject json, int sel){
		switch(sel){
		case 0:
			actListViewState = 0;
			try {
				if (json.getBoolean("result")){
					actListViewValues = json.getJSONArray("objects");
					List<String> list = new ArrayList<String>();
								
					if (actListViewValues.length() > 0){
						for (int i = 0;i<actListViewValues.length();i++){
							String host = actListViewValues.getJSONObject(i).getString("hostname");
							String status= actListViewValues.getJSONObject(i).getString("status");
							list.add(host+ " ("+status+")");
						}
						ArrayAdapter<String> ad = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
						listView1.setAdapter(ad);
						
					}
				}else{
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case 1:
			actListViewState = 1;
			try {
				if (json.getBoolean("result")){
					actListViewValues = json.getJSONArray("connections");
					List<String> list = new ArrayList<String>();
								
					if (actListViewValues.length() > 0){
						for (int i = 0;i<actListViewValues.length();i++){
							String cable = actListViewValues.getJSONObject(i).getString("cable");
							list.add(cable);
						}
						ArrayAdapter<String> ad = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
						listView1.setAdapter(ad);
					}
				}else{
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break; 
		}
		
		}
	
	
	private void fillConnectionDialog(JSONObject json,String cable) {
		dia = new Dialog(this);
		dia.setContentView(R.layout.connection_info_dialog);
		dia.setOwnerActivity(this);
		
		TextView textView10 = (TextView)dia.findViewById(R.id.textView10);
		TextView textView11 = (TextView)dia.findViewById(R.id.textView11);
		TextView textView12 = (TextView)dia.findViewById(R.id.textView12);
		TextView textView13 = (TextView)dia.findViewById(R.id.textView13);
		TextView textView14 = (TextView)dia.findViewById(R.id.textView14);
		
		TextView textView20 = (TextView)dia.findViewById(R.id.textView20);
		TextView textView21 = (TextView)dia.findViewById(R.id.textView21);
		TextView textView22 = (TextView)dia.findViewById(R.id.textView22);
		TextView textView23 = (TextView)dia.findViewById(R.id.textView23);
		TextView textView24 = (TextView)dia.findViewById(R.id.textView24);
		
		TextView textView30 = (TextView)dia.findViewById(R.id.textView30);
		
		
		try {
		JSONArray interfaces = json.getJSONArray("interfaces");
		
		switch(interfaces.length()){
			case 0:
				textView30.setText("Kabel "+cable+" nicht gesteckt!");
			case 1:
				JSONObject j11 = interfaces.getJSONObject(0);
				textView10.setText(j11.getString("intHostname"));
				textView11.setText(j11.getString("intName"));
				textView12.setText(j11.getString("intPanel"));
				textView13.setText(j11.getString("intPort"));
				textView14.setText(j11.getString("intObjLocation"));
				
				textView30.setText(cable);
				dia.show();
				break;
			case 2:
				
				JSONObject j1 = interfaces.getJSONObject(0);
				JSONObject j2 = interfaces.getJSONObject(1);
								
				textView10.setText(j1.getString("intHostname"));
				textView11.setText(j1.getString("intName"));
				textView12.setText(j1.getString("intPanel"));
				textView13.setText(j1.getString("intPort"));
				textView14.setText(j1.getString("intObjLocation"));
				
				textView20.setText(j2.getString("intHostname"));
				textView21.setText(j2.getString("intName"));
				textView22.setText(j2.getString("intPanel"));
				textView23.setText(j2.getString("intPort"));
				textView24.setText(j2.getString("intObjLocation"));
				
				textView30.setText(cable);
				dia.show();
				break;
			default:
				Toast.makeText(this, "Fehler bei den Kabelverbindung!",
						10).show();
				
		
		
		}
		
		
			
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
	}

	private void fillObjectDialog(JSONObject json) {
		dia = new Dialog(this);
		dia.setContentView(R.layout.server_info_dialog);
		dia.setOwnerActivity(this);
		

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
				ip.setText("-");
				dia.setTitle("Serverinformation für: \"" + json.getString("hostname") + "\"");
				dia.show();
			} else {
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

		if (v.equals(textSearch)) {
			if (isIntentAvailable(this, zxing)) {
				Toast.makeText(this, "Barcode Scanner ist installiert!", 10)
						.show();
				startActivityForResult(intent, 0);
			}
		}

		return false;
	}

	public void onActivityResult(int i, int result, Intent intent) {

		if (i == 0) {
			if (result == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				// Handle successful scan
				textSearch.setText(contents);
			} else if (result == RESULT_CANCELED) {
				Toast.makeText(this, "Probleme beim Scannen des Barcodes!", 10)
				.show();
			}
		}

	}

	public boolean isIntentAvailable(Context context, String action) {
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
				spin2.setEnabled(true);
			} else {
				spin2.setEnabled(false);
				
			}
		}
		

	}
	 
	public void onItemClick(AdapterView<?> parent, View view, int 
			 position, long id) { 
		JSONObject json = new JSONObject();
		
		if (parent.equals(listView1)){
			switch(actListViewState){
				case 0:
					String s;
					try {
						s = actListViewValues.getJSONObject((int)id).getString("objectId");
						json = net.getObjectInfo(6,s);
						fillObjectDialog(json);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;
				case 1:
					try {
						JSONObject j = actListViewValues.getJSONObject((int)id);
						fillConnectionDialog(j,actListViewValues.getJSONObject((int)id).getString("cable"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
			}
		}
	} 

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	public void setSpin2Pos(int spin2Pos) {
		this.spin2Pos = spin2Pos;
	}

	public int getSpin2Pos() {
		return spin2Pos;
	}

	public void setSpin1Pos(int spin1Pos) {
		this.spin1Pos = spin1Pos;
	}

	public int getSpin1Pos() {
		return spin1Pos;
	}

	public void setSearchTextValue(String searchTextActValue) {
		this.searchTextActValue = searchTextActValue;
	}

	public String getSearchActTextValue() {
		return searchTextActValue;
	}
	public NetworkManager getNet() {
		return net;
	}

	public void setNet(NetworkManager net) {
		this.net = net;
	}
}