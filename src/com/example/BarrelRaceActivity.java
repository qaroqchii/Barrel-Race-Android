package com.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

public class BarrelRaceActivity extends Activity implements Callback,
		SensorEventListener, android.view.View.OnClickListener {
	private static final int BALL_RADIUS = 35;
	public static int BARREL_RADIUS = 40;

	public static int BOTTOM_PADDING;
	private SurfaceView surface;
	private SurfaceHolder holder;
	private final BarrelRaceModel model = new BarrelRaceModel(BALL_RADIUS);
	private GameLoop gameLoop;
	private Paint backgroundPaint;
	private Paint borderPaint;

	private Paint ballPaint;

	private long lastSensorUpdate = -1;

	public static int barrelLeftX;
	public static int barrelLeftY;
	public static int barrelRightX;
	public static int barrelRightY;
	public static int barrelMiddleX;
	public static int barrelMiddleY;
	private SensorManager mSensorManager;
	private Sensor mAccel;
	private TextView timeTextView;
	private Handler timeHandler = new Handler();
	private ImageButton playButton;
	private ImageButton resetButton;
	// Static variable for checking if any variable is circled.
	public boolean barrelColorLeft = false;
	public boolean barrelColorRight = false;
	public boolean barrelColorMiddle = false;

	private Paint barrelPaintLeft;
	private Paint barrelPaintRight;
	private Paint barrelPaintMiddle;
	private Runnable updateTimerThread;
	private static Long savedTime = 9223372036854775807L;
	private Long currTime = 0L;
	private String minTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// fullscreen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.bouncing_ball);
		// setting vibrator
		Vibrator vibrator = (Vibrator) getSystemService(Activity.VIBRATOR_SERVICE);
		model.setVibrator(vibrator);

		// making court
		surface = (SurfaceView) findViewById(R.id.bouncing_ball_surface);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;

		screenWidth = Math.min(screenHeight, screenWidth);

		LayoutParams lp = surface.getLayoutParams();
		lp.width = screenWidth;
		BOTTOM_PADDING = BALL_RADIUS * 2 + 10;
		lp.height = screenWidth + BOTTOM_PADDING; // To make square height =
													// width
		surface.setLayoutParams(lp);

		holder = surface.getHolder();
		surface.getHolder().addCallback(this);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		mSensorManager.registerListener(this, mAccel,
				SensorManager.SENSOR_DELAY_GAME);
		SharedPreferences settingsPref = getSharedPreferences("settingColor", 0);
		backgroundPaint = new Paint();
		if (settingsPref.getString("bgColor", "light gray").equals("white")) {
			backgroundPaint.setColor(Color.WHITE);
		} else {
			backgroundPaint.setColor(Color.LTGRAY);
		}

		ballPaint = new Paint();
		ballPaint.setAntiAlias(true);

		if (settingsPref.getString("horseColor", "Gray").equals("Gray")) {
			ballPaint.setColor(Color.GRAY);
		} else {
			ballPaint.setColor(Color.BLACK);
		}

		// Add 3 diiferent paints for barrels.
		barrelPaintLeft = new Paint();
		barrelPaintLeft.setAntiAlias(true);

		barrelPaintRight = new Paint();
		barrelPaintRight.setAntiAlias(true);

		barrelPaintMiddle = new Paint();
		barrelPaintMiddle.setAntiAlias(true);

		if (settingsPref.getString("barrelColor", "Yellow").equals("Blue")) {
			barrelPaintLeft.setColor(Color.BLUE);
			barrelPaintMiddle.setColor(Color.BLUE);
			barrelPaintRight.setColor(Color.BLUE);
		} else if (settingsPref.getString("barrelColor", "Yellow").equals(
				"Yellow")) {
			barrelPaintLeft.setColor(Color.YELLOW);
			barrelPaintMiddle.setColor(Color.YELLOW);
			barrelPaintRight.setColor(Color.YELLOW);
		} else {
			barrelPaintLeft.setColor(Color.RED);
			barrelPaintMiddle.setColor(Color.RED);
			barrelPaintRight.setColor(Color.RED);
		}

		if (settingsPref.getString("barrelSize", "small").equals("small")) {
			BARREL_RADIUS = 40;
		} else if (settingsPref.getString("barrelSize", "small").equals(
				"medium")) {
			BARREL_RADIUS = 50;
		} else {
			BARREL_RADIUS = 60;
		}

		borderPaint = new Paint();
		borderPaint.setColor(Color.DKGRAY);
		borderPaint.setAntiAlias(true);
		borderPaint.setStrokeWidth(10);
		// Starting position of ball
		model.moveBall(lp.width / 2, lp.height);

		timeTextView = (TextView) findViewById(R.id.time);

		playButton = (ImageButton) findViewById(R.id.play_button);
		playButton.setOnClickListener(this);

		resetButton = (ImageButton) findViewById(R.id.reset_button);
		resetButton.setOnClickListener(this);

	}

	@Override
	protected void onPause() {
		super.onPause();
		try {

			if (mSensorManager != null && this != null) {
				mSensorManager.unregisterListener(this);
			}
		} catch (Exception e) {
			Log.w("exceptions", e.getMessage());
		}
		model.setVibrator(null);
		model.setAccel(0, 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccel,
				SensorManager.SENSOR_DELAY_GAME);
		Vibrator vibrator = (Vibrator) getSystemService(Activity.VIBRATOR_SERVICE);
		model.setVibrator(vibrator);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mSensorManager.registerListener(this, mAccel,
				SensorManager.SENSOR_DELAY_GAME);
		Vibrator vibrator = (Vibrator) getSystemService(Activity.VIBRATOR_SERVICE);
		model.setVibrator(vibrator);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mSensorManager.unregisterListener(this);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		model.setSize(width, height);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		draw();

	}

	private void draw() {
		// thread safety - the SurfaceView could go away while we are drawing

		Canvas c = null;
		try {
			c = holder.lockCanvas();

			// this needs to synchronize on something
			if (c != null) {
				doDraw(c);
			}
		} finally {
			if (c != null) {
				holder.unlockCanvasAndPost(c);
			}
		}
	}

	/**
	 * Drawing logic goes in this process.
	 * 
	 * @param Canvas
	 *            c
	 */
	private void doDraw(Canvas c) {
		int width = c.getWidth();
		int height = c.getHeight();
		c.drawRect(0, 0, width, height, backgroundPaint); // White background

		c.drawLine(5, 0, 5, height, borderPaint); // left border
		c.drawLine(width - 5, 0, width - 5, height, borderPaint); // right
																	// border
		c.drawLine(0, 5, width, 5, borderPaint); // top border

		// Bottom border lines
		c.drawLine(0, height - BOTTOM_PADDING, width / 2 - BOTTOM_PADDING / 2,
				height - BOTTOM_PADDING, borderPaint);
		c.drawLine(width / 2 + BOTTOM_PADDING / 2, height - BOTTOM_PADDING,
				width, height - BOTTOM_PADDING, borderPaint);

		// Barrel Circles
		barrelLeftX = width / 4;
		barrelLeftY = height / 4;
		barrelRightX = width * 3 / 4;
		barrelRightY = height / 4;
		barrelMiddleX = width / 2;
		barrelMiddleY = height * 2 / 3;

		if (barrelColorLeft) {
			barrelPaintLeft.setColor(Color.GREEN);
		}
		c.drawCircle(barrelLeftX, barrelLeftY, BARREL_RADIUS, barrelPaintLeft);// top
																				// left
																				// barrel
		if (barrelColorRight) {
			barrelPaintRight.setColor(Color.GREEN);
		}
		c.drawCircle(barrelRightX, barrelRightY, BARREL_RADIUS,
				barrelPaintRight);// top right barrel

		if (barrelColorMiddle) {
			barrelPaintMiddle.setColor(Color.GREEN);
		}
		c.drawCircle(barrelMiddleX, barrelMiddleY, BARREL_RADIUS,
				barrelPaintMiddle);// bottom barrel

		float ballX, ballY;
		synchronized (model.LOCK) {
			ballX = model.ballPixelX;
			ballY = model.ballPixelY;
		}
		c.drawCircle(ballX, ballY, BALL_RADIUS, ballPaint);
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		try {
			model.setSize(0, 0);

		} finally {
			gameLoop = null;
		}
	}

	private class GameLoop extends Thread {
		private volatile boolean running = true;
		private String currTimeString;

		public void run() {
			while (running) {
				draw();
				model.updatePhysics();

				int roundStateChanged[] = new int[3];
				roundStateChanged = model.isCompletedCircle();

				if (roundStateChanged[0] == 1) {
					barrelColorLeft = true;
					Log.d(POWER_SERVICE, "Touched");
				}

				if (roundStateChanged[1] == 1) {
					barrelColorRight = true;
					Log.d(POWER_SERVICE, "Touched");
				}

				if (roundStateChanged[2] == 1) {
					barrelColorMiddle = true;
					Log.d(POWER_SERVICE, "Touched");
				}

				if (barrelColorLeft && barrelColorRight && barrelColorMiddle) {
					if (model.ballPixelX <= (model.getPixelWidth() / 2) + 15
							&& model.ballPixelX >= (model.getPixelWidth() / 2) - 15
							&& model.ballPixelY >= model.getPixelHeight() / 2) {
						timeHandler.removeCallbacks(updateTimerThread);

						currTime = model.getUpdatedTime();

						if (savedTime > currTime) {
							savedTime = currTime;
						}
						int secs = (int) (savedTime / 1000);
						int mins = secs / 60;
						secs = secs % 60;
						int milliseconds = (int) (savedTime % 1000);
						minTime = new String("" + mins + ":"
								+ String.format("%02d", secs) + ":"
								+ String.format("%03d", milliseconds));

						int secs1 = (int) (currTime / 1000);
						int mins1 = secs1 / 60;
						secs1 = secs1 % 60;
						int milliseconds1 = (int) (currTime % 1000);
						currTimeString = new String("" + mins1 + ":"
								+ String.format("%02d", secs1) + ":"
								+ String.format("%03d", milliseconds1));

						SharedPreferences minT = getSharedPreferences(
								"findhighscore", 0);
						SharedPreferences.Editor editor = minT.edit();
						editor.putString("highscore", minTime);
						editor.commit();

						Intent winIntent = new Intent(BarrelRaceActivity.this,
								FinalActivity.class);
						winIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
						winIntent.putExtra("win", "You Won!");
						winIntent.putExtra("time", currTimeString);
						startActivity(winIntent);
					}
				}
				if (model.isUserLost()) {
					// stop the timer thread
					timeHandler.removeCallbacks(updateTimerThread);
					// create intent
					Intent lostIntent = new Intent(BarrelRaceActivity.this,
							FinalActivity.class);

					lostIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					lostIntent.putExtra("win", "You Lost!");
					lostIntent.putExtra("time", model.getTimeString());
					lostIntent.putExtra("updatedtime", model.getUpdatedTime());
					startActivity(lostIntent);
					// stop updatephysics thread
					safeStop();
					finish();
				}
			}
		}

		public void safeStop() {
			running = false;
			interrupt();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent evt) {
		if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			if (lastSensorUpdate == -1 || (curTime - lastSensorUpdate) > 50) {
				lastSensorUpdate = curTime;
				model.setAccel(-evt.values[0], -evt.values[1]);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.play_button:
			/**
			 * Updates the time of the game
			 */
			model.setStartTime(SystemClock.uptimeMillis());
			gameLoop = new GameLoop();
			gameLoop.start();

			updateTimerThread = new Runnable() {
				public void run() {
					timeTextView.setText(model.getTimeString());
					timeHandler.postDelayed(this, 0);
				}
			};
			timeHandler.postDelayed(updateTimerThread, 0);
			playButton.setEnabled(false);
			break;

		case R.id.reset_button:
			startActivity(new Intent(this, BarrelRaceActivity.class));

		}
	}

}
