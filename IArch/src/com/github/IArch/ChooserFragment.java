package com.github.IArch;

import java.io.File;
import java.util.ArrayList;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ChooserFragment extends Fragment {

	private ListView listView;
	private ListViewAdapter customGridAdapter;
	public static File fileName = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		View chooserView = inflater.inflate(R.layout.fragment_chooser, container, false);
        
		listView = (ListView) chooserView.findViewById(R.id.listView);
		customGridAdapter = new ListViewAdapter(getActivity(), R.layout.row_list, getData());
		listView.setAdapter(customGridAdapter);
		
		//handle item click
				listView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
						//Toast.makeText(Gallery.this, position + "#Selected",
						//		Toast.LENGTH_SHORT).show();
						
						//get files in images directory
						File path = new File(Environment.getExternalStoragePublicDirectory(
								Environment.DIRECTORY_PICTURES) + "/iArch/");
					    File[] imageFiles = path.listFiles();
					    //use it like imageFiles[position]
					    fileName = imageFiles[position];
					    System.out.println("image selected : " + imageFiles[position]);
						if (fileName.isDirectory()) {
							System.out.println("THIS IS A DIRECTORY");
							
							//Intent intent = new Intent(Chooser.this, Gallery.class);
						    //startActivity(intent);
							// Create new fragment and transaction
							Fragment newFragment = new GalleryFragment();
							FragmentTransaction transaction = getFragmentManager().beginTransaction();

							// Replace whatever is in the fragment_container view with this fragment,
							// and add the transaction to the back stack
							transaction.replace(R.id.container, newFragment);
							transaction.addToBackStack(null);

							// Commit the transaction
							transaction.commit();
						} else {
							//Intent intent = new Intent(Chooser.this, ImageDetails.class);
						    //startActivity(intent);
						}
					}

				});
		
		return chooserView;
	}
	
	private ArrayList<ImageItem> getData() {
		final ArrayList<ImageItem> imageItems = new ArrayList<ImageItem>();
		
		File path = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES) + "/iArch/");
	    File[] imageFiles = path.listFiles();
	    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.folder_icon_small);
	    
	    if (imageFiles != null) {
	    	for (int i = 0; i < imageFiles.length; i++) {
	    		String folderName = imageFiles[i].toString();
	    		String[] shortFolderName = folderName.split("/");
	    		imageItems.add(new ImageItem(icon, shortFolderName[6]));
	    		//imageItems.add(new ImageItem(decodeSampledBitmapFromFile(imageFiles[i].getAbsolutePath(), 200, 200), shortFolderName[6]));
	    	}
	    }
	    
		return imageItems;
	}
	
}
