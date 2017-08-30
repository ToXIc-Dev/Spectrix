package com.samuelberrien.spectrix.test.vr;

import android.os.Bundle;

import com.google.vr.sdk.base.GvrActivity;
import com.samuelberrien.spectrix.test.main.MainActivity;
import com.samuelberrien.spectrix.test.utils.Visualization;
import com.samuelberrien.spectrix.test.visualizations.explosion.Explosion;
import com.samuelberrien.spectrix.test.visualizations.icosahedron.Icosahedron;
import com.samuelberrien.spectrix.test.visualizations.snow.Snow;

public class MyGvrActivity extends GvrActivity {

	private MyGvrView gvrView;

	private int id_Visualisation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		id_Visualisation = getIntent().getIntExtra(MainActivity.ID_RENDERER, 0);

		Visualization visualization;
		if(id_Visualisation == 1) {
			visualization = new Icosahedron();
		} else if(id_Visualisation == 2) {
			visualization = new Explosion();
		} else if (id_Visualisation == 3) {
			visualization = new Snow();
		} else {
			throw new RuntimeException("Unsuported Visualisation");
		}

		this.gvrView = new MyGvrView(this, visualization);
		setContentView(gvrView);
	}

	@Override
	protected void onPause() {
		this.gvrView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.gvrView.onResume();
	}
}
