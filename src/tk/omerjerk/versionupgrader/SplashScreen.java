package tk.omerjerk.versionupgrader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class SplashScreen extends Activity {
	
	private AdView adView;
	private boolean clicked;
	private static final String MY_AD_UNIT_ID = "a151bf1a0035e57";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		clicked = false;
		setContentView(R.layout.activity_splash_screen);
		
		// Create the adView
	    adView = new AdView(SplashScreen.this, AdSize.BANNER, MY_AD_UNIT_ID);

	    // Lookup your LinearLayout assuming it's been given
	    // the attribute android:id="@+id/mainLayout"
	    LinearLayout layout = (LinearLayout) findViewById(R.id.adLayout1);

	    // Add the adView to it
	    layout.addView(adView);

	    // Initiate a generic request to load it with an ad
	    adView.loadAd(new AdRequest());
	    
	    layout.setOnClickListener(new LinearLayout.OnClickListener(){
	    	public void onClick (View v){
	    		clicked = true;
	    	}
	    });
	}
	
	public void start(View v){
		if(clicked == true){
			Intent intent = new Intent(SplashScreen.this, MainActivity.class);
			startActivity(intent);
			finish();
		} else {
			Toast.makeText(SplashScreen.this, "Please click on the above ad atleast once to continue.", Toast.LENGTH_SHORT).show();
		}
	}

    @Override
	  public void onDestroy() {
	    if (adView != null) {
	      adView.destroy();
	    }
	    super.onDestroy();
	  }

	@Override
	public void onPause(){
		super.onPause();
		clicked = true;
	}

}
