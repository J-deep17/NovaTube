package com.liskovsoft.appupdatechecker2.core;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.URLUtil;
import com.liskovsoft.appupdatechecker2.other.downloadmanager.DownloadManager;
import com.liskovsoft.appupdatechecker2.other.downloadmanager.DownloadManager.MyRequest;
import com.liskovsoft.sharedutils.helpers.FileHelpers;

import java.io.File;

/**
 * Usage:
 * <pre>
 *   downloader = new AppDownloader(ctx, listener);
 *   downloader.download(new String[]{"http://serverurl/appfile.apk"});
 * </pre>
 */
public class AppDownloader {
    private static final String TAG = AppDownloader.class.getSimpleName();
    private static final String CURRENT_APK = "update.apk";
    private final Context mContext;
    private boolean mInProgress;
    private final AppDownloaderListener mListener;
    private final int mMinApkSizeBytes;
    private AppDownloadTask mDownloadTask;

    public AppDownloader(Context context, AppDownloaderListener listener, int minApkSizeBytes) {
        mContext = context;
        mListener = listener;
        mMinApkSizeBytes = minApkSizeBytes;
    }

    /**
     * Uses first available url in the list.
     */
    public void download(Uri[] downloadUris) {
        if (!mInProgress) {
            if (mDownloadTask == null) {
                mDownloadTask = new AppDownloadTask();
                mDownloadTask.execute(downloadUris);
            } else {
                Log.e(TAG, "DownloadTask not null. Strange...");
            }
        } else {
            Log.e(TAG, "Another downloading in progress. Canceling...");
        }
    }

    private class AppDownloadTask extends AsyncTask<Uri[],Void,String> {
        @Override
        protected String doInBackground(Uri[]... args) {
            mInProgress = true;

            Uri[] uris = args[0];

            String path = null;
            for (Uri uri : uris) {
                if (URLUtil.isValidUrl(uri.toString())) {
                    path = downloadPackage(uri.toString());
                    if (path != null)
                        break;
                }
            }

            return path;
        }

        @Override
        protected void onPostExecute(String path) {
            if (path != null) {
                mListener.onApkDownloaded(path);
            } else {
                String msg = "Error while download. Install path is null";
                Log.e(TAG, msg);
                mListener.onDownloadError(new IllegalStateException(msg));
            }

            mInProgress = false;
            mDownloadTask = null;
        }

        private String downloadPackage(String uri) {
            File cacheDir = FileHelpers.getCacheDir(mContext);
            if (cacheDir == null && mContext != null) {
                cacheDir = mContext.getExternalCacheDir();
            }
            if (cacheDir == null && mContext != null) {
                cacheDir = mContext.getCacheDir();
            }
            if (cacheDir == null) {
                Log.e(TAG, "Cannot get cache directory for update download");
                return null;
            }
            File outputFile = new File(cacheDir, CURRENT_APK);
            String path = null;
            try {
                Log.d(TAG, "Attempting update download from: " + uri);
                DownloadManager manager = new DownloadManager(mContext);
                MyRequest request = new MyRequest(Uri.parse(uri));
                request.setDestinationUri(Uri.fromFile(outputFile));
                try {
                    long id = manager.enqueue(request);
                    int size = manager.getSizeForDownloadedFile(id);
                    Uri destination = manager.getUriForDownloadedFile(id);

                    if (destination != null && destination.getPath() != null) {
                        File downloadedFile = new File(destination.getPath());
                        long diskSize = downloadedFile.exists() ? downloadedFile.length() : 0;
                        Log.d(TAG, "Download finished. Disk size: " + diskSize + ", reported size: " + size);

                        // Ensure file exists and meets minimum APK threshold (~1MB)
                        if (diskSize > mMinApkSizeBytes || size > mMinApkSizeBytes) {
                            path = destination.getPath();
                            Log.d(TAG, "Valid update package downloaded to: " + path);
                        } else { // do cleanup
                            Log.w(TAG, "Downloaded file too small (" + diskSize + " bytes), deleting...");
                            FileHelpers.delete(destination.getPath());
                        }
                    } else {
                        Log.e(TAG, "Destination is null for URL: " + uri);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Download failure for " + uri + ": " + ex.getMessage(), ex);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Setup failure for " + uri + ": " + ex.getMessage(), ex);
            }
            return path;
        }
    }

    public boolean isInProgress() {
        return mInProgress;
    }
}
