package de.htwds.mada.foodsharing;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;


public class OfferEditActivity extends Activity {
    private static final String LOG=OfferEditActivity.class.getName();

    private TextView activityTitle;


    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView photoImageView;
    private Bitmap bitmap;
    private File photoFile=null;

    private final Handler handler = new Handler();
    private final FragmentManager fragMan = getFragmentManager();
    /* fields */
    //title field
    private EditText titleInputField;
    //category field
    private EditText categoryInputField;
    //mhd field
    private static Calendar bestBeforeDate;
    private EditText bestBeforeDateInputField;
    //description field
    private EditText longDescriptionInputField;
    //finish button
    private Button publishOfferButton;



    private Offer currentOffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_edit);

        registerViews();

        currentOffer = new Offer(OfferEditActivity.this);
        currentOffer.setID(getIntent().getIntExtra(Constants.keyOfferID, -1));
        activityTitle.setText(Constants.CREATE_OFFER);

        if (currentOffer.getID() >= 0) {
            activityTitle.setText(Constants.EDIT_OFFER);
            currentOffer.setEdited(true);
            new RetrieveOfferInfoTask().execute();
        }


    }

    private void registerViews()
    {
        activityTitle = (TextView) findViewById(R.id.offerEditActivityTitle);

        photoImageView = (ImageView) findViewById(R.id.offerPicture);

        titleInputField = (EditText) findViewById(R.id.title_tv);


        categoryInputField = (EditText) findViewById(R.id.offer_category_edit);
        categoryInputField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    onCategorieEditClick();
                }
            }
        });
        categoryInputField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View w) {
                onCategorieEditClick();
            }
        });

        bestBeforeDate = Calendar.getInstance();
        bestBeforeDateInputField = (EditText) findViewById(R.id.best_before_date_edit);
        bestBeforeDateInputField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDateEditClick();
            }
        });
        bestBeforeDateInputField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    onDateEditClick();
                }
            }
        });

        longDescriptionInputField = (EditText) findViewById(R.id.detailed_description_tv);

        publishOfferButton = (Button) findViewById(R.id.publish_offer_btn);

    }

    private void onCategorieEditClick() {
        DialogFragment dialog = new CategoryFragment();
        dialog.show(getFragmentManager(), Constants.CATEGORY_DIALOG_TAG);
    }


    private void onDateEditClick() {
        DialogFragment dateFragment = new DatePickerFragment() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                bestBeforeDate.set(year, monthOfYear, dayOfMonth);
                bestBeforeDateInputField.setText(String.format("%tF", bestBeforeDate));
            }
        };
        dateFragment.show(fragMan, Constants.DATEPICKER_TAG);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_offer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void makePictureButtonClicked(View view) {
        //TODO: hasSystemFeature(PackageManager.FEATURE_CAMERA)
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //makes sure any app can handle the Intent:
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            try {
                photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), Constants.PHOTO_FILENAME);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            }
            catch (Exception e)
            {
                Toast.makeText(getBaseContext(), "Error trying to get big picture, only thumbnail will be acquired: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /* Thumbnail */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(LOG, Constants.IN_ONACTIVITY_RESULT);
        Log.i(LOG, Constants.IN_ONACTIVITY_RESULT + requestCode + " " + resultCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.i(LOG, Constants.IN_ONACTIVITY_RESULT + Constants.RESULT_OK);
            Bundle extras = data.getExtras();
            bitmap = (Bitmap)extras.get(Constants.DATA_WORD);
            photoImageView.setImageBitmap(bitmap);
            if (currentOffer.getID() >= 0) {
                currentOffer.setPictureEdited(true);
            }
        }
    }

    public void publishOfferButtonClicked(View view)
    {
        if (!formToObject()) return;
        new PublishOfferTask().execute();

    }

    private boolean formToObject()
    {
        boolean formCorrectlyFilled=true;
        View firstWrongField=null;

        if (this.photoFile != null
                && this.photoFile.isFile()
                && this.photoFile.canRead())
            currentOffer.setPicture(photoFile);
        else currentOffer.setPicture(bitmap);
        /*
        try { currentOffer.setPicture(bitmap); }
        catch (Exception e) { emailInputField.setError(e.getLocalizedMessage()); formCorrectlyFilled=false; if (firstWrongField == null) firstWrongField=longDescriptionInputField;}
        */

        try { currentOffer.setShortDescription(titleInputField.getText().toString().trim()); }
        catch (Exception e) { titleInputField.setError(e.getLocalizedMessage()); formCorrectlyFilled=false; if (firstWrongField == null) firstWrongField=titleInputField; }

        try { currentOffer.setLongDescription(longDescriptionInputField.getText().toString().trim()); }
        catch (Exception e) { longDescriptionInputField.setError(e.getLocalizedMessage()); formCorrectlyFilled=false; if (firstWrongField == null) firstWrongField=longDescriptionInputField; }

        //try { currentOffer.setCategory(Integer.parseInt(categoryInputField.getText().toString().trim())); }
        try { currentOffer.setCategory(3); }
        catch (Exception e) { categoryInputField.setError(e.getLocalizedMessage()); formCorrectlyFilled=false; if (firstWrongField == null) firstWrongField=categoryInputField;}

        try { currentOffer.setMhd(bestBeforeDateInputField.getText().toString().trim()); }
        catch (Exception e) { bestBeforeDateInputField.setError(e.getLocalizedMessage()); formCorrectlyFilled=false; if (firstWrongField == null) firstWrongField=bestBeforeDateInputField;}

        /*
        try { currentOffer.setPickupTimes(Constants.BLA_WORD); }
        catch (Exception e) { pickupTimesInputField.setError(e.getLocalizedMessage()); formCorrectlyFilled=false; if (firstWrongField == null) firstWrongField=pickupTimesInputField;}
        */

        if (firstWrongField != null)
            firstWrongField.requestFocus();

        return formCorrectlyFilled;
    }

    private class RetrieveOfferInfoTask extends AsyncTask<Void, Void, Void>
    {
        private boolean errorOccurred=false;
        private String errorMessage="";
        private ProgressDialog progressDialog;
        private File pictureFile;


        protected void onPreExecute()
        {
            progressDialog=new ProgressDialog(OfferEditActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Retrieving offer info...");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        protected Void doInBackground(Void... params)
        {
            if (!currentOffer.fillObjectFromDatabase()) {
                Log.e(LOG, currentOffer.getErrorMessage());
                errorOccurred=true;
                errorMessage=currentOffer.getErrorMessage();
                return null;
            }

            pictureFile=currentOffer.getPicture();

            return null;
        }


        protected void onPostExecute(Void param)
        {
            if (errorOccurred) {
                progressDialog.dismiss();
                Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_LONG).show();
                return;
            }

            titleInputField.setText(currentOffer.getShortDescription());
            longDescriptionInputField.setText(currentOffer.getLongDescription());
            bestBeforeDateInputField.setText(String.format("%tF", currentOffer.getMhd()));
            if (pictureFile != null) {
                photoImageView.setImageURI(null);
                photoImageView.setImageURI(Uri.fromFile(pictureFile));
            }

            progressDialog.dismiss();
            Toast.makeText(getBaseContext(), Constants.OFFER_FETCHED, Toast.LENGTH_LONG).show();
        }
    }

    private class PublishOfferTask extends AsyncTask<Void, Void, Void>
    {
        private boolean errorOccurred=false;
        private String errorMessage="";
        private ProgressDialog progressDialog;


        protected void onPreExecute()
        {
            progressDialog=new ProgressDialog(OfferEditActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Publishing offer ...");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        protected Void doInBackground(Void... params)
        {
            if (!currentOffer.saveObjectToDatabase()) {
                Log.e(LOG, currentOffer.getErrorMessage());
                errorOccurred=true;
                errorMessage=currentOffer.getErrorMessage();
                return null;
            }

            return null;
        }


        protected void onPostExecute(Void param)
        {
            if (errorOccurred) {
                progressDialog.dismiss();
                Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_LONG).show();
                return;
            }

            progressDialog.dismiss();
            Toast.makeText(getBaseContext(), Constants.OFFER_EDITED, Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
