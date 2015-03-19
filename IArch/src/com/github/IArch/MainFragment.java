package com.github.IArch;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainFragment extends Fragment {

	static Button mLinkButton;
	static final int REQUEST_LINK_TO_DBX = 0;
	
    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mLinkButton = (Button) rootView.findViewById(R.id.link_button);
        
        //dropbox button listener 
      	mLinkButton.setOnClickListener(new OnClickListener() { 
      	@Override 
      	    public void onClick(View v) { 
      	    	onClickLinkToDropbox(); 
      	    } 
      	}); 
        
        return rootView;
    }
    
    private void onClickLinkToDropbox() {
    	
    	if (MainActivity.mAccountManager.hasLinkedAccount()) {
    		//if already linked to dropbox and button is clicked, unlink
    		MainActivity.mAccountManager.unlink();
        	showUnlinkedView();
    	} else {
    		MainActivity.mAccountManager.startLink(getActivity(), REQUEST_LINK_TO_DBX);
    		showLinkedView();
    	}
    }
    
    private void showLinkedView() {
    	mLinkButton.setText("Unlink from Dropbox");
        //navDrawerItems[4] = "Logout";
        //mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, navDrawerItems));
    }

    private void showUnlinkedView() {
    	mLinkButton.setText("Connect to Dropbox");
    	//navDrawerItems[4] = "Login";
    	//mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, navDrawerItems));
    }
    
}