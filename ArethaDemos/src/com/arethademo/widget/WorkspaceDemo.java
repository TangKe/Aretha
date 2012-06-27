package com.arethademo.widget;

import com.aretha.widget.Workspace;
import com.arethademo.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class WorkspaceDemo extends Activity {
	private Workspace mWorkspace;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.workspace);

		mWorkspace = (Workspace) findViewById(R.id.workspace);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.next_child:
			mWorkspace.animationToNextChild();
			break;
		case R.id.prev_child:
			mWorkspace.animationToPrevChild();
			break;
		case R.id.artha_child:
			mWorkspace.scrollToChild(1, true);
			break;
		}
	}
}
