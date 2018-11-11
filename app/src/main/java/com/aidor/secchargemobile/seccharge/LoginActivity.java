package com.aidor.secchargemobile.seccharge;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    public static boolean loginAttemptFailedInternet;

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    public static int slideshowIndex = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */


    // UI references.
    //private AutoCompleteTextView mEmailView;
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView secChargeTxtView;
    private TextView forgetPasswordTextView;
    private Button btnRegister;
    private Button btnSearch;
    ViewPager mViewPager;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String UserID = "UserID";

    SharedPreferences sharedpreferences;

    public static LinearLayout layoutLogin;
    public static ImageView imgBackground;
    public static ImageView imgLogo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,

                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        //startBackgroundSlideShow();

        loginAttemptFailedInternet = false;
        checkNetwork();
        setContentView(R.layout.activity_login);


        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

//        secChargeTxtView = (TextView)findViewById(R.id.secChargeTextView);
//        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/Alice.ttf");
//        secChargeTxtView.setTypeface(tf);
        forgetPasswordTextView = (TextView)findViewById(R.id.forgetPasswordTextView);
        forgetPasswordTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this,ForgetPasswordActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
        btnRegister = (Button)findViewById(R.id.register_button);
        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this,RegisterActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        /*button to jump on mapwithout login */
        btnSearch = (Button)findViewById(R.id.map_button);
        btnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent search = new Intent(LoginActivity.this,BeforeLoginMapActivity.class);
                startActivity(search);
            }
        });

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        //populateAutoComplete();
        mPasswordView = (EditText) findViewById(R.id.password);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {


                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    return true;
                }
                return false;


            }


        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checkNetwork();

                if (!loginAttemptFailedInternet) {
                    attemptLogin();
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void startBackgroundSlideShow() {

        layoutLogin = (LinearLayout) findViewById(R.id.layout_login);
        final Animation fadeIn = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade_in_slow);
        final Animation fadeOut = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade_out);
        final Handler handler = new Handler();

        final List<Integer> backgrounds = new ArrayList<>();
        backgrounds.add(R.drawable.carousel_home_portrail);
        backgrounds.add(R.drawable.login_background2);
        backgrounds.add(R.drawable.login_background3);

        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {

                        try {
                            LoginActivity.imgLogo = (ImageView) findViewById(R.id.imgLogo);
                                    fadeIn.reset();
                                    fadeOut.reset();
                                    if (slideshowIndex > 2) {
                                        slideshowIndex = 0;
                                    }

                                    LoginActivity.layoutLogin = (LinearLayout) findViewById(R.id.layout_login);
                                    LoginActivity.imgBackground = (ImageView) findViewById(R.id.background_img);
                                    LoginActivity.imgBackground.startAnimation(fadeOut);
                                    LoginActivity.imgBackground.setVisibility(View.GONE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            LoginActivity.imgBackground.setBackground(getDrawable(backgrounds.get(slideshowIndex)));
                                            if (slideshowIndex==0){
                                                LoginActivity.imgLogo.setVisibility(View.VISIBLE);
                                            } else LoginActivity.imgLogo.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    LoginActivity.imgBackground.setVisibility(View.VISIBLE);
                                    LoginActivity.imgBackground.startAnimation(fadeIn);

                                    slideshowIndex++;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        timer.schedule(doAsynchronousTask, 0, 6000); //execute in every 4 seconds
    }

    @Override
    public void onResume(){
        super.onResume();
        checkNetwork();
    }

    private void checkNetwork() {
        if (!isNetworkAvailable()){
            loginAttemptFailedInternet = true;
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, NoInternetActivity.class).putExtra("activityName", "LoginActivity"));

        } else loginAttemptFailedInternet = false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        Log.d("insidePasLogin","done");

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        Log.d("password Attempt:",password);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (mPasswordView.getText().length() == 0) {
            Log.d("inside Valid:",password);
            mPasswordView.setError("This field is Required");
            focusView = mPasswordView;
            cancel = true;
        }else

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }  else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            UserLoginTask mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        //return email.contains("@");
        return true;
    }

    private boolean isPasswordValid() {
        //TODO: Replace this with your own logic

        Log.d("inside Function Valid :","done");
        String passwordCheck = mPasswordView.getText().toString();
        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(passwordCheck);

        if(!passwordCheck.contains("(?=.*[0-9])")){
            mPasswordView.setError("At Least 1 Digit Required!");
            mPasswordView.requestFocus();
        }else  if(!passwordCheck.contains("(?=.*[a-z])")) {
            mPasswordView.setError("At Least 1 Lower Case Required!");
            mPasswordView.requestFocus();
        }else  if(!passwordCheck.contains("(?=.*[A-Z])")) {
            mPasswordView.setError("At Least 1 Upper Case Required!");
            mPasswordView.requestFocus();
        }else  if(!passwordCheck.contains("(?=.*[@#$%^&+=])")) {
            mPasswordView.setError("At Least 1 Special Required!");
            mPasswordView.requestFocus();
        }else  if(!passwordCheck.contains("(?=\\\\S+$)")) {
            mPasswordView.setError("Spaces Are Not Allowed!");
            mPasswordView.requestFocus();
        }else  if(!passwordCheck.contains(".{7,}")) {
            mPasswordView.setError("Password Must be At Least 7 Charecter Long!");
            mPasswordView.requestFocus();
        }
        return matcher.matches();
    }

    /**
     * Shows the progress UI and hides the login form.
     */

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
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
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

      //  mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String return_text;

            MyURL Url = new MyURL();
            String URL = Url.getUrl();
            try {
                System.out.println("Back Email -->> " + mEmail);
                System.out.println("Back Password -->> " + mPassword);

                HttpClient httpClient = new DefaultHttpClient();
//                HttpPost httpPost = new HttpPost("http://.2.12:8011/seccharge/test/login/mob");
//                List<NameValuePair> para = new ArrayList<>();
//                para.add(new BasicNameValuePair("email", sEmail));
//                para.add(new BasicNameValuePair("password", sPassword));

                String NewURL =  URL+"/login/mob?username=" + mEmail + "&password=" + mPassword;
                HttpGet httpGet = new HttpGet(NewURL);
                // Encoding POST data
//                httpGet.setEntity(new UrlEncodedFormEntity(para));
                // Making HTTP Request
                HttpResponse response = httpClient.execute(httpGet);

                String res = response.toString();
                System.out.println("Http Post Response : " + res);
                InputStream is = response.getEntity().getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                String line = "";
                StringBuilder stringBuffer = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
                return_text = stringBuffer.toString();
                System.out.println("DATA FROM SERVER --> " + return_text);
                return return_text;
            } catch (ClientProtocolException | UnsupportedEncodingException clientEx) {
                clientEx.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "false";
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            showProgress(false);

            if(s.equals("Invalid Credentials")){
                Log.d("Invalid credentials",s);
                Toast.makeText(LoginActivity.this, "Invalid Credentials!", Toast.LENGTH_SHORT).show();
            }else
            {
                Log.d("UserID", s);

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(UserID,s);
                editor.commit();

                String value = sharedpreferences.getString(UserID, "");
                Log.d("GetUSerID", value);

                Intent  i = new Intent(LoginActivity.this,HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }

        }


    }
}

