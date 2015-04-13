package com.github.IArch;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DialogHandler extends DialogFragment {
	File myLocation;
	
    public DialogHandler(File fileLocation) {
    	myLocation = fileLocation;
	}

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.title_delete)
               .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       if (myLocation.isDirectory()) {
                    	   DeleteRecursive(myLocation);
                    	   getActivity().getFragmentManager().popBackStack();
                    	   GalleryFragment.mDiskLruCache.clearCache();
                       } else {
                    	   //delete file
                    	   myLocation.delete();
                    	   getActivity().getFragmentManager().popBackStack();
                    	   GalleryFragment.mDiskLruCache.clearCache();
                       }
                   }
               })
               .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	void DeleteRecursive(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            DeleteRecursive(child);

	    fileOrDirectory.delete();
	}
	
}