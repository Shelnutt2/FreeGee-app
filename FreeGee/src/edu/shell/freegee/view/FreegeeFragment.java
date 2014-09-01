package edu.shell.freegee.view;

import edu.shell.freegee.R;
import edu.shell.freegee.R.id;
import edu.shell.freegee.R.layout;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class FreegeeFragment extends Fragment {
	
	private View v;
	
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";
	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static FreegeeFragment newInstance(int sectionNumber, String title) {
		FreegeeFragment fragment = new FreegeeFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		args.putString("Title", title);
		fragment.setArguments(args);
		return fragment;
	}

	public FreegeeFragment() {
		super();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		v = rootView;
		return rootView;
	}

	public boolean setContent(String content) {
		if(v != null){
			TextView view = (TextView) v.findViewById(R.id.json_results);
			view.setText(content);
			return true;
		}
		return false;
		
	}
}