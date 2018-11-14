package de.sgrad.yamahathreditor;

import android.os.Handler;

public abstract class SimpleTimer {
    abstract void onTimer();

    private Runnable runnableCode = null;
    private Handler handler = new Handler();
    public int intervalMS = 0;

    void startDelayed(final int intervalMS, int delayMS) {
        runnableCode = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(runnableCode, intervalMS);
                onTimer();
            }
        };
        handler.postDelayed(runnableCode, delayMS);
    }

    void startWithInterval(final int intervalMS) {
    	this.intervalMS = intervalMS;
        startDelayed(intervalMS, 0);
    }
    
    void start() {
        startDelayed(intervalMS, 0);
    }

    void stop() {
        handler.removeCallbacks(runnableCode);
       // intervalMS = 0;
    }
}
