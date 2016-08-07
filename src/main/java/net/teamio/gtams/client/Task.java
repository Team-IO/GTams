package net.teamio.gtams.client;

public abstract class Task implements Runnable {
	public boolean isCancelled = false;
	/**
	 * True if the dask is done in any way.
	 * This includes error & cancelled status.
	 */
	public boolean isDone = false;
	public boolean isError = false;
	protected boolean resultsAvailable = false;

	@Override
	public void run() {
		try {
			process();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isError = true;
		}
		isDone = true;
	}

	/**
	 * Called in a background thread
	 * @throws GTamsException
	 */
	public abstract void process() throws GTamsException;

	public void processSyncTasks() {
		if(resultsAvailable) {
			try {
				doInSync();
			} catch (Exception e) {
				isError = true;
				System.err.println("Error processing sync actions for a task");
				e.printStackTrace();
			} finally {
				resultsAvailable = false;
			}
		}
	}

	/**
	 * Called from the owner of this task to do work in sync,  e.g. in TileEntity updates or in GUI.
	 */
	protected abstract void doInSync() throws GTamsException;

	protected void waitForSync() {
		resultsAvailable = true;
		while(resultsAvailable) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
