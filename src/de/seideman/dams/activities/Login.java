package de.seideman.dams.activities;


import de.seideman.dams.manager.NetworkManager;
import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Login extends Activity implements OnClickListener {

	private Button btnLogin;
	private EditText textUser;
	private EditText textPass;
	
	
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
	}
	
	
	public void onClick(View v) {
		
		if (v.equals(btnLogin)){
			startMainActivity();
		}
		
	}


	private void startMainActivity() {

		if (checkWebService()){
			Intent myIntent = new Intent(this, Dams.class);
			
			if(login()){
				try {
					this.startActivity(myIntent);}
				catch (Exception ex) {
				}
			}else{
				Toast.makeText(this, "Falscher Username oder Passwort!", 10).show();
			}}
		else{
			Toast.makeText(this, "Keine Verbindung zum Server!", 10).show();
			
		}
		
		
	}


	private boolean login() {
		Boolean result = false;
		NetworkManager net = new NetworkManager((ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE));
		
		if (net.tryLogin(textUser.getText().toString(), textPass.getText().toString())) result = true;
				
		return result;
	}


	private boolean checkWebService() {
		Boolean result = false;
		NetworkManager net = new NetworkManager((ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE));
		
		if(net.tryNetwork()) result = true;
		
		return result;
	}
		
	

}
