package com.arethademo.widget;

import com.aretha.widget.SectorView;
import com.aretha.widget.SectorView.OnSectorClickListener;
import com.arethademo.R;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class SectorViewDemo extends Activity implements OnClickListener,
		OnSectorClickListener {
	private SectorView mSectorView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sector_view);

		mSectorView = (SectorView) findViewById(R.id.sectorView);
		mSectorView.setOnSectorClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.position:
			showDialog(R.id.change_sectorview_position_dialog);
			break;
		case R.id.toggle:
			mSectorView.toggle(!mSectorView.isExpanded());
			break;
		case R.id.radius_plus:
			mSectorView.setRadius(mSectorView.getRadius() + 50,
					TypedValue.COMPLEX_UNIT_PX);
			break;
		case R.id.radius_minus:
			mSectorView.setRadius(mSectorView.getRadius() - 50,
					TypedValue.COMPLEX_UNIT_PX);
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Builder builder = new Builder(this);
		switch (id) {
		case R.id.change_sectorview_position_dialog:
			builder.setTitle(R.string.gravity);
			builder.setSingleChoiceItems(R.array.position_item, 1, this);
			break;
		default:
			break;
		}
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		int gravity = -1;
		switch (which) {
		case 0:
			gravity = Gravity.CENTER;
			break;
		case 1:
			gravity = Gravity.LEFT | Gravity.BOTTOM;
			break;
		case 2:
			gravity = Gravity.RIGHT | Gravity.BOTTOM;
			break;
		case 3:
			gravity = Gravity.RIGHT | Gravity.TOP;
			break;
		case 4:
			gravity = Gravity.LEFT | Gravity.TOP;
			break;
		case 5:
			gravity = Gravity.TOP;
			break;
		case 6:
			gravity = Gravity.LEFT;
			break;
		case 7:
			gravity = Gravity.BOTTOM;
			break;
		case 8:
			gravity = Gravity.RIGHT;
			break;
		}
		mSectorView.setGravity(gravity);
		dialog.dismiss();
	}

	@Override
	public boolean onSectorClick(View sector) {
		Toast.makeText(
				getApplicationContext(),
				getString(R.string.sector_click,
						mSectorView.indexOfChild(sector)), Toast.LENGTH_SHORT)
				.show();
		return false;
	}
}
