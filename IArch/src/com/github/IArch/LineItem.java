package com.github.IArch;

import android.graphics.Bitmap;

public class LineItem {
	private Bitmap image;
	private String title;
	private String lastModified;

	public LineItem(Bitmap image, String title, String lastModified) {
		super();
		this.image = image;
		this.title = title;
		this.lastModified = lastModified;
	}

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getLastModified() {
		return lastModified;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	
}