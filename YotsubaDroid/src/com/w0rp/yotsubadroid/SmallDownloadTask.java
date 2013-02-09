package com.w0rp.yotsubadroid;

import java.net.URI;

/*
 * This abstract AsyncTask class collected entire downloads
 * and sends the results as strings to the abstract method handleDownload().
 */
public abstract class SmallDownloadTask extends DownloadTask {
	private StringBuilder sb;
	
	@Override
	protected void onDownloadStart(URI uri) {
		sb = new StringBuilder();
	}
	
	@Override
	protected void onDownloadPiece(URI uri, char[] piece) {
		sb.append(piece);
	}
	
	@Override
	protected void onDownloadInterrupt(URI uri) {
		sb = null;
	}
	
	@Override
	protected void onDownloadComplete(URI uri, int responseCode) {
		handleDownload(uri, responseCode, sb.toString());
	}
	
	abstract protected void handleDownload(URI uri, int responseCode, String result);
}