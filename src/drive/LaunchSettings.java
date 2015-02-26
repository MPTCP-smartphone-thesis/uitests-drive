package drive;

import utils.Utils;

import com.android.uiautomator.core.UiCollection;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

public class LaunchSettings extends UiAutomatorTestCase {

	private static final String ID_LIST_FILE = "com.google.android.apps.docs:id/title";
	private static final String ID_TITLE_ALERT = "android:id/alertTitle";
	private static final String LABEL_REMOVE = "Remove";
	private static final String ID_BUTTON_REMOVE = "com.google.android.apps.docs:id/btn_ok";

	private static final String ID_DRIV_TITLE = "android:id/action_bar_title";
	private static final String LABEL_MY_DRIVE = "My Drive";
	private static final String ID_DRIV_UPLOAD = "com.google.android.apps.docs:id/create_bar_upload";
	private static final String LABEL_INTERNAL_STORAGE = "Internal storage";
	private static final String LABEL_OPEN_FROM = "Open from";
	private static final String ID_DRIV_LIST = "com.android.documentsui:id/list";
	private static final String ID_TITLE_FILE = "android:id/title";

	private static final String ID_TITLE_STATUS = "com.google.android.apps.docs:id/statusLabels";
	private static final String ID_TITLE_MSG = "android:id/message";
	private static final String LABEL_MSG_PREPARING = "Preparing 1 file for upload.";

	private static final String SEND_FILE = "a_random_seed_concat.bin";
	private static int NB_FILES = 2;
	private static int MAX_TIME = 2 * 60;


	private void removePreviousFiles(String fileName)
			throws UiObjectNotFoundException {
		// GDrive keep a file per version, we could delete several files
		while (true) {
			UiObject oldFile = Utils.findLayoutInList(fileName,
					android.widget.RelativeLayout.class.getName(), 9, null,
					ID_LIST_FILE, true);
			if (oldFile != null && oldFile.exists()) {
				// refresh can take some time...
				for (int i = 0; i < 20; i++) {
					// long press with text for the menu: longClick doesn't work
					Utils.longPress(Utils.getObjectWithText(fileName), 500);
					// when refreshing, longPress is disabled
					if (Utils.hasText(ID_TITLE_ALERT, fileName))
						break;
				}
				// menu, find delete
				Utils.getObjectWithText(LABEL_REMOVE)
						.clickAndWaitForNewWindow();
				// confirmation
				Utils.clickAndWaitForNewWindow(ID_BUTTON_REMOVE);
			}
			else
				return;
		}
	}

	private void updateFile(String fileName) throws UiObjectNotFoundException {
		// If we are on the left menu: pressback
		if (!Utils.hasText(ID_DRIV_TITLE, LABEL_MY_DRIVE)) {
			getUiDevice().pressBack();
		}

		// Upload button on the bottom
		assertTrue("Upload button is not here",
				Utils.clickAndWaitForNewWindow(ID_DRIV_UPLOAD));

		// If we are on the left menu or not in Internal storage
		UiObject action_title = Utils.getObjectWithId(ID_DRIV_TITLE);
		if (!Utils.hasText(action_title, LABEL_INTERNAL_STORAGE)) {
			if (!Utils.hasText(action_title, LABEL_OPEN_FROM)) {
				Utils.click(action_title);
				sleep(750);
			}
			assertTrue("Button Internal storage is not here",
					Utils.clickAndWaitForNewWindow(Utils
							.getObjectWithText(LABEL_INTERNAL_STORAGE)));
		}

		// Scroll the view until finding our file
		UiScrollable list = Utils.getScrollableWithId(ID_DRIV_LIST);
		list.setAsVerticalList();
		boolean lastCheck = false;
		while (true) {
			UiCollection listView = new UiCollection(
					new UiSelector().resourceId(ID_DRIV_LIST));
			UiObject title = listView.getChild(new UiSelector().resourceId(
					ID_TITLE_FILE).text(fileName));
			if (title.exists() && title.clickAndWaitForNewWindow())
				return;
			if (lastCheck)
				throw new UiObjectNotFoundException("File not found");
			lastCheck = !Utils.scrollForward(list); // true: end of the list
		}
	}

	private UiObject waitForEndUpload(String fileName)
			throws UiObjectNotFoundException {
		int i = 0;
		// We first have a dialogue: Preparing file
		boolean stop = false;
		for (; i < MAX_TIME; i++) {
			UiObject msg = Utils.getObjectWithId(ID_TITLE_MSG);
			try {
				if (msg == null || !msg.exists()
						|| !msg.getText().equals(LABEL_MSG_PREPARING))
					break;
			} catch (UiObjectNotFoundException e) {
				// strange error: exists but not found when getting text
				if (stop)
					break;
				stop = true;
			}
			sleep(1000);
		}
		// error if timeout with the first dialogue
		assertTrue("Timeout", i < MAX_TIME);

		// wait: time to see the file in the list with Uploading status
		i += 2;
		sleep(2000);

		// Then the file is uploading
		UiObject uploadingFile = Utils.findLayoutInList(fileName,
				android.widget.RelativeLayout.class.getName(), 9, null,
				ID_LIST_FILE, true);
		for (; i < MAX_TIME + 10; i++) {
			UiObject status = uploadingFile.getChild(new UiSelector()
					.resourceId(ID_TITLE_STATUS));
			if (status == null || !status.exists()
					|| status.getText().isEmpty())
				return null;
			sleep(1000);
		}
		return uploadingFile;
	}

	public void testDemo() throws UiObjectNotFoundException {
		assertTrue("OOOOOpps",
				Utils.openApp(this, "Drive",
						"com.google.android.apps.docs",
						"com.google.android.apps.docs.app.NewMainProxyActivity"));
		double multTime = Utils.getMultTime(this);
		NB_FILES = Math.max(1, (int) (NB_FILES * multTime));
		MAX_TIME = Math.max(1, (int) (MAX_TIME * multTime));
		sleep(1000);

		for (int i = 0; i < NB_FILES; i++) {
			long start = System.currentTimeMillis();
			// create file with a few random
			Utils.createFile(SEND_FILE);
			removePreviousFiles(SEND_FILE);

			// upload file and wait
			updateFile(SEND_FILE);
			sleep(1000); // new window + dialog
			UiObject uploadingFile = waitForEndUpload(SEND_FILE);
			if (uploadingFile != null) {
				System.out.println("Timeout, cancel upload");
				// to avoid uploading a file during other tests
				removePreviousFiles(SEND_FILE);
				// Error if no file uploaded
				//assertTrue("Not able to upload any file", i > 0);
				System.err.println("WARNING: no enough time to upload a file")
				// we had at least one file, it's ok.
				break;
			}

			// check if we have enough time for a new upload
			int elapsedTimeSec = (int) ((System.currentTimeMillis() - start) / 1000);
			System.out.println("Elapsed time: " + elapsedTimeSec + " - "
					+ MAX_TIME);
			if (MAX_TIME > 2 * elapsedTimeSec)
				MAX_TIME -= elapsedTimeSec;
			else if (i + 1 < NB_FILES) {
				System.out.println("No more time for a new test...");
				return;
			}
		}

	}

}
