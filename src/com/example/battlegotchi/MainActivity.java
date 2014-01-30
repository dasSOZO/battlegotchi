package com.example.battlegotchi;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends Activity {

	// name of the shared reference
	public final static String PREFS_NAME = "gotchidata";

	// time in seconds for gotchi to poo, when user doesn't interact
	final int POO_TIME = 10;
	private Gotchi gotchi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// hide actionbar
		ActionBar actionBar = getActionBar();
		actionBar.hide();

		gotchi = new Gotchi();

		// if the app is run for the first time, set a timestamp (needed to
		// calculate gotchi age)
		long firstRunTimestamp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
				.getLong("firstRunTimestamp", 0);
		if (firstRunTimestamp == 0) {
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
					.putLong("firstRunTimestamp", System.currentTimeMillis())
					.commit();
		}

		loadGotchiData();

		ImageView mainSequence = (ImageView) findViewById(R.id.imageViewGotchi);
		setPooAnimation(mainSequence);
		AnimationDrawable gotchiAnimation = (AnimationDrawable) mainSequence
				.getBackground();

		gotchiAnimation.setVisible(false, true);
		gotchiAnimation.start();
	}

	@Override
	protected void onPause() {
		super.onPause();

		saveGotchiData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		ImageView mainSequence = (ImageView) findViewById(R.id.imageViewGotchi);
		setPooAnimation(mainSequence);
		AnimationDrawable gotchiAnimation = (AnimationDrawable) mainSequence
				.getBackground();

		gotchiAnimation.setVisible(false, true);
		gotchiAnimation.start();
	}

	/**
	 * called when an action button is pressed
	 * 
	 * @param view
	 *            the view (the button which was pressed)
	 */
	public void onAction(View view) {
		ImageView gotchiView = (ImageView) findViewById(R.id.imageViewGotchi);

		switch (view.getId()) {
		case R.id.btnInfo:
			// TODO: deactivate action buttons BEFORE switch (for now it has to
			// be in "case" for testing purposes)

			// deactivate action buttons
			changeAllButtonStates(false);

			// opens new activity to display gotchi info
			Intent intent = new Intent(this, InfoActivity.class);

			// put gotchi data as extras (maybe solution with "parcelables" is
			// better?)
			intent.putExtra("gotchiHealth", gotchi.getHealth());
			intent.putExtra("gotchiStrength", gotchi.getStrength());
			intent.putExtra("gotchiIsAngry", gotchi.getIsAngry());
			intent.putExtra("gotchiMadePoo", gotchi.getMadePoo());
			intent.putExtra("gotchiStage", gotchi.getStage());
			intent.putExtra("gotchiAge", gotchi.getAge(getSharedPreferences(
					PREFS_NAME, MODE_PRIVATE)));
			intent.putExtra("gotchiWeight", gotchi.getWeight());

			startActivity(intent);
			break;
		case R.id.btnFeed:
			gotchi.setHealth(gotchi.getHealth() + 50);

			// alter background resource depending on which stage the
			// gotchi currently is
			int resId = getResources().getIdentifier(
					"stage" + gotchi.getStage() + "_animationlist_eat",
					"drawable", getPackageName());
			gotchiView.setBackgroundResource(resId);
			break;
		case R.id.btnTrain:
			if (gotchi.getStage() == 1) {
				gotchi.setStage(2);
				gotchi.setWeight(2);
			} else {
				gotchi.setStage(1);
				gotchi.setWeight(1);
			}
			restartMainAnimation();
			break;
		case R.id.btnFight:
			// TODO: alter background resource depending on which stage the
			// gotchi
			// currently is
			clearGotchiData();
			break;
		default:
			break;
		}

		AnimationDrawable gotchiAnimation = (AnimationDrawable) gotchiView
				.getBackground();
		int animationDuration = getTotalAnimationDuration(gotchiAnimation);

		gotchiAnimation.setVisible(false, true);
		gotchiAnimation.start();

		waitUntilAnimationIsFinished(animationDuration);

	}

	/**
	 * called when the ImageView itself gets clicked
	 * 
	 * @param view
	 *            the ImageView
	 */
	public void onImageViewClick(View view) {
		// ImageView gotchiView = (ImageView)
		// findViewById(R.id.imageViewGotchi);
		if (gotchi.madePoo) {
			// alter background resource depending on which stage the
			// gotchi currently is

			int resId = getResources().getIdentifier(
					"stage" + gotchi.getStage() + "_animationlist_main",
					"drawable", getPackageName());
			view.setBackgroundResource(resId);
			AnimationDrawable gotchiAnimation = (AnimationDrawable) view
					.getBackground();

			gotchiAnimation.setVisible(false, true);
			gotchiAnimation.start();

			gotchi.setMadePoo(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStop() {
		super.onStop();

		saveGotchiData();
	}

	/**
	 * calculates the total duration of an animation
	 * 
	 * @param anim
	 *            the animation
	 * @return the total duration of the animation
	 */
	private int getTotalAnimationDuration(AnimationDrawable anim) {
		int duration = 0;

		for (int i = 0; i < anim.getNumberOfFrames(); i++) {
			duration = duration + anim.getDuration(i);
		}

		return duration;
	}

	/**
	 * restarts the main (idle) animation and
	 */
	public void restartMainAnimation() {
		// changes to UI may only be done in main thread, use runOnUiThread
		runOnUiThread(new Runnable() {
			public void run() {
				ImageView mainSequence = (ImageView) findViewById(R.id.imageViewGotchi);
				// TODO: alter background resource depending on which stage the
				// gotchi
				// currently is
				int resId = getResources().getIdentifier(
						"stage" + gotchi.getStage() + "_animationlist_main",
						"drawable", getPackageName());
				mainSequence.setBackgroundResource(resId);
				AnimationDrawable gotchiAnimation = (AnimationDrawable) mainSequence
						.getBackground();

				gotchiAnimation.setVisible(false, true);
				gotchiAnimation.start();

				// reactivate action buttons
				changeAllButtonStates(true);
			}
		});
	}

	/**
	 * waits until an animation is finished
	 * 
	 * @param duration
	 *            the duration of the animation
	 */
	public void waitUntilAnimationIsFinished(int duration) {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				restartMainAnimation();
			}
		};
		Timer timer = new Timer();
		timer.schedule(task, duration);
	}

	/**
	 * Sets the poo animation, if user wasn't interacting with gotchi for a
	 * certain time. Otherwise the standard animation is set.
	 * 
	 * @param mainSequence
	 *            the main image view
	 */
	public void setPooAnimation(ImageView mainSequence) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,
				MODE_PRIVATE);
		long timeSinceLastInteraction = System.currentTimeMillis()
				- settings.getLong("lastTimePlayed", 0);
		if (gotchi.madePoo || timeSinceLastInteraction >= (POO_TIME * 1000)) {
			// determine how much poo the gotchi made
			String pooSize = "small";
			if (timeSinceLastInteraction <= (POO_TIME * 2 * 1000)) {
				pooSize = "small";
			}

			else if ((timeSinceLastInteraction > (POO_TIME * 2 * 1000))
					&& (timeSinceLastInteraction < (POO_TIME * 3 * 1000))) {
				pooSize = "medium";
			}

			else if (timeSinceLastInteraction >= (POO_TIME * 3 * 1000)) {
				pooSize = "big";
			}

			// alter background resource depending on which stage the
			// gotchi currently is and how much poo it made
			int resId = getResources().getIdentifier(
					"stage" + gotchi.getStage() + "_animationlist_poo_"
							+ pooSize, "drawable", getPackageName());
			mainSequence.setBackgroundResource(resId);
			gotchi.setMadePoo(true);
		} else {
			// alter background resource depending on which stage the
			// gotchi currently is
			int resId = getResources().getIdentifier(
					"stage" + gotchi.getStage() + "_animationlist_main",
					"drawable", getPackageName());
			mainSequence.setBackgroundResource(resId);
		}
	}

	/**
	 * saves all gotchi data as shared preferences (persistence)
	 */
	public void saveGotchiData() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("gotchiHealth", gotchi.getHealth());
		editor.putInt("gotchiStrength", gotchi.getStrength());
		editor.putBoolean("gotchiMadePoo", gotchi.getMadePoo());
		editor.putBoolean("gotchiIsAngry", gotchi.getIsAngry());
		editor.putInt("gotchiStage", gotchi.getStage());
		editor.putInt("gotchiWeight", gotchi.getStage());
		// time stamp to determine when game was played the last time
		editor.putLong("lastTimePlayed", System.currentTimeMillis());

		editor.commit();
	}

	/**
	 * loads and sets the last saved gotchi data
	 */
	public void loadGotchiData() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,
				MODE_PRIVATE);
		gotchi.setHealth(settings.getInt("gotchiHealth", 100));
		gotchi.setStrength(settings.getInt("gotchiStrength", 1));
		gotchi.setIsAngry(settings.getBoolean("gotchiIsAngry", false));
		gotchi.setMadePoo(settings.getBoolean("gotchiMadePoo", false));
		gotchi.setStage(settings.getInt("gotchiStage", 1));
		gotchi.setWeight(settings.getInt("gotchiWeight", 1));
	}

	/**
	 * Clears all gotchi data to reset the game stats
	 */
	public void clearGotchiData() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		editor.clear();
		editor.commit();

		gotchi.setHealth(settings.getInt("gotchiHealth", 100));
		gotchi.setStrength(settings.getInt("gotchiStrength", 1));
		gotchi.setIsAngry(settings.getBoolean("gotchiIsAngry", false));
		gotchi.setMadePoo(settings.getBoolean("gotchiMadePoo", false));
		gotchi.setStage(settings.getInt("gotchiStage", 1));
		gotchi.setWeight(settings.getInt("gotchiWeight", 1));
	}

	/**
	 * enabled or disables all action buttons
	 * 
	 * @param enabled
	 *            whether the buttons shall be enabled or disabled
	 */
	public void changeAllButtonStates(boolean enabled) {
		((ImageButton) findViewById(R.id.btnInfo)).setEnabled(enabled);
		((ImageButton) findViewById(R.id.btnFeed)).setEnabled(enabled);
		((ImageButton) findViewById(R.id.btnTrain)).setEnabled(enabled);
		((ImageButton) findViewById(R.id.btnFight)).setEnabled(enabled);
		// TODO: implement other buttons
		// ((ImageButton) findViewById(R.id.btn)).setEnabled(enabled);
		// ((ImageButton) findViewById(R.id.btn)).setEnabled(enabled);
		// ((ImageButton) findViewById(R.id.btn)).setEnabled(enabled);
		// ((ImageButton) findViewById(R.id.btn)).setEnabled(enabled);
	}
}
