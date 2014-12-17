package com.example;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.TextView;

public class HighScoreActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// Coloring the action bar according to material design standards
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#BF360C")));

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_high_score);
		TextView scoreView = (TextView) findViewById(R.id.high_scores_list);

		SharedPreferences scorePrefs = getSharedPreferences("findhighscore", 0);

		String[] savedScores = scorePrefs.getString("highScores",
				"NO HIGHSCORE").split("\\|");

		Map<String, String> nameScore = new TreeMap<String, String>();

		try {
			for (int i = 0; i < savedScores.length; i++) {
				String temp[] = savedScores[i].split("-");
				nameScore.put(temp[1], temp[0]);

			}
			StringBuilder scoreBuild = new StringBuilder("");

			int ten = 0;

			for (Entry<String, String> entry : nameScore.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				scoreBuild.append(value + " " + "-" + " " + key + "\n");
				System.out.println(key + " => " + value);
				ten++;
				if (ten == 10)
					break;
			}

			scoreView.setText(scoreBuild.toString());
		} catch (ArrayIndexOutOfBoundsException e) {
			scoreView.setText("NO HIGHSCORE YET");
		}

	}
}
