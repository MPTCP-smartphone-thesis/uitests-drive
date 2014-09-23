package drive;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;


public class LaunchSettings extends UiAutomatorTestCase {
	
	private static String ID_DRIV_UPLOAD = "com.google.android.apps.docs:id/create_bar_upload";
	
	private void createFile() {
		String[] commands = {"cd /storage/sdcard0", "dd if=/dev/urandom of=/storage/sdcard0/random_seed bs=1 count=100000", "cat /storage/sdcard0/random_seed /storage/sdcard0/random_seed_orig /storage/sdcard0/random_seed > /storage/sdcard0/random_seed_concat"};
		Utils.runAsRoot(commands);
	}
	
	private void updateFile() {
		Utils.clickAndWaitForNewWindow(ID_DRIV_UPLOAD);
		
		sleep(1000);
		
		UiObject internal_storage = new UiObject(new UiSelector()
			.className("android.widget.LinearLayout").instance(5));
		Utils.click(internal_storage);
	}
	
	
	public void testDemo() throws UiObjectNotFoundException{
		assertTrue("OOOOOpps",Utils.openApp(this, "Drive", "com.google.android.apps.docs"));
		
		//while(true) {
			createFile();
			updateFile();
		//}
		
	}

}