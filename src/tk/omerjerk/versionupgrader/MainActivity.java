package tk.omerjerk.versionupgrader;

import java.io.DataOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void update(View v){
		Process p;
		try{
			// Preform su to get root privledges
			p = Runtime.getRuntime().exec("su");
			
			// Attempt to write a file to a root-only
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("echo \"Do I have root?\" >/system/etc/temporary.txt\n");
			
			// Close the terminal
			os.writeBytes("exit\n");
			os.flush();
			
			try{
				p.waitFor();
				if(p.exitValue() != 225){
					showToast("ROOTED !");
				} else {
					showToast("not root");
				}
			} catch(InterruptedException e){
				showToast("not root");
			}
		} catch(IOException e){
			showToast("not root");
		}
	}
	
	private void showToast(String s){
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

}
