package com.example;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FinalActivity extends Activity implements OnClickListener {

	private Button replay;
	private Button home;

	private EditText name;
	private Button save;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// fullscreen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_final);

		name = (EditText) findViewById(R.id.name);
		save = (Button) findViewById(R.id.save);

		name.setVisibility(View.INVISIBLE);
		save.setVisibility(View.INVISIBLE);

		Bundle b = getIntent().getExtras();
		System.out.println(b.getString("win"));

		replay = (Button) findViewById(R.id.replay);
		replay.setOnClickListener(this);

		home = (Button) findViewById(R.id.mainmenu);
		home.setOnClickListener(this);

		Bundle e = getIntent().getExtras();

		TextView tim = (TextView) findViewById(R.id.scoreTextView);
		tim.setText(e.getString("time"));

		TextView res = (TextView) findViewById(R.id.result);
		res.setText(e.getString("win"));

		TextView highScore = (TextView) findViewById(R.id.highTextView);
		SharedPreferences highPref = getSharedPreferences("findhighscore", 0);
		String s = highPref.getString("highscore", "NO SCORE AVAILABLE");
		highScore.setText(s);
		highScore.setTextColor(Color.LTGRAY);

		if (b.getString("win").equalsIgnoreCase("You Won!")) {

			name.setVisibility(View.VISIBLE);
			save.setVisibility(View.VISIBLE);

			save.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (name.getText().equals("")) {
						Toast.makeText(getApplicationContext(),
								"Please enter your name", Toast.LENGTH_SHORT)
								.show();
					} else {
						SharedPreferences high = getSharedPreferences(
								"findhighscore", 0);
						SharedPreferences.Editor scoreEdit = high.edit();

						String score = high.getString("highScores", "");
						String time = (String) getIntent().getExtras().get(
								"time");

						StringBuilder str = new StringBuilder(score);
						str.append(name.getText().toString());
						str.append("-");
						str.append(time);
						str.append("|");

						scoreEdit.putString("highScores", str.toString());
						scoreEdit.commit();

						Toast.makeText(getApplicationContext(),
								"Saved:" + str.toString(), Toast.LENGTH_SHORT)
								.show();
					}
				}
			});
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.replay:
			startActivity(new Intent(FinalActivity.this,
					BarrelRaceActivity.class));
			finish();
			break;

		case R.id.mainmenu:
			startActivity(new Intent(FinalActivity.this, Home.class));
			break;

		}

	}

	@Override
	public void onBackPressed() {
		finish();
		return;
	}
}
