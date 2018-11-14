package de.sgrad.yamahathreditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

public class DropboxGetFileListTask extends AsyncTask<Void, Long, Boolean> {
	
	MainActivity activity = null;
	private String mErrorMsg = null;
	Entry entries = null;
	PresetFileManager parent = null;
	
	public DropboxGetFileListTask(MainActivity activity, PresetFileManager parent){
		this.activity = activity;
		this.parent = parent;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		mErrorMsg = null;

		try {
			if(activity.mLoggedIn){
				entries = null;
				entries = activity.getDropboxDBApi().metadata("/", 100, null, true, null);
				
	            activity.runOnUiThread(new Runnable() {
	                @Override
	                public void run() {
	                    Toast.makeText(activity, "Requesting file list from dropbox.", Toast.LENGTH_SHORT).show();

	                }
	            });	
	            return true;

			}else{
	 			activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(activity, "Please log in first in your Dropbox account.", Toast.LENGTH_LONG).show();
					}
				});				
				return false;
			}
			
		} catch (DropboxUnlinkedException e) {
			// The AuthSession wasn't properly authenticated or user unlinked.
		} catch (DropboxPartialFileException e) {
			// We canceled the operation
			mErrorMsg = "Download canceled";
		} catch (DropboxServerException e) {
			// Server-side exception. These are examples of what could happen,
			// but we don't do anything special with them here.
			if (e.error == DropboxServerException._304_NOT_MODIFIED) {
				// won't happen since we don't pass in revision with metadata
			} else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
				// Unauthorized, so we should unlink them. You may want to automatically log the user out in this case.
				Log.d(MainActivity.TAG, "_401_UNAUTHORIZED ");
			} else if (e.error == DropboxServerException._403_FORBIDDEN) {
				// Not allowed to access this
				Log.d(MainActivity.TAG, "_403_FORBIDDEN ");
			} else if (e.error == DropboxServerException._404_NOT_FOUND) {
				// path not found
				Log.d(MainActivity.TAG, "_404_NOT_FOUND ");
			} else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
				// too many entries to return
				Log.d(MainActivity.TAG, "_406_NOT_ACCEPTABLE ");
			} else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
				Log.d(MainActivity.TAG, "_415_UNSUPPORTED_MEDIA ");
			} else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
				// user is over quota
				Log.d(MainActivity.TAG, "_507_INSUFFICIENT_STORAGE ");
			} else {
				Log.d(MainActivity.TAG, "Something else ");
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
		}
		
		if(mErrorMsg != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Dropbox Download Error: " + mErrorMsg, Toast.LENGTH_LONG).show();

                }
            });	
		}
		Log.d(MainActivity.TAG, "mErrorMsg: " + mErrorMsg);
		
		return false;
	}
	
	
    protected void onPostExecute(Boolean result) {
    	super.onPostExecute(result);
        if (result) {
			for (Entry e : entries.contents) {
				if (!e.isDeleted && !e.isDir) {
	                if(e.fileName().endsWith(".YDP") || e.fileName().endsWith(".YDL")){
	                    parent.listData.add(e.fileName());
	        			Log.d(MainActivity.TAG, "metadata: " + e.rev + " Path: " + e.path + " parent path: " + 
	        					e.parentPath() + " Root: " + e.root + " size: " + e.size + " Name: " + e.fileName() + 
	        					" modified: " + e.modified);
	                }					
				}
			}
        	/*
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   Toast.makeText(activity, "File List  " + existingEntry.fileName() + " from Dropbox./n Version: " + existingEntry.modified + 
                    		" /n Revision: " + existingEntry.rev, Toast.LENGTH_LONG).show();

                }
            });
            */
			parent.listAdapter.notifyDataSetChanged();   
			parent.dropboxList = true;
			parent.tvPresetName.setText("Content of your Dropbox account:");
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Could not get file list from Dropbox." + mErrorMsg, Toast.LENGTH_SHORT).show();

                }
            });	
        }
    	
    }

}
