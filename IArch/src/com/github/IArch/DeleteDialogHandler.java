package com.github.IArch;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DeleteDialogHandler extends DialogFragment {
	File myLocation = new File(getArguments().getString("file"));
	
    public DeleteDialogHandler() {
	}

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.title_delete)
               .setPositiveButton(R.string.title_accept, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   DeleteRecursive(myLocation);
                	   getActivity().getFragmentManager().popBackStack();
                	   if (GalleryFragment.mDiskLruCache != null) {
                		   GalleryFragment.mDiskLruCache.clearCache();
                	   }
                   }
               })
               .setNegativeButton(R.string.title_decline, new DialogInterface.OnClickListener() {
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