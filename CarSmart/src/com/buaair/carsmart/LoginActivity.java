package com.buaair.carsmart;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.buaair.carsmart.utils.HttpClientUtil;
import com.buaair.carsmart.utils.JsonUtil;
import com.buaair.carsmart.utils.LogUtil;
import com.buaair.carsmart.vo.LoginResponseVO;
import com.dd.processbutton.iml.ActionProcessButton;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {
	
	public static final String EXTRAS_ENDLESS_MODE = "EXTRAS_ENDLESS_MODE";
	
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// UI references.
	private AutoCompleteTextView mUserView;
	private EditText mPasswordView;
	
	private ActionProcessButton btnSignIn;
	
//	private View mProgressView;
	private View mLoginFormView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Set up the login form.
		mUserView = (AutoCompleteTextView) findViewById(R.id.user);
		populateAutoComplete();

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		btnSignIn = (ActionProcessButton) findViewById(R.id.btnSignIn);

		btnSignIn.setMode(ActionProcessButton.Mode.ENDLESS);
		
		btnSignIn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				progressGenerator.start(btnSignIn);
				
				attemptLogin();
			}
		});
//		attemptLogin();
//		Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
//		mEmailSignInButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				attemptLogin();
//			}
//		});

		mLoginFormView = findViewById(R.id.login_form);
//		mProgressView = findViewById(R.id.login_progress);
	}
	
//	@Override
//    public void onComplete() {
//        Intent intent = new Intent(LoginActivity.this,
//				CarMapActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		startActivity(intent);
//		finish();
//    }
	
	
	 
	private void populateAutoComplete() {
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		btnSignIn.setProgress(0);
		btnSignIn.setEnabled(true);
		mUserView.setEnabled(true);
		mPasswordView.setEnabled(true);
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUserView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String user = mUserView.getText().toString();
		String password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(user)) {
			mUserView.setError(getString(R.string.error_field_required));
			focusView = mUserView;
			cancel = true;
		} else if (!isEmailValid(user)) {
			mUserView.setError(getString(R.string.error_invalid_user));
			focusView = mUserView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
//			showProgress(true);
			btnSignIn.setEnabled(false);
			mUserView.setEnabled(false);
			mPasswordView.setEnabled(false);
			
			mAuthTask = new UserLoginTask(user, password);
			mAuthTask.execute((Void) null);
			btnSignIn.setProgress(50);
		}
	}

	private boolean isEmailValid(String email) {
		// return email.contains("@");
		return true;
	}

	private boolean isPasswordValid(String password) {
		return password.length() > 4;
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});

//			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//			mProgressView.animate().setDuration(shortAnimTime)
//					.alpha(show ? 1 : 0)
//					.setListener(new AnimatorListenerAdapter() {
//						@Override
//						public void onAnimationEnd(Animator animation) {
//							mProgressView.setVisibility(show ? View.VISIBLE
//									: View.GONE);
//						}
//					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
//			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(this,
				// Retrieve data rows for the device user's 'profile' contact.
				Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
						ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
				ProfileQuery.PROJECTION,

				// Select only email addresses.
				ContactsContract.Contacts.Data.MIMETYPE + " = ?",
				new String[] { ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE },

				// Show primary email addresses first. Note that there won't be
				// a primary email address if the user hasn't specified one.
				ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		List<String> emails = new ArrayList<String>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			emails.add(cursor.getString(ProfileQuery.ADDRESS));
			cursor.moveToNext();
		}

		addEmailsToAutoComplete(emails);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {

	}

	private interface ProfileQuery {
		String[] PROJECTION = { ContactsContract.CommonDataKinds.Email.ADDRESS,
				ContactsContract.CommonDataKinds.Email.IS_PRIMARY, };

		int ADDRESS = 0;
		// int IS_PRIMARY = 1;
	}

	private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
		// Create adapter to tell the AutoCompleteTextView what to show in its
		// dropdown list.
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				LoginActivity.this,
				android.R.layout.simple_dropdown_item_1line,
				emailAddressCollection);

		mUserView.setAdapter(adapter);
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, LoginResponseVO> {

		private final String mUser;
		private final String mPassword;

		UserLoginTask(String user, String password) {
			mUser = user;
			mPassword = password;
		}

		@Override
		protected LoginResponseVO doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			
			String result = HttpClientUtil.login(mUser, mPassword);
			LoginResponseVO responseVO = JsonUtil.parseObject(result,	LoginResponseVO.class);
			return responseVO;
		}

		@Override
		protected void onPostExecute(final LoginResponseVO responseVO) {
			mAuthTask = null;
			
			if (responseVO != null && responseVO.ret == 0) {
				if(btnSignIn != null){
					btnSignIn.setProgress(100);
				}
				HttpClientUtil.loginInfo = responseVO.rows;
				HttpClientUtil.loginInfo.account = mUser;
				Intent intent = new Intent(LoginActivity.this, CarMapActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
//				finish();
			} else {
				if(responseVO != null){
					LogUtil.e(responseVO.msg);
					mPasswordView.setError(responseVO.msg);
				}
				btnSignIn.setEnabled(true);
				mUserView.setEnabled(true);
				mPasswordView.setEnabled(true);
				btnSignIn.setProgress(0);
				mPasswordView.requestFocus();
			}
			
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
//			showProgress(false);
		}
	}
}
