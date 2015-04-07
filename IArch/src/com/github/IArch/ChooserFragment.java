package com.github.IArch;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import android.app.ActionBar;
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
	private ListViewAdapter customListAdapter;
	public static File folderName = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		View chooserView = inflater.inflate(R.layout.fragment_chooser, container, false);
        getActionBar().setTitle(R.string.title_fragment_chooser);
		
		listView = (ListView) chooserView.findViewById(R.id.listView);
		customListAdapter = new ListViewAdapter(getActivity(), R.layout.row_list, getData());
		listView.setAdapter(customListAdapter);
		
		//handle item click
				listView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
						//get projects in iArch directory
						File path = new File(Environment.getExternalStoragePublicDirectory(
								Environment.DIRECTORY_PICTURES) + "/iArch/");
					    File[] projectNames = path.listFiles();
					    //use it like imageFiles[position]
					    folderName = projectNames[position];
					    System.out.println("image selected : " + projectNames[position]);
						if (folderName.isDirectory()) {
							System.out.println("THIS IS A DIRECTORY");
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
							// Create new fragment and transaction
							Fragment newFragment = new ImageDetailsFragment();
							Bundle bundle = new Bundle();
							bundle.putString("EXTRAS_FILENAME", projectNames[position].toString());
							newFragment.setArguments(bundle);
							FragmentTransaction transaction = getFragmentManager().beginTransaction();

							// Replace whatever is in the fragment_container view with this fragment,
							// and add the transaction to the back stack
							transaction.replace(R.id.container, newFragment);
							transaction.addToBackStack(null);

							// Commit the transaction
							transaction.commit();
						}
					}

				});
		
		return chooserView;
	}
	
	private ActionBar getActionBar() {
	    return getActivity().getActionBar();
	}
	
	private ArrayList<LineItem> getData() {
		final ArrayList<LineItem> projectItems = new ArrayList<LineItem>();
		
		File path = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES) + "/iArch/");
	    File[] projectNames = path.listFiles();
	    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.folder_icon_small);
	    
	    if (projectNames != null) {
	    	for (int i = 0; i < projectNames.length; i++) {
	    		String folderName = projectNames[i].toString();
	    		String[] shortFolderName = folderName.split("/");
	    		Date lastMod = new Date(projectNames[i].lastModified());
	    		projectItems.add(new LineItem(icon, shortFolderName[6], "Last Modified: " + lastMod.toString()));
	    	}
	    }
	    
		return projectItems;
	}
	
}
