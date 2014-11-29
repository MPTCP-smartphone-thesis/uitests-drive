package drive;

import java.util.List;

import utils.Utils;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

public class LaunchSettings extends UiAutomatorTestCase {

	private static final String ID_DRIV_UPLOAD = "com.google.android.apps.docs:id/create_bar_upload";
	private static final String ID_DRIV_LIST = "com.android.documentsui:id/list";
	private static final String ID_DRIV_NAME = "android:id/title";
	private static final String ID_DRIV_TITLE = "android:id/action_bar_title";
	private static final String LABEL_OPEN_FROM = "Open from";
	private static final String LABEL_MY_DRIVE = "My Drive";
	private static final String SEND_FILE = "random_seed_concat.bin";

	private static final int NB_FILES = 1;

	private void updateFile() {
		// Be more adaptative...
		if (!Utils.hasText(ID_DRIV_TITLE, LABEL_MY_DRIVE)) {
			getUiDevice().pressBack();
		}

		assertTrue("Upload button is not here",
				Utils.clickAndWaitForNewWindow(ID_DRIV_UPLOAD));

		sleep(1000);

		// A little tricky to select the button...
		// Not always to have that, verify if needed first
		UiObject action_title = Utils.getObjectWithId(ID_DRIV_TITLE);
		
		if (Utils.hasText(action_title, LABEL_OPEN_FROM)) {

			UiObject internal_storage = new UiObject(
					new UiSelector()
							.className("android.widget.ListView")
							.instance(0)
							.childSelector(
									new UiSelector().className(
											"android.widget.LinearLayout")
											.instance(10)));
			assertTrue("Button Internal storage is not here",
					Utils.click(internal_storage));
		}

		sleep(1000);

		// Scroll the view until finding our file
		boolean found = false;
		UiScrollable list = Utils.getScrollableWithId(ID_DRIV_LIST);

		while (!found) {
			List<UiObject> available = Utils.getElems(ID_DRIV_LIST,
					ID_DRIV_NAME);
			assertTrue("Unable to retrieve the list", !available.isEmpty());

			for (UiObject dest : available) {
				if (Utils.hasText(dest, SEND_FILE)) {
					assertTrue("Unable to select element",
							Utils.hasTextAndClick(dest, SEND_FILE));
					found = true;
					break;
				}
			}

			if (!found) {
				assertTrue("Didn't find the requested file...",
						Utils.scrollForward(list));
			}
		}

	}

	public void testDemo() throws UiObjectNotFoundException {
		assertTrue("OOOOOpps",
				Utils.openApp(this, "Drive", "com.google.android.apps.docs"));

		for (int i = 0; i < NB_FILES; i++) {
			Utils.createFile(SEND_FILE);
			updateFile();
			sleep(60000);
		}

	}

}
