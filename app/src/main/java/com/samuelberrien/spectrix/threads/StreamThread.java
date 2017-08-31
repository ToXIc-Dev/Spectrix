package com.samuelberrien.spectrix.threads;

import android.media.audiofx.Visualizer;

import com.samuelberrien.spectrix.utils.core.Visualization;

/**
 * Created by samuel on 23/08/17.
 */

public class StreamThread extends VisualizationThread {

	protected Visualizer visualizer;

	private static float freqAugmentation = 0.3f;

	public StreamThread(Visualization visualization) {
		super("StreamThread", visualization);

		visualizer = new Visualizer(0);
		visualizer.setEnabled(false);
		/*visualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
		visualizer.setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS);*/
		visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
		visualizer.setEnabled(true);
	}

	@Override
	protected void work(Visualization visualization) {
		visualization.update(getFrequencyMagns());
	}

	@Override
	protected void onEnd() {
		visualizer.setEnabled(false);
		visualizer.release();
	}

	@Override
	protected float[] getFrequencyMagns() {
		byte[] bytes = new byte[visualizer.getCaptureSize()];
		visualizer.getFft(bytes);
		float[] fft = new float[bytes.length / 2];

		for (int i = 0; i < fft.length; i++) {
			float real = (float) (bytes[(i * 2) + 0]) / 128.0f;
			float imag = (float) (bytes[(i * 2) + 1]) / 128.0f;
			fft[i] = ((real * real) + (imag * imag));
			fft[i] += fft[i] * i * freqAugmentation;
		}
		return fft;
	}

}