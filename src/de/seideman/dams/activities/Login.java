package de.seideman.dams.activities;


import de.seideman.dams.exceptions.EmptyInputException;
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
import android.widget.Spinner;
import android.widget.Toast;


public class Login extends Activity implements OnClickListener {

	private Button btnLogin;
	private EditText textUser;
	private String userActValue="";
	private EditText textPass;
	private String passActValue="";
	private NetworkManager net;
	private ConnectivityManager con;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Delete the TitleBar on App-Start
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		
		this.setCon((ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE));
		this.setNet(new NetworkManager(getCon()));
		
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(this);
		

		textUser = (EditText) findViewById(R.id.textUser);
		textUser.setText("");
		textUser.setOnClickListener(this);
		
		
		textPass = (EditText) findViewById(R.id.textPass);
		textPass.setText("");
		textPass.setOnClickListener(this);
		
	}

	public void onPause() {

        super.onPause();
        
        userActValue = textUser.getText().toString();
        passActValue = textPass.getText().toString();
       

    }

	public void onResume() {

        super.onResume();
       
        EditText restoreUser = (EditText)findViewById(R.id.textUser);
        restoreUser.setText(userActValue);
        
        EditText restorePass = (EditText)findViewById(R.id.textPass);
        restorePass.setText("");
           
    }

	public void onClick(View v) {
		
		if (v.equals(btnLogin)){
			
				try {
					startMainActivity();
				} catch (NullPointerException e) {
					Toast.makeText(this, "NetworkManager ist NULL!", 10).show();
					
				} catch (EmptyInputException e) {
					Toast.makeText(this, "Fehlende Benutzer-Eingaben!", 10).show();}
				}
			
		}
		
	public void startMainActivity() throws NullPointerException,EmptyInputException {
		String user = textUser.getText().toString();
		String pass = textPass.getText().toString();
	
		setUserActValue(user);
		setPassActValue(pass);
		
		if (checkWebService()){
			Intent myIntent = new Intent(this, Dams.class);
			 if(login(user,pass)){
					try {
						this.startActivity(myIntent);}
					catch (Exception ex) {
					}
				}else{
					Toast.makeText(this, "Falscher Username oder Passwort!", 10).show();}
		}
		else{
			Toast.makeText(this, "Keine Verbindung zum Server!", 10).show();
			
		}
		
		
	}

	private boolean login(String user, String pass) throws NullPointerException, EmptyInputException{
		Boolean result = false;
				
		if (!(this.net==null)){
			if (user.equals("") || pass.equals("")){
				throw new EmptyInputException("Fehlende Eingaben!");}
			else{
			result = net.tryLogin(user, pass);}
		}
		else{
			throw new NullPointerException("NetworkManager is NULL");
		}
	return result;
	}

	private boolean checkWebService()throws NullPointerException {
		Boolean result = false;
		
		if (!(this.net==null)){
			if(net.tryNetwork()) result = true;}
		else{
			throw new NullPointerException("NetworkManager is NULL");
		}
		return result;
	}

	public void setNet(NetworkManager net) {
		this.net = net;
	}

	public NetworkManager getNet() {
		return net;
	}

	public void setCon(ConnectivityManager con) {
		this.con = con;
	}

	public ConnectivityManager getCon() {
		return con;
	}

	public void setUserActValue(String userActValue) {
		this.userActValue = userActValue;
	}

	public String getUserActValue() {
		return userActValue;
	}

	public void setPassActValue(String passActValue) {
		this.passActValue = passActValue;
	}

	public String getPassActValue() {
		return passActValue;
	}
		
	

}
