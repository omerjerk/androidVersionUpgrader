package tk.omerjerk.versionupgrader;

import java.io.DataOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new rootCheckAsyncTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private boolean checkRoot(){
		Process p;
		try{
			// Preform su to get root privledges
			p = Runtime.getRuntime().exec("su");
			
			// Attempt to write a file to a root-only
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("mount -o rw,remount -t yaffs2 /dev/block/mtdblock0 /system\n");
			os.writeBytes("echo \"Do I have root?\" >/system/etc/temporary.txt\n");
			
			// Close the terminal
			os.writeBytes("exit\n");
			os.flush();
			
			try{
				p.waitFor();
				if(p.exitValue() != 225){
					return true;
				} else {
					return false;
				}
			} catch(InterruptedException e){
				return false;
			}
		} catch(IOException e){
			return false;
		}
	}
	
	// Dirty hack for now in AsyncTask
	public class rootCheckAsyncTask extends AsyncTask <Void, Void, Boolean> {
		protected Boolean doInBackground(Void... voids ){
			boolean result = checkRoot();
			return result;
		}
		
		protected void onPostExecute(Boolean b){
			if(b){
				showToast("ROOTED !");
				//Do rest of work
			} else {
				showToast("No Root!");
				TextView noRoot = (TextView) findViewById(R.id.noRoot);
				noRoot.setVisibility(View.VISIBLE);
			}
		}
	}
	
	private void showToast(String s){
		Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
	}

}
