package com.samuelberrien.spectrix.main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.samuelberrien.spectrix.R;
import com.samuelberrien.spectrix.normal.MyGLSurfaceView;
import com.samuelberrien.spectrix.threads.VisualizationThread;
import com.samuelberrien.spectrix.utils.core.Visualization;
import com.samuelberrien.spectrix.utils.core.VisualizationHelper;
import com.samuelberrien.spectrix.utils.ui.SpectrixToolBar;
import com.samuelberrien.spectrix.utils.ui.expand.ExpandButton;
import com.samuelberrien.spectrix.utils.ui.expand.RadioExpand;
import com.samuelberrien.spectrix.visualizations.spectrum.Spectrum;
import com.samuelberrien.spectrix.vr.MyGvrActivity;

public class MainActivity extends AppCompatActivity implements MyGLSurfaceView.OnVisualizationInitFinish, View.OnTouchListener {

	public static final String IS_STREAM = "IS_STREAM";
	public static final String ID_RENDERER = "ID_RENDERER";

	private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 0;

	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private Button showToolBarButton;

	private MyGLSurfaceView myGLSurfaceView;

	private Menu menu;
	private SubMenu subMenu;

	private MediaPlayer mPlayer;

	private int idVisualisation = 0;

	private int currentListeningId;

	private RelativeLayout frameLayoutSurfaceView;
	private ProgressBar progressBar;

	private GestureDetector gestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		SpectrixToolBar toolbar = (SpectrixToolBar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0);
		drawerLayout.addDrawerListener(drawerToggle);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		frameLayoutSurfaceView = (RelativeLayout) findViewById(R.id.layout_surface_view);

		showToolBarButton = (Button) findViewById(R.id.show_toolbar_button);

		progressBar = (ProgressBar) findViewById(R.id.progress_bar_visu);

		//FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/ace_futurism.ttf");

		currentListeningId = VisualizationThread.NONE;
		requestRecordPermission();

		mPlayer = MediaPlayer.create(this, R.raw.crea_session_8);
		mPlayer.setLooping(false);
		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.play_icon));
			}
		});

		switchOrientation(getResources().getConfiguration().orientation);

		gestureDetector = new GestureDetector(this, new OnSwipeListener());

		toolbar.setOnTouchListener(this);
		showToolBarButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showToolBar();
			}
		});
	}

	private void hideToolBar() {
		getSupportActionBar().hide();
		showToolBarButton.setVisibility(View.VISIBLE);
	}

	private void showToolBar() {
		showToolBarButton.setVisibility(View.GONE);
		getSupportActionBar().show();
	}

	private void requestRecordPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
		} else {
			setUpDrawer();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					setUpDrawer();
				} else {
					if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
						showEndDialog();
					} else {
						showSorryDialog();
					}
				}
				break;
			}
		}
	}

	private void showEndDialog() {
		AlertDialog endDialog = new AlertDialog.Builder(this, R.style.SpectrixDialogTheme)
				.setMessage("Spectrix can't work without audio record, Sorry...")
				.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						finish();
					}
				})
				.create();
		endDialog.setCancelable(false);
		endDialog.setCanceledOnTouchOutside(false);
		endDialog.show();
	}

	private void showSorryDialog() {
		AlertDialog sorryDialog = new AlertDialog.Builder(this, R.style.SpectrixDialogTheme)
				.setMessage("Spectrix needs to record audio,\n" +
						"Please accept the permission !")
				.setNegativeButton("No, Exit", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						finish();
					}
				})
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						requestRecordPermission();
					}
				})
				.create();
		sorryDialog.setCancelable(false);
		sorryDialog.setCanceledOnTouchOutside(false);
		sorryDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		this.menu = menu;
		subMenu = this.menu.getItem(2).getSubMenu();
		return true;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
		switchOrientation(newConfig.orientation);
	}

	private void switchOrientation(int orientation) {
		LinearLayout menuDrawer = (LinearLayout) findViewById(R.id.layout_menu_drawer);
		LinearLayout layoutToggle = (LinearLayout) findViewById(R.id.layout_toggle);
		LinearLayout layoutScroll = (LinearLayout) findViewById(R.id.layout_scroll);

		LinearLayout.LayoutParams layoutPortraitParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams layoutLandParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f);

		switch (orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				menuDrawer.setOrientation(LinearLayout.HORIZONTAL);
				layoutScroll.setLayoutParams(layoutLandParams);
				layoutToggle.setLayoutParams(layoutLandParams);
				findViewById(R.id.about_spectrix_text_view).setVisibility(View.VISIBLE);
				break;
			case Configuration.ORIENTATION_PORTRAIT:
				menuDrawer.setOrientation(LinearLayout.VERTICAL);
				layoutToggle.setLayoutParams(layoutPortraitParams);
				layoutScroll.setLayoutParams(layoutPortraitParams);
				findViewById(R.id.about_spectrix_text_view).setVisibility(View.GONE);
				break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				drawerLayout.openDrawer(GravityCompat.START);
				return true;
			case R.id.hide_toolbar:
				hideToolBar();
				return true;
			case R.id.play_pause_toolbar:
				if (mPlayer.isPlaying()) {
					menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.play_icon));
					mPlayer.pause();
				} else {
					menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.pause_icon));
					mPlayer.start();
				}
				return true;
			case R.id.cardboard_toolbar:
				Intent intent = new Intent(this, MyGvrActivity.class);
				intent.putExtra(MainActivity.ID_RENDERER, idVisualisation);
				intent.putExtra(MainActivity.IS_STREAM, currentListeningId == VisualizationThread.STREAM_MUSIC);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setUpDrawer() {
		Visualization startVisualization = new Spectrum();

		myGLSurfaceView = new MyGLSurfaceView(this, startVisualization, currentListeningId, this);

		findViewById(R.id.stream_radio_button).performClick();

		getSupportActionBar().setTitle(startVisualization.getName());

		frameLayoutSurfaceView.addView(myGLSurfaceView);

		RadioExpand radioExpand = (RadioExpand) findViewById(R.id.radio_expand_scroll_view_visualisations);

		for (int i = 0; i < VisualizationHelper.NB_VISUALIZATIONS; i++) {
			final int index = i;
			final String name = VisualizationHelper.getVisualization(i).getName();
			Runnable onConfirm = new Runnable() {
				@Override
				public void run() {
					idVisualisation = index;
					myGLSurfaceView.onPause();

					frameLayoutSurfaceView.removeView(myGLSurfaceView);

					progressBar.setVisibility(View.VISIBLE);

					Visualization visualization = VisualizationHelper.getVisualization(index);

					myGLSurfaceView = new MyGLSurfaceView(
							getApplicationContext(), visualization,
							currentListeningId, MainActivity.this);
					frameLayoutSurfaceView.addView(myGLSurfaceView);

					getSupportActionBar().setTitle(name);

					drawerLayout.closeDrawers();
				}
			};
			ExpandButton expandButton = new ExpandButton(this, onConfirm);
			expandButton.setText(name);
			radioExpand.addExpandButton(expandButton);
		}
	}

	@Override
	protected void onPause() {
		if (myGLSurfaceView != null)
			myGLSurfaceView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (myGLSurfaceView != null)
			myGLSurfaceView.onResume();
	}

	public void stream(View v) {
		currentListeningId = VisualizationThread.STREAM_MUSIC;
		myGLSurfaceView.setListening(VisualizationThread.STREAM_MUSIC);
	}

	public void mic(View view) {
		currentListeningId = VisualizationThread.MIC_MUSIC;
		myGLSurfaceView.setListening(VisualizationThread.MIC_MUSIC);
	}

	@Override
	public void onFinish() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progressBar.setVisibility(View.GONE);
			}
		});
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		view.performClick();
		return gestureDetector.onTouchEvent(motionEvent);
	}

	private class OnSwipeListener extends GestureDetector.SimpleOnGestureListener {

		private int LimitSwipeSpeed;

		OnSwipeListener() {
			LimitSwipeSpeed = 1;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			float yDelta = e2.getY() - e1.getY();
			if (yDelta > 0 && Math.abs(velocityY) > LimitSwipeSpeed) {
				showToolBar();
			} else if (Math.abs(velocityY) > LimitSwipeSpeed) {
				hideToolBar();
			} else {
				return false;
			}
			return true;
		}


		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
	}
}
