package tk.omerjerk.versionupgrader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
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
			ProgressBar pBar = (ProgressBar) findViewById(R.id.progressBar);
			TextView checkingRoot = (TextView) findViewById(R.id.checkingRoot);
			pBar.setVisibility(View.GONE);
			checkingRoot.setVisibility(View.GONE);
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
	
	public void backupBuildProp(View v){
		try{
			// Preform su to get root privledges
			Process p = Runtime.getRuntime().exec("su");
						
			// Attempt to write a file to a root-only
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			// Remounting /system as read+write
			os.writeBytes("mount -o rw,remount -t yaffs2 /dev/block/mtdblock0 /system\n");
			// Copying file to SD Card
			os.writeBytes("cp -f /system/build.prop " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/build.prop.bak\n");
			os.writeBytes("exit\n");
			os.flush();
			p.waitFor();
			
		} catch (Exception e){
			showToast("Unable to get root access. Please restart the app.");
		}
		
	}
	File tempBuildProp;
	public void getValue(View v){
		final Properties properties = new Properties();
		tempBuildProp = new File(Environment.getExternalStorageDirectory().getPath() + "/build.prop.bak");
		
		try{
			properties.load(new FileInputStream(tempBuildProp));
		} catch (IOException e){
			showToast("Error Occured: " + e);
		}
		
		final String[] pTitle = properties.keySet().toArray(new String[0]);
    	final ArrayList<String> pDesc = new ArrayList<String>();
    	for (int i = 0; i < pTitle.length; i++) {
    		pDesc.add(properties.getProperty(pTitle[i]));
    	}
    	
    	for (int i=0; i <pTitle.length; i++){
    		System.out.println("Property : " + pTitle[i] + "  Value : " + pDesc.get(i));
    	}
	}
	
	public void editValue(View v){
		
		final Properties properties = new Properties();
		try{
			
			FileInputStream in = new FileInputStream(tempBuildProp);
			properties.load(in);
			in.close();
		} catch (IOException e){
			showToast("Error : " + e);
		}
		
		properties.setProperty("ro.build.version.release", "4.3");
		
		try {
			FileOutputStream changedOutput = new FileOutputStream(tempBuildProp);
			properties.store(changedOutput, null);
			changedOutput.close();
			System.out.println("storing properties");
		} catch(IOException e){
			showToast("Error : " + e);
		}
		
	}
	
	public void copyToSystem (View v){
		Process process = null;
        DataOutputStream os = null;
        
        try {
            process = Runtime.getRuntime().exec("su");
	        os = new DataOutputStream(process.getOutputStream());
	        os.writeBytes("mount -o remount,rw -t yaffs2 /dev/block/mtdblock4 /system\n");
	        os.writeBytes("mv -f /system/build.prop /system/build.prop.bak\n");
	        os.writeBytes("busybox cp -f " + tempBuildProp + " /system/build.prop\n");
	        os.writeBytes("chmod 755 /system/build.prop\n");
	        //os.writeBytes("mount -o remount,ro -t yaffs2 /dev/block/mtdblock4 /system\n");
	        //os.writeBytes("rm " + propReplaceFile);
	        //os.writeBytes("rm " + tempFile);
	        os.writeBytes("exit\n");
	        os.flush();
	        process.waitFor();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            	Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
	}

}
