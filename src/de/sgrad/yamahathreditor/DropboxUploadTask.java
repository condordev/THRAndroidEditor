package de.sgrad.yamahathreditor;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import de.sgrad.yamahathreditor.SysExCommands.THRModel;


public class DropboxUploadTask extends AsyncTask<Void, Long, Boolean> {

    private UploadRequest mRequest = null;
    private String mErrorMsg;
    private MainActivity activity;
    private File libraryFile = null;
    FileInputStream fis = null;


    public DropboxUploadTask(MainActivity activity) {
        // We set the context this way so we don't accidentally leak activities
        //mContext = context.getApplicationContext();
    	this.activity = activity;
    	
    }
    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            // By creating a request, we get a handle to the putFile operation, so we can cancel it later if we want to
        	libraryFile = Patch.getLibraryFile(activity.model);
            fis = new FileInputStream(libraryFile);
            mErrorMsg = null;
            
            mRequest = activity.getDropboxDBApi().putFileOverwriteRequest(getDropboxFilePath(activity.model), fis, libraryFile.length(), new ProgressListener() {
                @Override
                public long progressInterval() {
                    // Update the progress bar every half-second or so
                    return 10;
                }

                @Override
                public void onProgress(long bytes, long total) {
                    publishProgress(bytes);
                }
            });

            if (mRequest != null) {
                mRequest.upload();

                return true;
            }

        } catch (DropboxUnlinkedException e) {
            // This session wasn't authenticated properly or user unlinked
            mErrorMsg = "This app wasn't authenticated properly.";
        } catch (DropboxFileSizeException e) {
            // File size too big to upload via the API
            mErrorMsg = "This file is too big to upload";
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            mErrorMsg = "Upload canceled";
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to automatically log the user out in this case.
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be thumbnailed)
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
            } else {
                // Something else
            }
            // This gets the Dropbox error, translated into the user's language
            mErrorMsg = e.body.userError;
            if (mErrorMsg == null) {
                mErrorMsg = e.body.error;
            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
            mErrorMsg = "Network error.  Try again.";
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
            mErrorMsg = "Dropbox error.  Try again.";
        } catch (DropboxException e) {
            // Unknown error
            mErrorMsg = "Unknown error.  Try again.";
        } catch (FileNotFoundException e) {
        }
        
		if(mErrorMsg != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Dropbox Upload Error: " + mErrorMsg, Toast.LENGTH_LONG).show();

                }
            });	
		}
		Log.d(MainActivity.TAG, "mErrorMsg: " + mErrorMsg);
        return false;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        int percent = (int)(100.0*(double)progress[0]/libraryFile.length() + 0.5);
        Log.d(MainActivity.TAG, "Uploading " + percent  + " % ");
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            showToast("File successfully uploaded");
        } else {
            showToast(mErrorMsg);
        }
        
        try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private void showToast(final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, msg , Toast.LENGTH_LONG).show();

            }
        });	
    }
    
	public static String getDropboxFilePath(THRModel model){
		switch (model) {
			case THR10:
				return "/THR5_10.YDL";
			case THR10C:
				return "/THR10C.YDL";
			case THR10X:
				return "/THR10X.YDL";
			case THR5:
				return "/THR5_10.YDL";
			case THR5A:
				return "/THR5A.YDL";
			default:
				return null;
		}
	}
}
