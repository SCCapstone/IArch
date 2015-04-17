package com.github.IArch;

import java.io.File;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class FullscreenImageFragment extends Fragment {

	String fileLocation;
	View galleryView;
	File filename;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		galleryView = inflater.inflate(R.layout.fragment_fullscreen_image, container, false);
		
		fileLocation = ImageDetailsFragment.fileLocation;
		setPic(fileLocation);
		
		getActivity().getActionBar().hide();
				
		return galleryView;
	}

	private void setPic(String file) {
		//get dimensions of view
		WebView myImage = (WebView) galleryView.findViewById(R.id.imageViewFS);
		myImage.getSettings().setBuiltInZoomControls(true);
		myImage.setInitialScale(40);
		/*
		//this works for now... hard coded scale factor
		int targetW = 900;//myImage.getWidth();
		int targetH = 600;//myImage.getHeight();
				
		System.out.println("targetW: " + targetW + " targetH: " + targetH);
		
		Bitmap myBitmap = decodeSampledBitmapFromFile(file, targetW, targetH);
		System.out.println("FILE LOCATION: " + fileLocation);
		 */	
		myImage.loadUrl("file:///" + fileLocation);
	}	
	/*	
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
		
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
	    System.out.println("INSAMPLE SIZE: " + inSampleSize);
	    return inSampleSize;
	}
	*/
	
}
