package tk.omerjerk.versionupgrader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	
	String versionValue;
	File tempBuildProp;
	EditText versionEditor, modelEditor, buildEditor, buildDateEditor;
	boolean fix1,fix2;
	TextView noRootText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		versionEditor = (EditText) findViewById(R.id.versionEditor);
		modelEditor = (EditText) findViewById(R.id.modelEditor);
		buildEditor = (EditText) findViewById(R.id.buildEditor);
		buildDateEditor = (EditText) findViewById(R.id.buildDateEditor);
		TextView header = (TextView) findViewById(R.id.header);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Chantelli_Antiqua.ttf");  
        header.setTypeface(font);
        noRootText = (TextView) findViewById(R.id.noRoot);
        noRootText.setTypeface(font);
		if(savedInstanceState == null){
			new rootCheckAsyncTask().execute();
		} else {
			ProgressBar pBar = (ProgressBar) findViewById(R.id.progressBar);
			TextView checkingRoot = (TextView) findViewById(R.id.checkingRoot);
			pBar.setVisibility(View.GONE);
			checkingRoot.setVisibility(View.GONE);
			LinearLayout mainContent = (LinearLayout) findViewById(R.id.mainContent);
			mainContent.setVisibility(View.VISIBLE);
			fillValues();
		}		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.settings_about:
	            DialogFragment aboutD = new aboutDialog();
	            aboutD.show(getSupportFragmentManager(), "ABOUT_DIALOG");
	            return true;
	        case R.id.settings_restore:
	        	restore();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private static boolean checkRoot(){
		Process p;
		try{
			// Preform su to get root privledges
			p = Runtime.getRuntime().exec("su");
			
			// Attempt to write a file to a root-only
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("mount -o rw,remount /system\n");
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
				LinearLayout mainContent = (LinearLayout) findViewById(R.id.mainContent);
				mainContent.setVisibility(View.VISIBLE);
				fillValues();
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
	
	private void backupBuildProp(){
		try{
			// Preform su to get root privledges
			Process p = Runtime.getRuntime().exec("su");
						
			// Attempt to write a file to a root-only
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			// Remounting /system as read+write
			os.writeBytes("mount -o rw,remount /system\n");
			// Copying file to SD Card
			os.writeBytes("cp -f /system/build.prop " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/build.prop.bak\n");
			os.writeBytes("cp -f /system/build.prop " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/build.prop.temp\n");
			os.writeBytes("exit\n");
			os.flush();
			p.waitFor();
			
		} catch (Exception e){
			showToast("Unable to get root access. Please restart the app.");
		}
		
	}
	
	private void fillValues(){
		
		backupBuildProp();
		final Properties properties = new Properties();
		tempBuildProp = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/build.prop.temp");
		
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
    		//System.out.println("Property : " + pTitle[i] + "  Value : " + pDesc.get(i));
    		if (pTitle[i].equals("ro.build.version.release")){
    			versionEditor.setText(pDesc.get(i));
    		}
    		if (pTitle[i].equals("ro.product.model")){
    			modelEditor.setText(pDesc.get(i));
    		}
    		if (pTitle[i].equals("ro.build.display.id")){
    			buildEditor.setText(pDesc.get(i));
    			fix1 = true;
    		}
    		if (pTitle[i].equals("ro.build.date")){
    			buildDateEditor.setText(pDesc.get(i));
    			fix2 = true;
    		}
    	}
    	if (fix1 == false){
    		buildEditor.setText("This field is not supported in your ROM.");
    		buildEditor.setKeyListener(null);
    	}
    	if (fix2 == false){
    		buildDateEditor.setText("This field is not supported in your ROM.");
    		buildDateEditor.setKeyListener(null);
    	}
	}
	
	public void commit(View v){
		
		final Properties properties = new Properties();
		try{
			
			FileInputStream in = new FileInputStream(tempBuildProp);
			properties.load(in);
			in.close();
		} catch (IOException e){
			showToast("Error : " + e);
		}
		
		properties.setProperty("ro.build.version.release", versionEditor.getText().toString());
		properties.setProperty("ro.product.model", modelEditor.getText().toString());
		if (fix1 == true){
			properties.setProperty("ro.build.display.id", buildEditor.getText().toString());
		}
		if (fix2 == true){
			properties.setProperty("ro.build.date", buildDateEditor.getText().toString());
		}
		
		try {
			FileOutputStream changedOutput = new FileOutputStream(tempBuildProp);
			properties.store(changedOutput, null);
			changedOutput.close();
			
			copyToSystem();
		} catch(IOException e){
			showToast("Error : " + e);
		}
		
	}
	
	private void copyToSystem (){
		Process process = null;
        DataOutputStream os = null;
        
        try {
            process = Runtime.getRuntime().exec("su");
	        os = new DataOutputStream(process.getOutputStream());
	        os.writeBytes("mount -o rw,remount /system\n");
	        os.writeBytes("mv -f /system/build.prop /system/build.prop.bak\n");
	        os.writeBytes("busybox cp -f " + tempBuildProp + " /system/build.prop\n");
	        os.writeBytes("chmod 755 /system/build.prop\n");
	        os.writeBytes("exit\n");
	        os.flush();
	        process.waitFor();
	        
	        Toast.makeText(getApplicationContext(), "Your changes have been comitted. Please restart the phone to observe the changes!", Toast.LENGTH_LONG).show();
	       // Toast.makeText(getApplicationContext(), "Please click on the above ad to support me so that I could continue my schooling ! :)", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            	showToast("Error: " + e.getMessage());
            }
        }
	}
	
	private void restore(){
		Process process;
		DataOutputStream os;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("mount -o rw,remount /system\n");
			os.writeBytes("busybox cp -f " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/build.prop.bak" + " /system/build.prop\n");
			os.writeBytes("chmod 755 /system/build.prop\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
			showToast("Original build.prop restored !");
		} catch(Exception e){
			showToast("Error : " + e);
		}	
	}
}
