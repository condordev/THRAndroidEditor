package de.sgrad.yamahathreditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import de.sgrad.yamahathreditor.SysExCommands.THRModel;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class DropboxDownloadTask extends AsyncTask<Void, Long, Boolean> {
	
	/* file list
	     List<String> arrList = new ArrayList<String>();
		 Entry entries = mDropboxApi.metadata("/metadata", 100, null, true, null);
		
		 for (Entry e : entries.contents) {
		     if (!e.isDeleted && !e.isDir) {
		         arrList.add(e.fileName);
		     }
		 }
	 */
	
	MainActivity activity = null;
	private String mErrorMsg = null;
	Entry existingEntry = null;
	
	public DropboxDownloadTask(MainActivity activity){
		this.activity = activity;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		FileOutputStream outputStream = null;
		DropboxFileInfo info = null;
		mErrorMsg = null;
		
		File libraryFile = Patch.getLibraryFile(activity.model);

		try {
			outputStream = new FileOutputStream(libraryFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return false;
		}

		try {
			existingEntry = activity.getDropboxDBApi().metadata(getDropboxFileName(activity.model), 1, null, false, null);
			Log.d(MainActivity.TAG, "metadata: " + existingEntry.rev + " Path: " + existingEntry.path + " parent path: " + 
					existingEntry.parentPath() + " Root: " + existingEntry.root + " size: " + existingEntry.size + " Name: " + existingEntry.fileName() + 
					" modified: " + existingEntry.modified);
			
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Downloading " + existingEntry.fileName() + " from Dropbox./n Version: " + existingEntry.modified + 
                    		" /n Revision: " + existingEntry.rev, Toast.LENGTH_SHORT).show();

                }
            });			
			
			info = activity.getDropboxDBApi().getFile(getDropboxFileName(activity.model), null,outputStream, null);
			Log.d(MainActivity.TAG, "File written to : " + libraryFile.getAbsolutePath() + " Size: " + info.getFileSize());
			
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			
			return true;
			
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
	
	public String getDropboxFileName(THRModel model){
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
	
    @Override
    protected void onPostExecute(Boolean result) {
    	super.onPostExecute(result);
        if (result) {
            activity.runOnUiThread(new Runnable() {
            	
                @Override
                public void run() {
                    Toast.makeText(activity, "Download finished. File: " + existingEntry.fileName() + " from Dropbox./n Version: " + existingEntry.modified + 
                    		" /n Revision: " + existingEntry.rev, Toast.LENGTH_LONG).show();

                }
            });
            activity.patch.loadLibraryContent();
                    	
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Could not download from Dropbox." + mErrorMsg, Toast.LENGTH_SHORT).show();

                }
            });	
        }
    	
    }
	
}
