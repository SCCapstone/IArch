package com.github.IArch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddDialogHandler extends DialogFragment {
	
    public AddDialogHandler() {
	}

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View inflator = inflater.inflate(R.layout.dialog_add_project, null);
        
        final EditText et1 = (EditText) inflator.findViewById(R.id.new_project_name);
        builder.setView(inflator);
        builder.setMessage(R.string.title_add)
               .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   String s1 = et1.getText().toString();
                	   TakePictureFragment.list.add(s1);
                	   Toast.makeText(getActivity(), s1 + " added to project list!", Toast.LENGTH_SHORT).show();

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
	
}
