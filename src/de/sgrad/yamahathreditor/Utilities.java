package de.sgrad.yamahathreditor;

public class Utilities {
	 
    /**
     * Function to convert seconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     * */
    
    public static String secondsToTimer(int seconds) {
        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;
        String secondsString = "";

        int minutes = seconds / SECONDS_IN_A_MINUTE;
        seconds -= minutes * SECONDS_IN_A_MINUTE;

        int hours = minutes / MINUTES_IN_AN_HOUR;
        minutes -= hours * MINUTES_IN_AN_HOUR;

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}
        
        if(hours > 0)
        	return hours + ":" + minutes + ":" + secondsString;
        else
        	return minutes + ":" + secondsString;
    }
    
    /**
     * Function to convert time to seconds Format: String 01:30 -> 90 
     * Minutes:Seconds
     * */
    public static int timerToSeconds(String time) {
        final int SECONDS_IN_A_MINUTE = 60;
        
        String[] units = time.split(":"); //will break the string up into an array
        int minutes = Integer.parseInt(units[0]); //first element
        int seconds = Integer.parseInt(units[1]); //second element
        int duration = SECONDS_IN_A_MINUTE * minutes + seconds; //add up our values
        return duration;
    }
    
    
    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     * */
    public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";
 
        // Convert total duration into time
           int hours = (int)( milliseconds / (1000*60*60));
           int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
           int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
           // Add hours if there
           if(hours > 0){
               finalTimerString = hours + ":";
           }
 
           // Prepending 0 to seconds if it is one digit
           if(seconds < 10){
               secondsString = "0" + seconds;
           }else{
               secondsString = "" + seconds;}
 
           finalTimerString = finalTimerString + minutes + ":" + secondsString;
 
        // return timer string
        return finalTimerString;
    }
 
    /**
     * Function to get Progress percentage
     * @param currentDuration
     * @param totalDuration
     * */
    public int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;
 
        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);
 
        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;
 
        // return percentage
        return percentage.intValue();
    }
 
    /**
     * Function to change progress to timer
     * @param progress -
     * @param totalDuration
     * returns current duration in milliseconds
     * */
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);
 
        // return current duration in milliseconds
        return currentDuration * 1000;
    }
}
