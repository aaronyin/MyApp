package com.buaair.carsmart;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

import com.buaair.carsmart.fragment.CarInfoFragment;
import com.buaair.carsmart.fragment.CommandFragment;
import com.buaair.carsmart.fragment.HistoryFragment;
import com.buaair.carsmart.fragment.TrackingFragment;

public class MainActivity extends FragmentActivity implements
		View.OnClickListener {

	private static FragmentManager fMgr;

	public static Fragment[] fragments;

	public static final int FRAGMENT_FLAG_CARINFO = 0;
	public static final int FRAGMENT_FLAG_TRACKING = 1;
	public static final int FRAGMENT_FLAG_HISTORY = 2;
	public static final int FRAGMENT_FLAG_COMMAND = 3;

	private RadioButton btnCarinfo,btnTracking,btnHistory,btnCommand;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 获取FragmentManager实例
		fMgr = getSupportFragmentManager();

		getActionBar().setDisplayHomeAsUpEnabled(true);

		int fragment_flag = getIntent().getIntExtra("fragment_flag", 0);

		btnCarinfo = (RadioButton)findViewById(R.id.rbCarinfo);
		btnCarinfo.setOnClickListener(this);
		btnTracking = (RadioButton)findViewById(R.id.rbTracking);
		btnTracking.setOnClickListener(this);
		btnHistory = (RadioButton)findViewById(R.id.rbHistory);
		btnHistory.setOnClickListener(this);
		btnCommand = (RadioButton)findViewById(R.id.rbCommand);
		btnCommand.setOnClickListener(this);

		initFragment();
		startFragment(fragment_flag);
	}

	/** 初始化fragment */
	public void initFragment() {
		fragments = new Fragment[4];
		fragments[0] = new CarInfoFragment();
		fragments[1] = new TrackingFragment();
		fragments[2] = new HistoryFragment();
		fragments[3] = new CommandFragment();

		Bundle bundle = getIntent().getExtras();
		for (int i = 0; i < fragments.length; i++) {
			fragments[i].setArguments(bundle);
		}
		FragmentTransaction ft = fMgr.beginTransaction();
		ft.add(R.id.fragmentRoot, fragments[0]);
		ft.add(R.id.fragmentRoot, fragments[1]);
		ft.add(R.id.fragmentRoot, fragments[2]);
		ft.add(R.id.fragmentRoot, fragments[3]);
		ft.commit();
	}

	private void startFragment(int fragment_flag) {
		switch (fragment_flag) {
		case FRAGMENT_FLAG_CARINFO:
			setTitle(R.string.title_carinfo);
			btnCarinfo.setChecked(true);
			break;
		case FRAGMENT_FLAG_TRACKING:
			setTitle(R.string.title_tracking);
			btnTracking.setChecked(true);
			break;
		case FRAGMENT_FLAG_HISTORY:
			setTitle(R.string.title_history);
			btnHistory.setChecked(true);
			break;
		case FRAGMENT_FLAG_COMMAND:
			setTitle(R.string.title_command);
			btnCommand.setChecked(true);
			break;
		default:
			break;
		}
		
		FragmentTransaction ft = fMgr.beginTransaction();
		ft.hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]);
		ft.show(fragments[fragment_flag]);
//		ft.replace(R.id.fragmentRoot, fragments[fragment_flag]);
		ft.commit();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.rbCarinfo:
			startFragment(FRAGMENT_FLAG_CARINFO);
			break;
		case R.id.rbTracking:
			startFragment(FRAGMENT_FLAG_TRACKING);
			break;
		case R.id.rbHistory:
			startFragment(FRAGMENT_FLAG_HISTORY);
			break;
		case R.id.rbCommand:
			startFragment(FRAGMENT_FLAG_COMMAND);
			break;
		default:
			break;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			Intent upIntent = NavUtils.getParentActivityIntent(this);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				TaskStackBuilder.create(this)
						.addNextIntentWithParentStack(upIntent)
						.startActivities();
			} else {
				upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	// 点击返回按钮
	@Override
	public void onBackPressed() {
		// if (fMgr.findFragmentByTag(FRAGMENT_FLAG_CARINFO) != null
		// && fMgr.findFragmentByTag(FRAGMENT_FLAG_CARINFO).isVisible()) {
		MainActivity.this.finish();
		// } else {
		// super.onBackPressed();
		// }
	}
}
