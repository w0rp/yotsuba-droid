package com.w0rp.yotsubadroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import com.w0rp.androidutils.SLog;

import android.os.AsyncTask;

public abstract class DownloadTask extends AsyncTask<HttpUriRequest, Object, Long> {
	public static final int CHUNK_SIZE      = 4096;
	public static final int UNKNOWN_FAILURE = 600;
	
	@SuppressWarnings("serial")
	private static class CancelledException extends RuntimeException { }
	
	@Override
	protected Long doInBackground(HttpUriRequest... requestList) {
		long count = 0;
		
		for (HttpUriRequest request : requestList) {
			if (isCancelled()) {
				break;
			}
			
			try {
				downloadRequest(request);
			} catch (IOException e) {
				onDownloadInterrupt(request.getURI());
				continue;
			} catch (CancelledException e) {
				onDownloadInterrupt(request.getURI());
				break;
			}
			
			// TODO: Use size of downloads to enhance progress updates?
			publishProgress(++count / requestList.length);
			
			++count;
		}
		
		return count;
	}
	
	/*
	 * @param request The HTTP request to download.
	 * 
	 * @return false if the download is interrupted.
	 */
	private void downloadRequest(HttpUriRequest request) throws IOException, CancelledException {
    	int responseCode    = UNKNOWN_FAILURE;
    	
    	onDownloadStart(request.getURI());
    	
    	try {
    		HttpResponse response = new DefaultHttpClient().execute(request);
	        HttpEntity entity = response.getEntity();
	        InputStream is = null;
	        
	        if (isCancelled()) {
				throw new CancelledException();
			}
	        
	        try {
	        	is = entity.getContent();
	        	
	        	downloadStream(request, is);
	        } finally {
				is.close();
			}
	        
    		responseCode = response.getStatusLine().getStatusCode();
		} catch (UnknownHostException e) {
			SLog.e("unknown host: " + e.getMessage());
		} catch (Exception e) {
		}
    	
    	onDownloadComplete(request.getURI(), responseCode);
	}
	
	private void downloadStream(HttpUriRequest request, InputStream is) throws IOException, CancelledException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        
        char[] buffer = new char[CHUNK_SIZE];
        int readLength;
        
        while (true) {
        	if (isCancelled()) {
				throw new CancelledException();
			}
        	
        	readLength = reader.read(buffer);
        	
        	if (readLength < 0) {
				break;
			}
        	
        	onDownloadPiece(request.getURI(), Arrays.copyOfRange(buffer, 0, readLength));
        }
    }
	
	/*
	 * This method is when a download starts.
	 * 
	 * @param uri The URI for the download which just began.
	 */
	abstract protected void onDownloadStart(URI uri);
	
	/*
	 * This method is called to handle a piece of the download in progress.
	 * 
	 * No two downloads will occur simultaneously for a single task instance.
	 * 
	 * @param uri The URI for the download in progress.
	 * @param piece A piece of data.
	 */
	abstract protected void onDownloadPiece(URI uri, char[] piece);
	
	/*
	 * This method is called when a download is completed.
	 * 
	 * @param uri The URI for the completed download.
	 * @param responseCode The HTTP response code for the download.
	 */
	abstract protected void onDownloadComplete(URI uri, int responseCode);
	
	/*
	 * This method is called when a download is interrupted, by some error
	 * or canceling the task.
	 * 
	 * @param uri The URI for the download which was interrupted.
	 */
	abstract protected void onDownloadInterrupt(URI uri);
}