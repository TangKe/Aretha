package com.arethademos.widget;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.aretha.widget.Workspace;
import com.arethademos.R;

public class WorkspaceDemo extends Activity {
	private Workspace mWorkspace;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.workspace);

		mWorkspace = (Workspace) findViewById(R.id.workspace);
	}

	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.next_child) {
			mWorkspace.animationToNextPage();
		} else if (id == R.id.prev_child) {
			mWorkspace.animationToPrevPage();
		} else if (id == R.id.aretha_child) {
			mWorkspace.scrollToPage(1, true);
		}
	}
}
