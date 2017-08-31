package com.samuelberrien.spectrix.vr;

import android.content.Context;
import android.media.audiofx.Visualizer;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.samuelberrien.spectrix.utils.core.Visualization;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by samuel on 11/01/17.
 */

public class GLStereoRenderer implements GvrView.StereoRenderer {

	protected Context context;

	private static final float Z_NEAR = 1f;
	private static final float Z_FAR = 50.0f;
	protected float mCameraX = 0f;
	protected float mCameraY = 0f;
	protected float mCameraZ = 0.001f;

	protected float[] mProjectionMatrix = new float[16];
	protected final float[] mViewMatrix = new float[16];

	private float[] mHeadView = new float[16];
	private float[] mCamera = new float[16];

	private final float[] mLightPosInModelSpace = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
	protected final float[] mLightPosInEyeSpace = new float[4];
	private final float[] mLightModelMatrix = new float[16];
	private final float[] mLightPosInWorldSpace = new float[4];

	private Visualization visualization;
	private float[] freqArray;

	/**
	 * @param context
	 */
	public GLStereoRenderer(Context context, Visualization visualization) {
		this.context = context;
		this.visualization = visualization;
		this.freqArray = new float[Visualizer.getCaptureSizeRange()[1]];
	}

	@Override
	public void onSurfaceCreated(EGLConfig eglConfig) {
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glDepthMask(true);
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		this.visualization.init(context, true);
	}

	@Override
	public void onNewFrame(HeadTransform headTransform) {
		float[] camPos = visualization.getCameraPosition();
		Matrix.setLookAtM(mCamera, 0, camPos[0], camPos[1], camPos[2], 0.0f + camPos[0], 0.0f + camPos[1], 1f + camPos[2], 0.0f, 1.0f, 0.0f);
		headTransform.getHeadView(mHeadView, 0);
		visualization.update(freqArray);
	}

	@Override
	public void onDrawEye(Eye eye) {
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glDepthMask(true);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		// Apply the eye transformation to the camera.
		Matrix.multiplyMM(mViewMatrix, 0, eye.getEyeView(), 0, mCamera, 0);

		mProjectionMatrix = eye.getPerspective(Z_NEAR, Z_FAR);

		Matrix.setIdentityM(mLightModelMatrix, 0);
		//TODO light pos
		Matrix.translateM(mLightModelMatrix, 0, 0f, 0f, 0f);
		Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
		Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);

		float[] cam = visualization.getCameraPosition();

		visualization.draw(mProjectionMatrix.clone(), mViewMatrix.clone(), mLightPosInEyeSpace.clone(), new float[]{cam[0], cam[1], cam[2]});
	}

	@Override
	public void onFinishFrame(Viewport viewport) {
	}

	@Override
	public void onSurfaceChanged(int width, int height) {
	}

	@Override
	public void onRendererShutdown() {
	}

	public void updateFreqArray(float[] newFreqArray) {
		freqArray = newFreqArray;
	}
}