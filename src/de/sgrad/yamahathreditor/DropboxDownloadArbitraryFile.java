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

public class DropboxDownloadArbitraryFile extends AsyncTask<Void, Long, Boolean> {
	
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
	Entry entry = null;
	PresetFileManager parent = null;
	String fileName = null;
	
	public DropboxDownloadArbitraryFile(MainActivity activity, String fileName, PresetFileManager parent){
		this.activity = activity;
		this.parent = parent;
		this.fileName = fileName;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		FileOutputStream outputStream = null;
		DropboxFileInfo info = null;
		mErrorMsg = null;
		File newFile = null;
		
		try {
			newFile = new File(parent.sdcardTHRFolderDir.getAbsolutePath().concat("/").concat(fileName));
			outputStream = new FileOutputStream(newFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return false;
		}

		try {
			entry = activity.getDropboxDBApi().metadata("/".concat(fileName), 1, null, false, null);
			Log.d(MainActivity.TAG, "metadata: " + entry.rev + " Path: " + entry.path + " parent path: " + 
					entry.parentPath() + " Root: " + entry.root + " size: " + entry.size + " Name: " + entry.fileName() + 
					" modified: " + entry.modified);
			
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Downloading " + entry.fileName() + " from Dropbox./n Version: " + entry.modified + 
                    		" /n Revision: " + entry.rev, Toast.LENGTH_SHORT).show();

                }
            });			
			
			info = activity.getDropboxDBApi().getFile("/".concat(fileName), null,outputStream, null);
			Log.d(MainActivity.TAG, "File written to : " + newFile.getAbsolutePath() + " Size: " + info.getFileSize());
			
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
	
    @Override
    protected void onPostExecute(Boolean result) {
    	super.onPostExecute(result);
        if (result) {
            activity.runOnUiThread(new Runnable() {
            	
                @Override
                public void run() {
                    Toast.makeText(activity, "Download " + entry.fileName() + " finished. Open your SDCard folder", Toast.LENGTH_LONG).show();
                }
            });
                    	
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

