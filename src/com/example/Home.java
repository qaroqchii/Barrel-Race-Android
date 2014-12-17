package com.example;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Home extends Activity implements OnClickListener {

	private Button startBtn;
	private Button highscore;
	private Button settingsBtn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Coloring the action bar according to material design standards
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#BF360C")));

		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		startBtn = (Button) findViewById(R.id.start);

		startBtn.setOnClickListener(this);

		highscore = (Button) findViewById(R.id.highscore);
		highscore.setOnClickListener(this);

		settingsBtn = (Button) findViewById(R.id.setting);
		settingsBtn.setOnClickListener(this);

	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start:
			startActivity(new Intent(Home.this, BarrelRaceActivity.class));

			break;

		case R.id.highscore:
			/*
			 * SharedPreferences high = getSharedPreferences("findhighscore",
			 * 0); String score = high.getString("highscore",
			 * "NO SCORE AVAILABLE");
			 * 
			 * SharedPreferences.Editor scoreEdit = high.edit();
			 * scoreEdit.putString("highScores",score+"|");
			 * 
			 * scoreEdit.commit();
			 */

			Intent highIntent = new Intent(this, HighScoreActivity.class);
			this.startActivity(highIntent);

			// Toast.makeText(this, score, Toast.LENGTH_SHORT).show();
			break;

		case R.id.setting:

			startActivity(new Intent(Home.this, SettingsActivity.class));
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
		return;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		android.os.Process.killProcess(android.os.Process.myPid());
		super.onDestroy();
	}

}
