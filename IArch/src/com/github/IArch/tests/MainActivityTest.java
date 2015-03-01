package com.github.IArch.tests;

import com.github.IArch.MainActivity;
import com.github.IArch.R;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private MainActivity mFirstTestActivity;
    private TextView mFirstTestText;
    
    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFirstTestActivity = getActivity();
        mFirstTestText = (TextView) mFirstTestActivity.findViewById(R.id.app_title);
    }
    
    public void testPreconditions() {
        assertNotNull("mFirstTestActivity is null", mFirstTestActivity);
        assertNotNull("mFirstTestText is null", mFirstTestText);
    }
    
    public void testMyFirstTestTextView_labelText() {
        final String expected = mFirstTestActivity.getString(R.string.app_name);
        final String actual = mFirstTestText.getText().toString();
        assertEquals(expected, actual);
    }
}
