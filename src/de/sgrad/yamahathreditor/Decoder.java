package de.sgrad.yamahathreditor;

import android.content.res.Resources;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.ByteBuffer;

public class Decoder implements Runnable{
	private Thread thread = null;
    private static final String TAG = "THR";
    private Resources mResources = null;
    private int sourceRawResId = -1;
    private MediaFormat format = null;
    private String sourcePath = "";
    
    private MediaExtractor extractor = null;
    private MediaCodec codec = null;
    
    private ByteBuffer[] codecInputBuffers;
    private ByteBuffer[] codecOutputBuffers;
    
   // private PipedOutputStream out = null;
    
    private int sampleRate = 0, channels = 0, bitrate = 0;
    private long duration = 0, presentationTimeUs = 0;
    private int samplesEncoded = 0;
    
    private String mime;
    private byte[] chunk;
    
	private DecoderEvents events = null;
	private DecoderState state = null;
	boolean sawOutputEOS = false;
	boolean sawInputEOS = false;
	final long kTimeOutUs = 5000;
	MediaCodec.BufferInfo info = null;
	File decodedDataFile = null;
	//FileOutputStream outputFile = null;
	BufferedOutputStream bos = null;
	
	Handler handler = null;
	int numBytesDecoded = 0;
    
	private void setEventsListener(DecoderEvents events) {
		this.events = events;
	}
	
	public Decoder(DecoderEvents events) {
		handler = new Handler();
		state = new DecoderState();
		state.set(DecoderState.STOPPED);
		chunk = new byte[512]; // initial size will be realigned if necessary
		info = new MediaCodec.BufferInfo();
		setEventsListener(events);
		Log.d( TAG, "C'tor called." + " State: " + state.toString());
		//extractor = new MediaExtractor();
		
		//newPCMFile(Environment.getExternalStorageDirectory(),"data.pcm");
		//bufferedOutputStreamFromFile(decodedDataFile);
		
	}
	
	public synchronized void newPCMFile(File directory, String fileName){
		decodedDataFile = new File(directory, fileName);
		if (decodedDataFile.exists()) {
			boolean deleted = decodedDataFile.delete();
			Log.d( TAG, "File: " + decodedDataFile.getAbsolutePath() + " already exists. Deleting... Deleted = " + deleted);
		}		
	}
	
	public synchronized void bufferedOutputStreamFromFile(File file){
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file));
			//outputFile = new FileOutputStream(decodedDataFile, true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * set the data source, a file path or an url, or a file descriptor, to play encoded audio from 
	 * @param src
	 */
	public synchronized void setDataSource(String src) {
		sourcePath = src;
	}
	
	public synchronized DecoderState getState(){
		return this.state;
	}
	
	/*
	public synchronized void setPipedOutputStream(PipedOutputStream out){
		this.out = out;
	}
	
	public PipedOutputStream getPipedOutputStream(){
		return this.out;
	}
	*/
	
	
	public synchronized void start() {
		Log.d( TAG, "START() Decoder Thread State: " + state.toString() + " JAVA: " + Thread.currentThread().getState().toString());
		Log.d(TAG, "Decoded stream lenght in byte: " + calculateDecodedStreamLenght());
		
		if(state.get() == DecoderState.STOPPED){
			thread = new Thread(this);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
		
		if (state.get() == DecoderState.IN_PAUSE) {
			syncNotify();
		}
		state.set(DecoderState.DECODING);
	}
	
	public  void  pause() {
		state.set(DecoderState.IN_PAUSE);
		//if (events != null && !stop) handler.post(new Runnable() { @Override public void run() { events. }});
	}
	
	public  String  getDataSource() {
		return sourcePath;
		//if (events != null && !stop) handler.post(new Runnable() { @Override public void run() { events. }});
	}
	
	public synchronized void stop() {
		state.set(DecoderState.STOPPED);
		Log.d( TAG, "STOP() Decoder Thread State: " + state.toString() + " JAVA: " + Thread.currentThread().getState().toString());
		//if (events != null) handler.post(new Runnable() { @Override public void run() { events.onStop(numBytesDecoded, sawInputEOS); }});
		//setOnHold();
	}
	
	/**
     * Call notify to control the PAUSE (waiting) state, when the state is changed
     */
    public synchronized void syncNotify() {
    	Log.d(TAG, "notify called");
    	this.notify();
    }
	
    /**
     * A pause mechanism that would block current thread when pause flag is set (IN_PAUSE)
     */
    public void waitDecoding(){
        while(state.get() == DecoderState.IN_PAUSE) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * decoding job is finished (song end) block current thread until restart
     */
    public void setOnHold(){
        while(state.get() == DecoderState.STOPPED) {
            try {
            	Log.d(TAG, "wait");
            	if (events != null) handler.post(new Runnable() { @Override public void run() { events.onStop(numBytesDecoded, sawInputEOS, duration / 1000); }});
            	wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void prepareDecoderRessources(){
    	Log.d(TAG, "prepareDecoderRessources with song: " + sourcePath);
    	
    	newPCMFile(Environment.getExternalStorageDirectory(),"data.pcm");
    	bufferedOutputStreamFromFile(decodedDataFile);
    	numBytesDecoded = 0;
    	
    	extractor = new MediaExtractor();
    	//extractor.setDataSource(testFd.getFileDescriptor(), testFd.getStartOffset(), testFd.getLength());
    	if (sourcePath != null){
    		extractor.setDataSource(this.sourcePath);
    	}
        try {
        	format = extractor.getTrackFormat(0);
	        mime = format.getString(MediaFormat.KEY_MIME);
	        sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
			channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
			// if duration is 0, we are probably playing a live stream
			duration = format.getLong(MediaFormat.KEY_DURATION);
			bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE);
        } catch (Exception e) {
			Log.e(TAG, "Reading format parameters exception:"+e.getMessage());
			// don't exit, tolerate this error, we'll fail later if this is critical
		}

        codec = MediaCodec.createDecoderByType(mime);
        
        codec.configure(format, null /* surface */, null /* crypto */, 0 /* flags */);
        codec.start();
        codecInputBuffers = codec.getInputBuffers();
        codecOutputBuffers = codec.getOutputBuffers();
        extractor.selectTrack(0);
        sawOutputEOS = false;
        sawInputEOS = false;

        if (events != null) handler.post(new Runnable() { @Override public void run() { events.onStart(mime, sampleRate, channels, duration, bitrate);  } });
    }
	
    @Override
    public void run()  {
    	prepareDecoderRessources();
    	Log.d(TAG, "RUN() State: " + state.toString() + " sawOutputEOS: " + sawOutputEOS + " sawInputEOS: " + sawInputEOS);
        if (events != null) handler.post(new Runnable() { @Override public void run() { events.onDecoding(); }});
        while (!sawOutputEOS && !state.isStopped()) {
        	/*
        	if(sawOutputEOS || stop){
        		//if (events != null) handler.post(new Runnable() { @Override public void run() { events.onStop(numBytesDecoded, sawInputEOS); }});
        		Log.d(TAG, "setting onHold");
        		setOnHold();
        	}
        	waitDecoding();
        	*/
            if (!sawInputEOS) {
                int inputBufIndex = codec.dequeueInputBuffer(kTimeOutUs);
                if (inputBufIndex >= 0) {
                    ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                    // store encoded sample in ByteBuffer 'dstBuf'
                    int sampleSize = extractor.readSampleData(dstBuf, 0 /* offset */);
                    
                    if (sampleSize < 0) {
                        Log.d(TAG, "saw input EOS.");
                        sawInputEOS = true;
                        sampleSize = 0;
                    } else {
                        presentationTimeUs = extractor.getSampleTime();
                        final int percent =  (duration == 0)? 0 : (int) (100 * presentationTimeUs / duration);
                        if (events != null) handler.post(new Runnable() { @Override public void run() { events.onDecoderUpdate(percent, presentationTimeUs / 1000, duration / 1000);  } });                        
                    }
                    // submit valid date to codec for decoding
                    codec.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                    if (!sawInputEOS) {
                        extractor.advance();
                    }
                    samplesEncoded += sampleSize;
                   // Log.d(TAG, "Input Buffer Info dstBuffer:" + dstBuf.capacity() + " sampleSize: " + sampleSize + " presentationTimeUs: " + presentationTimeUs + " inputBufIndex: " + inputBufIndex + " bytesEncoded: " + samplesEncoded);
                }
            }
            
            // decode to PCM and push it to the stretcher thread via pipe
            int res = codec.dequeueOutputBuffer(info, kTimeOutUs);
            if (res >= 0) {
                int outputBufIndex = res;
                ByteBuffer buf = codecOutputBuffers[outputBufIndex];
                numBytesDecoded += info.size;
                //Log.d(TAG, "Output Buffer Info numBytesDecoded: " + numBytesDecoded + " info.size: " + info.size);
                
                if(chunk.length != info.size){
                	chunk = new byte[info.size];
                	Log.d(TAG, "CHNUNK REALLOC WITH: " + info.size);
                }
                
                buf.get(chunk); // Read the buffer all at once
                buf.clear(); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET THIS SAME BUFFER BAD THINGS WILL HAPPEN
                
                try {
                	bos.write(chunk, 0, info.size);
					//out.flush();
					//Log.d(TAG, "File out writing " + info.size);
				} catch (IOException e) {
					Log.d(TAG, "IOException out.write: " + e.getMessage());
					e.printStackTrace();
				}
                
                codec.releaseOutputBuffer(outputBufIndex, false /* render */);
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d(TAG, "saw output EOS.");
                    sawOutputEOS = true;
                }

            } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                codecOutputBuffers = codec.getOutputBuffers();
                Log.d(TAG, "output buffers have changed.");
            } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat oformat = codec.getOutputFormat();
                Log.d(TAG, "output format has changed to " + oformat);
            }

        }
        
//        int file_size = Integer.parseInt(String.valueOf(decodedDataFile.length()));
        int file_size = Integer.parseInt(String.valueOf(decodedDataFile.length()));
        state.set(DecoderState.STOPPED);
        Log.d(TAG, "Decoder thread is terminated...File size in byte:" + file_size + " State: " + state.toString());
        if (events != null) handler.post(new Runnable() { @Override public void run() { events.onStop(numBytesDecoded, sawInputEOS, duration / 1000); }});
        releaseDecoderRessources();
    }
    
    public void releaseDecoderRessources(){
    	Log.d(TAG, "releaseDecoderRessources()");
    	
        try {
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

        if(codec != null) {
			codec.stop();
			codec.release();
			codec = null;
		}
        
        extractor.release();
        extractor = null;
      //  numBytesDecoded = 0;
    }
    
    public Double getMP3Length(){
    	MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever(); 
    	metaRetriever.setDataSource(this.sourcePath);

    	String duration =  metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    	double dur = Double.parseDouble(duration);
    	String seconds = String.valueOf(dur / 1000);
    	Log.d(TAG, "MP3: " + this.sourcePath + " Lenght in sec: "+ seconds + " Duration:  " + dur);

    	metaRetriever.release();    
    	return Double.parseDouble(seconds);
    }
    
    public int calculateDecodedStreamLenght(){
    	double mp3LenghtInSec = getMP3Length();
    	return (int) (mp3LenghtInSec * 44100 * 2 * 2);
    }
}


