package edu.shell.freegee.view;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class FreegeePager extends FragmentPagerAdapter {

	private ArrayList<FreegeeFragment> fragments;
	
	public FreegeePager(FragmentManager fm, ArrayList<FreegeeFragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// Return a PlaceholderFragment (defined as a static inner class
		// below).
			return this.fragments.get(position);
	}

	@Override
	public int getCount() {
		// Show 3 total pages.
		return fragments.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		return fragments.get(position).getArguments().getString("Title");
	}
}