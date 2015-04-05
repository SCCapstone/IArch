package com.github.IArch;

import java.io.File;
import java.util.ArrayList;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.LruCache;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class GalleryFragment extends Fragment {

	private GridView gridView;
	private GridViewAdapter customGridAdapter;
	public static File fileName = null;
	static LruCache<String, Bitmap> mMemoryCache;
	static DiskLruCache mDiskLruCache;
	final static Object mDiskCacheLock = new Object();
	static boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final String DISK_CACHE_SUBDIR = "thumbnails";


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		setHasOptionsMenu(true);
		View galleryView = inflater.inflate(R.layout.fragment_gallery, container, false);
		getActionBar().setTitle(R.string.title_fragment_gallery);
		
		gridView = (GridView) galleryView.findViewById(R.id.gridView);
		customGridAdapter = new GridViewAdapter(getActivity(), R.layout.row_grid, getData());
		gridView.setAdapter(customGridAdapter);
		gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
		gridView.setMultiChoiceModeListener(new MultiChoiceModeListener());
		
		//handle item click
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				//get files in images directory
				String longFileName = ChooserFragment.folderName.toString();
				String[] shortFileName = longFileName.split("/");
				File path = new File(Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_PICTURES) + "/iArch/" + shortFileName[6]);
			    File[] imageFiles = path.listFiles();
			    //use it like imageFiles[position]
			    fileName = imageFiles[position];
			    System.out.println("image selected : " + imageFiles[position]);
				
			    // Create new fragment and transaction
				Fragment newFragment = new ImageDetailsFragment();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();

				// Replace whatever is in the fragment_container view with this fragment,
				// and add the transaction to the back stack
				transaction.replace(R.id.container, newFragment);
				transaction.addToBackStack(null);

				// Commit the transaction
				transaction.commit();
			    
			}

		});
		
		//sync datastores so that no fields will be empty when picture clicked the first 
		//time or after user disconnects and reconnects dropbox
		DbxDatastore datastore;
		try {
			String[] splitFile = ChooserFragment.folderName.toString().split("/");
			if (MainActivity.mAccountManager.hasLinkedAccount()) {	
				datastore = MainActivity.mDatastoreManager.openDatastore(splitFile[6]);
				datastore.sync();
				datastore.close();
			}
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Get max available VM memory, exceeding this amount will throw an
	    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
	    // int in its constructor.
	    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	    // Use 1/8th of the available memory for this memory cache.
	    final int cacheSize = maxMemory / 8;

	    //RetainFragment retainFragment = RetainFragment.findOrCreateRetainFragment(getFragmentManager());
	    //mMemoryCache = retainFragment.mRetainedCache;
	    //if (mMemoryCache == null) {
	    	mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
	    		@Override
	    		protected int sizeOf(String key, Bitmap bitmap) {
	    			// The cache size will be measured in kilobytes rather than
	    			// number of items.
	    			return bitmap.getByteCount() / 1024;
	    		}
	    	};
	    	//retainFragment.mRetainedCache = mMemoryCache;
	    //}
	    
	    
	    // Initialize disk cache on background thread
	    File cacheDir = getDiskCacheDir(getActivity(), DISK_CACHE_SUBDIR);
	    new InitDiskCacheTask().execute(cacheDir);

		return galleryView;
	}

	private ActionBar getActionBar() {
	    return getActivity().getActionBar();
	}
	
	private ArrayList<ImageItem> getData() {
		final ArrayList<ImageItem> imageItems = new ArrayList<ImageItem>();
		
		File path = new File(ChooserFragment.folderName.toString());
	    File[] imageFiles = path.listFiles();
	    
	    for (int i = 0; i < imageFiles.length; i++) {
	    	String folderName = imageFiles[i].toString();
	    	String[] shortFolderName = folderName.split("/");
	    	imageItems.add(new ImageItem(imageFiles[i], shortFolderName[7], folderName));
	    }
	    
		return imageItems;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.gallery_fragment, menu);	
	}
		
	public class MultiChoiceModeListener implements GridView.MultiChoiceModeListener {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.setTitle("Select Items");
            mode.setSubtitle("One item selected");
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			View singleView = gridView.getChildAt(position);
			if (checked == true) {
				//item checked
				singleView.setBackgroundColor(getResources().getColor(android.R.color.background_light));
			} else {
				//item unchecked
				singleView.setBackgroundColor(Color.parseColor("#fff3f3f3"));
			}
			
			//count number of items selected; display it at top of screen
			int selectCount = gridView.getCheckedItemCount();
            switch (selectCount) {
            case 1:
                mode.setSubtitle("One item selected");
                break;
            default:
                mode.setSubtitle("" + selectCount + " items selected");
                break;
            }
		}

	}
	
	class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
	    @Override
	    protected Void doInBackground(File... params) {
	        synchronized (mDiskCacheLock) {
	            File cacheDir = params[0];
	            mDiskLruCache = DiskLruCache.openCache(getActivity(), cacheDir, DISK_CACHE_SIZE);
	            mDiskCacheStarting = false; // Finished initialization
	            mDiskCacheLock.notifyAll(); // Wake any waiting threads
	        }
	        return null;
	    }
	}

	// Creates a unique subdirectory of the designated app cache directory. Tries to use external
	// but if not mounted, falls back on internal storage.
	public static File getDiskCacheDir(Context context, String uniqueName) {
	    // Check if media is mounted or storage is built-in, if so, try and use external cache dir
	    // otherwise use internal cache dir
	    final String cachePath = context.getCacheDir().getPath();

	    return new File(cachePath + File.separator + uniqueName);
	}
			
	static class RetainFragment extends Fragment {
	    private static final String TAG = "RetainFragment";
	    public LruCache<String, Bitmap> mRetainedCache;

	    public RetainFragment() {}

	    public static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
	        RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(TAG);
	        if (fragment == null) {
	            fragment = new RetainFragment();
	            fm.beginTransaction().add(fragment, TAG).commit();
	        }
	        return fragment;
	    }

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setRetainInstance(true);
	    }
	}
}
