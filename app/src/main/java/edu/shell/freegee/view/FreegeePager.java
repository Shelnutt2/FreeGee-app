package edu.shell.freegee.view;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;
import android.util.SparseArray;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class FreegeePager extends FragmentPagerAdapter {

	private SparseArray<FreegeeFragment> fragments;
	
	public FreegeePager(FragmentManager fm, SparseArray<FreegeeFragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}
	
	public void addItem(FreegeeFragment newFrag){
		fragments.put(fragments.size(), newFrag);
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// Return a PlaceholderFragment (defined as a static inner class
		// below).
		//if(this.fragments.size() < position)
		return this.fragments.get(position);
		//return FreegeeFragment.newInstance(position,"");
	}

	@Override
	public int getCount() {
		// Show 3 total pages.
		return fragments.size();
	}
	
	public SparseArray<FreegeeFragment> getSparseArray(){
		return fragments;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		return fragments.get(position).getArguments().getString("Title");
	}
}