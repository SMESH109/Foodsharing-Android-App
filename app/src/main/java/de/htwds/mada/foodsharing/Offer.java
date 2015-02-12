package de.htwds.mada.foodsharing;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Offer {

    private int offerID;
    private int transactID;
    private int category;
    private String shortDescription;
    private String longDescription;
    private File picture;
    private Calendar mhd;
    private Calendar dateAdded;
    private String pickupTimes; //too complex to use date or time types


    //Exceptions
    private static final String NOT_NEGATIVE = "No negative numbers!";
    private static final String NO_ARGUMENT = "No or empty object given!";

    public Offer() {
        mhd = new GregorianCalendar();
    }

    public Offer(JSONObject offerJSONObject)
    {
        mhd = new GregorianCalendar();

        this.setOfferID(offerJSONObject.optInt("id", -1));
        this.setTransactID(offerJSONObject.optInt("transaction_id", -1));
        //TODO: this.setPicture();
        this.setShortDescription(offerJSONObject.optString("title"));
        this.setLongDescription(offerJSONObject.optString("descr"));
        //TODO: this.setMhd(userJSONObject.optString("bbd"));
        //TODO: this.setDateAdded(userJSONObject.optString("date"));
        //TODO: this.setValidDate(userJSONObject.optString("valid_date"));
    }

    public int getOfferID() {        return offerID;    }
    public void setOfferID(int offerID) {
        /*
        if (offerID < 0) {
            throw new NumberFormatException(NOT_NEGATIVE);
        }
        */
        this.offerID = offerID;
    }

    public int getTransactID() {        return transactID;    }
    public void setTransactID(int transactID) {
        if (transactID < 0) {
            throw new NumberFormatException(NOT_NEGATIVE);
        }
        this.transactID = transactID;
    }

    public int getCategory() {        return category;    }
    public void setCategory(int category) {
        if (category < 0) {
            throw new NumberFormatException(NOT_NEGATIVE);
        }
        this.category = category;
    }

    public String getShortDescription() {        return shortDescription;    }
    public void setShortDescription(String shortDescription) {
        if (shortDescription.trim().isEmpty()) {
            throw new IllegalArgumentException(NO_ARGUMENT);
        }
        this.shortDescription = shortDescription.trim();
    }

    public String getLongDescription() {        return longDescription;    }
    public void setLongDescription(String longDescription) {
        if (longDescription.trim().isEmpty()) {
            throw new IllegalArgumentException(NO_ARGUMENT);
        }
        this.longDescription = longDescription.trim();
    }

    public File getPicture() {        return picture;    }
    public void setPicture(File picture) {
        if (picture == null) {
            throw new IllegalArgumentException(NO_ARGUMENT);
        }
        this.picture = picture;
    }

    public Calendar getMhd() {        return mhd;    }
    public void setMhd(int year, int month, int day) {
        mhd.setLenient(false);          //make calendar validating
        mhd.set(year, month, day); //throws exception if date is invalid
    }

    public Calendar getDateAdded() {        return dateAdded;    }
    public void setDateAdded() {
        this.dateAdded = Calendar.getInstance(); //take current time
    }

    public String getPickupTimes() {        return pickupTimes;    }
    public void setPickupTimes(String pickupTimes) {
        if (pickupTimes.trim().isEmpty()) {
            throw new IllegalArgumentException(NO_ARGUMENT);
        }
        this.pickupTimes = pickupTimes.trim();
    }

    private String errorMessage;
    public String getErrorMessage() {return errorMessage; }


    public boolean fillObjectFromDatabase() {
        errorMessage = "";
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("oid", String.valueOf(this.getOfferID())));

        JSONParser jsonParser = new JSONParser();
        JSONObject returnObject = jsonParser.makeHttpRequest("http://odin.htw-saarland.de/get_offer_details.php", "GET", nameValuePairs);

        if (returnObject.optBoolean("success"))
        {
            JSONArray offerJSONArray=returnObject.optJSONArray("offer");
            JSONObject offerJSONObject=offerJSONArray.optJSONObject(0);
            if (offerJSONObject != null)
            {
                this.setTransactID(offerJSONObject.optInt("transaction_id", -1));
                //TODO: this.setPicture();
                this.setShortDescription(offerJSONObject.optString("title"));
                this.setLongDescription(offerJSONObject.optString("descr"));
                //TODO: this.setMhd(userJSONObject.optString("bbd"));
                //TODO: this.setDateAdded(userJSONObject.optString("date"));
                //TODO: this.setValidDate(userJSONObject.optString("valid_date"));
            }
            else
            {
                errorMessage="Could not retrieve offer info!";
                return false;
            }
        }

        if (!returnObject.optBoolean("success"))
            errorMessage=returnObject.optString("message");

        return returnObject.optBoolean("success");
    }

    public boolean saveObjectToDatabase()
    {
        errorMessage="";
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("transaction_id",String.valueOf(this.getTransactID())));
        nameValuePairs.add(new BasicNameValuePair("image_id","4"));
        nameValuePairs.add(new BasicNameValuePair("title", this.getShortDescription()));
        nameValuePairs.add(new BasicNameValuePair("descr", this.getLongDescription()));
        nameValuePairs.add(new BasicNameValuePair("bbd", this.getMhd().toString()));
        Timestamp timestamp=new Timestamp(this.getDateAdded().getTimeInMillis());
        nameValuePairs.add(new BasicNameValuePair("date", timestamp.toString()));
        nameValuePairs.add(new BasicNameValuePair("valid_date", "1423216493"));

        JSONParser jsonParser = new JSONParser();
        JSONObject returnObject = jsonParser.makeHttpRequest("http://odin.htw-saarland.de/create_offer.php", "POST", nameValuePairs);

        if (!returnObject.optBoolean("success"))
            errorMessage=returnObject.optString("message", "Unknown error!");

        return returnObject.optBoolean("success");
    }



    public String toString()
    {
        return getShortDescription() + "\n" + getLongDescription();
    }
}
