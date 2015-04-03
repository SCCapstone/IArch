package com.github.IArch;

import java.io.File;
import java.util.ArrayList;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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
				//Toast.makeText(Gallery.this, position + "#Selected",
				//		Toast.LENGTH_SHORT).show();
				
				//get files in images directory
				String longFileName = ChooserFragment.folderName.toString();
				String[] shortFileName = longFileName.split("/");
				File path = new File(Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_PICTURES) + "/iArch/" + shortFileName[6]);
			    File[] imageFiles = path.listFiles();
			    //use it like imageFiles[position]
			    fileName = imageFiles[position];
			    System.out.println("image selected : " + imageFiles[position]);
				
			    //Intent intent = new Intent(GalleryFragment.this, ImageDetails.class);
			    //startActivity(intent);
			    //Intent intent = new Intent(Chooser.this, Gallery.class);
			    //startActivity(intent);
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
	    	
	    	imageItems.add(new ImageItem(decodeSampledBitmapFromFile(imageFiles[i].getAbsolutePath(), 200, 200), shortFolderName[7], folderName));
	    }
	    
		return imageItems;
	}
	
	public static Bitmap decodeSampledBitmapFromFile(String file, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(file, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(file, options);
	}
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }
    //System.out.println(inSampleSize);
    return inSampleSize;
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
}
