package de.sgrad.yamahathreditor;


public interface DecoderEvents {
	public void onStart(String mime, int sampleRate,int channels, long duration, int bitrate);
	public void onDecoding();
	public void onDecoderUpdate(int percent, long currentms, long totalms);
	public void onStop(int numBytesDecoded, boolean sawInputEOS, long duration_ms);
	public void onError();
}
 