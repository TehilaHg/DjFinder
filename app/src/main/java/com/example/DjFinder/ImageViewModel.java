package com.example.DjFinder;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.ViewModel;

public class ImageViewModel extends ViewModel {
    private Bitmap bitmap;
    private Uri url;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        url=null;
    }

    public Uri getUrl() {
        return url;
    }

    public void setUrl(Uri url) {
        this.url = url;
        bitmap=null;
    }
}
