package de.sgrad.yamahathreditor;

import java.util.LinkedHashSet;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class SongManager{
	private static final String TAG = "SONG MANAGER";
	private LinkedHashSet<SongDetails> songs = null;
	private LinkedHashSet<String> albums;
	private LinkedHashSet<String> artists;
	private ContentResolver contentResolver;

	public String lastAlbumName = "";
	
	/*
	SongManager(ContentResolver contentResolver){
		this.contentResolver = contentResolver;
	}
	*/
	
	void setContentResolver(ContentResolver contentResolver){
		this.contentResolver = contentResolver;
	}
	
	
	public class SongDetails {
        String songTitle = "";
        String songArtist = "";
        String albumName = "";
        //song location on the device
        String songData = "";
	}
	
	public LinkedHashSet<SongDetails> getSongList(){
		return songs;
	}
	
	public LinkedHashSet<String> getAlbumList(){
		
        String[] columns = { android.provider.MediaStore.Audio.Albums._ID, android.provider.MediaStore.Audio.Albums.ALBUM };

        Cursor cursor = contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, columns, null, null, null);
        albums = new LinkedHashSet<String>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
            	albums.add(cursor.getString(cursor.getColumnIndex(android.provider.MediaStore.Audio.Albums.ALBUM)));
            	Log.d(TAG, "Album: " + cursor.getString(cursor.getColumnIndex(android.provider.MediaStore.Audio.Albums.ALBUM)));
            } while (cursor.moveToNext());
        }
	
		return albums;
	}
	
	public LinkedHashSet<String> getArtistList(){
		
        String[] columns = { android.provider.MediaStore.Audio.Artists._ID, android.provider.MediaStore.Audio.Artists.ARTIST};

        Cursor cursor = contentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, columns, null, null, null);
        artists = new LinkedHashSet<String>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
            	artists.add(cursor.getString(cursor.getColumnIndex(android.provider.MediaStore.Audio.Artists.ARTIST)));
            	Log.d(TAG, "Artist: " + cursor.getString(cursor.getColumnIndex(android.provider.MediaStore.Audio.Artists.ARTIST)));
            } while (cursor.moveToNext());
        }
	
		return artists;
	}
	
	public void retrievAlbumTracks(String albumName){
		Log.d(TAG, "retrievAlbumTracks for Album: " + albumName);
		lastAlbumName = albumName;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        final String[] column = new String[] {
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA};        

        String where = android.provider.MediaStore.Audio.Media.ALBUM + "=?";
        String whereVal[] = {albumName};
        String orderBy = android.provider.MediaStore.Audio.Media.DISPLAY_NAME;
        Cursor cursor = null;
        try{
	        cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,column, where, whereVal, orderBy);
	
	        if (cursor != null) {
	            songs = new LinkedHashSet<SongDetails>(cursor.getCount());
	            cursor.moveToFirst();
	            SongDetails details;
	            while (!cursor.isAfterLast()) {
	                //collecting song information and store in array,
	                //moving to the next row
	                details = new SongDetails();
	                details.songTitle = cursor.getString(0);
	                details.songArtist = cursor.getString(1);
	                details.albumName = cursor.getString(2);
	                details.songData = cursor.getString(3);
	                songs.add(details);
	                cursor.moveToNext();
	                Log.d(TAG, "song title " + details.songTitle);
	            }
	        }
	    } catch (Exception ex) {
	
	    } finally {
	        if (cursor != null) {
	            cursor.close();
	        }
	    }
        /*
        if (cursor.moveToFirst()) {
            do {
                Log.v("Vipul", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
            } while (cursor.moveToNext());
        }
        */
	}
	
	public void retrievArtistTracks(String artistName){
		Log.d(TAG, "retrievArtistTracks for Artist " + artistName);
		//lastAlbumName = artistName;
        final String[] column = new String[] {
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA};        

        String where = android.provider.MediaStore.Audio.Media.ARTIST + "=?";
        String whereVal[] = {artistName};
        String orderBy = android.provider.MediaStore.Audio.Media.DISPLAY_NAME;
        Cursor cursor = null;
        try{
	        cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,column, where, whereVal, orderBy);
	
	        if (cursor != null) {
	            songs = new LinkedHashSet<SongDetails>(cursor.getCount());
	            cursor.moveToFirst();
	            SongDetails details;
	            while (!cursor.isAfterLast()) {
	                //collecting song information and store in array,
	                //moving to the next row
	                details = new SongDetails();
	                details.songTitle = cursor.getString(0);
	                details.songArtist = cursor.getString(1);
	                details.albumName = cursor.getString(2);
	                details.songData = cursor.getString(3);
	                songs.add(details);
	                cursor.moveToNext();
	                Log.d(TAG, "song title " + details.songTitle);
	            }
	        }
	    } catch (Exception ex) {
	
	    } finally {
	        if (cursor != null) {
	            cursor.close();
	        }
	    }
        /*
        if (cursor.moveToFirst()) {
            do {
                Log.v("Vipul", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
            } while (cursor.moveToNext());
        }
        */
	}
	
	public void retrieveAllSongs() {
       //creating selection for the database
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        final String[] projection = new String[] {
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA};

        //creating sort by for database
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";

        //stating pointer
        Cursor cursor = null;

        try {
            //the table for query
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            // query the db
            //cursor = getBaseContext().getContentResolver().query(uri, projection, selection, null, sortOrder);
            cursor = contentResolver.query(uri, projection, selection, null, sortOrder);
            if (cursor != null) {

                //create array for incoming songs
                songs = new LinkedHashSet<SongDetails>(cursor.getCount());

                //go to the first row
                cursor.moveToFirst();

                SongDetails details;

                while (!cursor.isAfterLast()) {
                    //collecting song information and store in array,
                    //moving to the next row
                    details = new SongDetails();
                    details.songTitle = cursor.getString(0);
                    details.songArtist = cursor.getString(1);
                    details.albumName = cursor.getString(2);
                    details.songData = cursor.getString(3);
                    songs.add(details);
                    cursor.moveToNext();
                    //Log.d(TAG, "song data" + details.songTitle);

                }
            }
        } catch (Exception ex) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
