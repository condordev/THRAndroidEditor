package de.sgrad.yamahathreditor;

public class DecoderState {
	 /**
     * Decoder states which can either be stopped, decoding or ready to decode (also used for pause)
     */
	public static final int	IN_PAUSE = 2; 
	public static final int DECODING = 3; 
	public static final int STOPPED = 4; 
    public int decoderState = STOPPED;
    
    public int get() {
    	return decoderState;
    }
    
    public void set(int state) { 
    	decoderState = state;
    }
    
    public synchronized String toString(){
    	String str = "";
    	switch (decoderState) {
		case IN_PAUSE:
			str = "IN_PAUSE"; 
			break;
		case DECODING:
			str = "DECODING"; 
			break;
		case STOPPED:
			str = "STOPPED"; 
			break;
		default:
			str="UNKNOWN_STATE";
			break;
		}
    	return str;
    }
    
    
    /**
     * Checks whether the decoder is ready to decode, this is the state used also for Pause 
     *
     * @return <code>true</code> if ready, <code>false</code> otherwise
     */
    public synchronized boolean isInPause() {
        return decoderState == DecoderState.IN_PAUSE;
    }
    
    /**
     * Checks whether the decoder is currently decoding
     *
     * @return <code>true</code> if decoding, <code>false</code> otherwise
     */
    public synchronized boolean isDecoding() {
        return decoderState == DecoderState.DECODING;
    }
    
    
    /**
     * Checks whether the decoder is currently stopped (not decoding)
     *
     * @return <code>true</code> if stopped, <code>false</code> otherwise
     */
    public synchronized boolean isStopped() {
        return decoderState == DecoderState.STOPPED;
    }

}
