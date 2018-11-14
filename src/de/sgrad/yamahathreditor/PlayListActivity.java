package de.sgrad.yamahathreditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import de.sgrad.yamahathreditor.SongManager.SongDetails;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

public class PlayListActivity extends ListActivity {
	
	private static final String TAG = "PLAYLIST ACTIVITY";
	
	public static final String	ALL_TRACKS = "All Tracks"; 
	public static final String	ALBUM = "Album"; 
	public static final String	ARTIST = "Artist";
	public static final String	PERSISTED_DATA = "Persisted";
	
	private Spinner metaDataSpinner;
	private Button btnBack;
	String category = ALL_TRACKS;
	String categoryBackButton = "";
	LinkedHashSet<SongDetails> songsList = new LinkedHashSet<SongDetails>();
	final ArrayList<HashMap<String, String>> listData = new ArrayList<HashMap<String, String>>();
	final PlayListActivity self = this;
	ListAdapter adapter = null;
	SongManager sm = null;
	boolean listIsShowingTracks = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);
		metaDataSpinner = (Spinner) findViewById(de.sgrad.yamahathreditor.R.id.metadata_spinner);
		btnBack = (Button) findViewById(de.sgrad.yamahathreditor.R.id.btnBack);
		
//		final SongManager sm = new SongManager(this.getContentResolver());
		sm = PlayerFragment.songManager;		
		ArrayList<String> metaDataCategories = new ArrayList<String>();
		metaDataCategories.add(ALL_TRACKS);
		metaDataCategories.add(ALBUM);
		metaDataCategories.add(ARTIST);
		
		Log.d(TAG, "PlayListActivity::onCreate(): " + sm.lastAlbumName);
		
		if(!sm.lastAlbumName.isEmpty()){
			Log.d(TAG, "LAST ALBUM NAME: " + sm.lastAlbumName);
			category = PERSISTED_DATA;
			getTrackListFromAlbum(sm.lastAlbumName);
		}
		
		final ArrayAdapter<String> metaDataSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, metaDataCategories);
		metaDataSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		metaDataSpinner.setAdapter(metaDataSpinnerAdapter);
		
		// handle the spinner item selections
		metaDataSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (!category.equals(PERSISTED_DATA)){
					category = (String)metaDataSpinner.getItemAtPosition(position);
					
					if (category.equals(ALL_TRACKS)){
						Log.d(TAG, "ALL TRACKS selected");
						listData.clear();
						sm.retrieveAllSongs();
						songsList = sm.getSongList();
						
						for(SongManager.SongDetails song : songsList){
							HashMap<String, String> songMap = new HashMap<String, String>();
							songMap.put("songTitle", song.songTitle);
							songMap.put("albumName", song.albumName);
							songMap.put("songPath", song.songData);
	
							listData.add(songMap);
						}
						
						listIsShowingTracks = true;
	
						// Adding menuItems to ListView
						//adapter = new SimpleAdapter(self, listData, R.layout.playlist_item, new String[] { "songTitle", "albumName", "songPath"  }, new int[] { R.id.songTitle,R.id.albumName,R.id.songPath });
						adapter = new SimpleAdapter(self, listData, R.layout.playlist_item, new String[] { "songTitle"}, new int[] { R.id.songTitle});
						setListAdapter(adapter);					
						
					}else if (category.equals(ALBUM)){
						Log.d(TAG, "ALBUM selected");
						listData.clear();
						for(String albumName : sm.getAlbumList()){
							HashMap<String, String> albumMap = new HashMap<String, String>();
							albumMap.put("albumName", albumName);
							listData.add(albumMap);
						}
						listIsShowingTracks = false;
						
						// Adding menuItems to ListView
						adapter = new SimpleAdapter(self, listData, R.layout.album_item, new String[] {"albumName"}, new int[] {R.id.albumName });
						setListAdapter(adapter);					
					}else if (category.equals(ARTIST)){
						Log.d(TAG, "ARTIST selected");
						listData.clear();
						for(String artistName : sm.getArtistList()){
							HashMap<String, String> artistMap = new HashMap<String, String>();
							artistMap.put("artistName", artistName);
							listData.add(artistMap);
						}
						
						listIsShowingTracks = false;
						
						// Adding menuItems to ListView
						adapter = new SimpleAdapter(self, listData, R.layout.album_item, new String[] {"artistName"}, new int[] {R.id.albumName });
						setListAdapter(adapter);							
					}
					
					metaDataSpinnerAdapter.notifyDataSetChanged();
				}else{
					// allow selection of new Album, Artist etc. category
					category = (String)metaDataSpinner.getItemAtPosition(position);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// not used
			}
		});

		// selecting single ListView item
		ListView lv = getListView();
		
		// listening to single list item click
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//String path = (String) parent.getItemAtPosition(position);
				//String path = songsList.get(songIndex).songData;
				
				Log.d(TAG, "setOnItemClickListener: position: " + position + " category: " + category + " ShowingTracks: " + listIsShowingTracks + " ID: " + id + " selected: " + listData.get(position));
				
				if (category.equals(ALL_TRACKS)){
					if(listIsShowingTracks){
						String path = listData.get(position).get("songPath");
						String title = listData.get(position).get("songTitle");
						
						Log.d(TAG, "setOnItemClickListener: path: " + path + " title: " + title);
						
						// Starting new intent
						Intent in = new Intent(getApplicationContext(), MainActivity.class);
						// Sending songIndex to PlayerActivity
						in.putExtra("songPath", path);
						in.putExtra("songTitle", title);
						setResult(100, in);
						// Closing PlayListView
						finish();
					}
					
				}else if (category.equals(ALBUM)){
					String albumName = listData.get(position).get("albumName");
					sm.retrievAlbumTracks(albumName);
					listIsShowingTracks = true;
					
					listData.clear();
					songsList = sm.getSongList();
					
					for(SongManager.SongDetails song : songsList){
						HashMap<String, String> songMap = new HashMap<String, String>();
						songMap.put("songTitle", song.songTitle);
						songMap.put("albumName", song.albumName);
						songMap.put("songPath", song.songData);

						listData.add(songMap);
					}

					// Adding menuItems to ListView
					//adapter = new SimpleAdapter(self, listData, R.layout.playlist_item, new String[] { "songTitle", "albumName", "songPath"  }, new int[] { R.id.songTitle,R.id.albumName,R.id.songPath });
					adapter = new SimpleAdapter(self, listData, R.layout.playlist_item, new String[] { "songTitle"}, new int[] { R.id.songTitle});
					setListAdapter(adapter);						
					
					category = ALL_TRACKS;
					categoryBackButton = ALBUM;
					
				}else if (category.equals(ARTIST)){
					String artistName = listData.get(position).get("artistName");
					sm.retrievArtistTracks(artistName);
					listIsShowingTracks = true;
					
					listData.clear();
					songsList = sm.getSongList();
					
					for(SongManager.SongDetails song : songsList){
						HashMap<String, String> songMap = new HashMap<String, String>();
						songMap.put("songTitle", song.songTitle);
						songMap.put("albumName", song.albumName);
						songMap.put("songPath", song.songData);

						listData.add(songMap);
					}

					// Adding menuItems to ListView
					//adapter = new SimpleAdapter(self, listData, R.layout.playlist_item, new String[] { "songTitle", "albumName", "songPath"  }, new int[] { R.id.songTitle,R.id.albumName,R.id.songPath });
					adapter = new SimpleAdapter(self, listData, R.layout.playlist_item, new String[] { "songTitle"}, new int[] { R.id.songTitle});
					setListAdapter(adapter);						
					
					category = ALL_TRACKS;	
					categoryBackButton = ARTIST;
				}
			}
		});
		
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (categoryBackButton.equals(ALBUM)){
					Log.d(TAG, "BACK to ALBUM");
					listData.clear();
					for(String albumName : sm.getAlbumList()){
						HashMap<String, String> albumMap = new HashMap<String, String>();
						albumMap.put("albumName", albumName);
						listData.add(albumMap);
					}
					
					// Adding menuItems to ListView
					adapter = new SimpleAdapter(self, listData, R.layout.album_item, new String[] {"albumName"}, new int[] {R.id.albumName });
					setListAdapter(adapter);						
					metaDataSpinnerAdapter.notifyDataSetChanged();
					listIsShowingTracks = false;
					category = ALBUM;
					
				}else if (categoryBackButton.equals(ARTIST)){
					Log.d(TAG, "BACK to ARTIST");
					listData.clear();
					for(String artistName : sm.getArtistList()){
						HashMap<String, String> artistMap = new HashMap<String, String>();
						artistMap.put("artistName", artistName);
						listData.add(artistMap);
					}
					
					// Adding menuItems to ListView
					adapter = new SimpleAdapter(self, listData, R.layout.album_item, new String[] {"artistName"}, new int[] {R.id.albumName });
					setListAdapter(adapter);
					listIsShowingTracks = false;
					metaDataSpinnerAdapter.notifyDataSetChanged();
					category = ARTIST;
				}
				
			}
		});

	}
	
	public void getTrackListFromAlbum(String albumName){
		sm.retrievAlbumTracks(albumName);
		
		listData.clear();
		songsList = sm.getSongList();
		
		for(SongManager.SongDetails song : songsList){
			HashMap<String, String> songMap = new HashMap<String, String>();
			songMap.put("songTitle", song.songTitle);
			songMap.put("albumName", song.albumName);
			songMap.put("songPath", song.songData);

			listData.add(songMap);
		}

		// Adding menuItems to ListView
		//adapter = new SimpleAdapter(self, listData, R.layout.playlist_item, new String[] { "songTitle", "albumName", "songPath"  }, new int[] { R.id.songTitle,R.id.albumName,R.id.songPath });
		adapter = new SimpleAdapter(self, listData, R.layout.playlist_item, new String[] { "songTitle"}, new int[] { R.id.songTitle});
		setListAdapter(adapter);				
		
	}

}
