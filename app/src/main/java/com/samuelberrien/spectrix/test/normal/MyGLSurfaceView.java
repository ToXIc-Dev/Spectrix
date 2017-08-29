package com.samuelberrien.spectrix.test.normal;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.samuelberrien.spectrix.test.utils.Visualization;
import com.samuelberrien.spectrix.test.utils.threads.UpdateThread;

/**
 * Created by samuel on 23/08/17.
 */

public class MyGLSurfaceView extends GLSurfaceView {

	private Visualization visualization;
	private UpdateThread updateThread;

	private boolean isRendererSetted = false;

	private GLRenderer3D glRenderer3D;

	public MyGLSurfaceView(Context context, Visualization visualization) {
		super(context);
		setEGLContextClientVersion(2);
		setPreserveEGLContextOnPause(true);

		this.visualization = visualization;
		if (this.visualization.is3D()) {
			glRenderer3D = new GLRenderer3D(getContext(), this.visualization);
			setRenderer(glRenderer3D);
		} else {
			GLRenderer2D glRenderer2D = new GLRenderer2D(getContext(), this.visualization);
			setRenderer(glRenderer2D);
		}

		updateThread = new UpdateThread(this.visualization);
		updateThread.start();
	}

	/*public void setVisualization(Visualization visualization) {
		if (isRendererSetted)
			throw new RuntimeException();
		this.visualization = visualization;

		if (updateThread != null && !updateThread.isCanceled()) {
			updateThread.cancel();
		}
		updateThread = new UpdateThread(this.visualization);
		updateThread.start();
		isRendererSetted = true;
	}*/

	@Override
	public void onPause() {
		updateThread.cancel();
		try {
			updateThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (updateThread != null) {
			updateThread.cancel();
			try {
				updateThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		updateThread = new UpdateThread(visualization);
		updateThread.start();
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (glRenderer3D != null)
			glRenderer3D.onTouchEvent(e);

		return true;
	}
}
