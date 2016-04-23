/*
 * Created by Timothy Street on 2016.04.12  * 
 * Copyright © 2016 Timothy Street. All rights reserved. * 
 */
package com.streetsmart.managers;

import com.streetsmart.entitypackage.Photo;
import com.streetsmart.entitypackage.Pin;
import com.streetsmart.entitypackage.User;
import com.streetsmart.sessionbeanpackage.PhotoFacade;
import com.streetsmart.sessionbeanpackage.PinFacade;
import com.streetsmart.sessionbeanpackage.UserFacade;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import javax.faces.application.FacesMessage;
import org.primefaces.model.UploadedFile;

@Named(value = "pinManager")
@SessionScoped
/**
 *
 * @author Tim
 */
public class PinManager implements Serializable {

    private static final String HIDDEN = "visibility: hidden";
    private static final String NOT_DISPLAYED = "display: none";
    
    // Instance Variables (Properties) for Pins 
    private UploadedFile file;
    private String newPinTitle;
    private String newPinDescription;
    private boolean newPinAnonymous;
    private boolean newPinPhotoExists;
    private int selectedPinId;
    private Pin selectedPin;
    private List<Pin> mapMenuPins;
    private List<Pin> pinValues;
    private String filterDistance;
    private String filterOption;
    private String distanceFilterStyle;
    private String keywordFilterInput;
    private String keywordFilterStyle;
    private Pin pin;

    /**
     * The instance variable 'userFacade' is annotated with the @EJB annotation.
     * This means that the GlassFish application server, at runtime, will inject
     * in this instance variable a reference to the @Stateless session bean
     * PhotoFacade.
     */
    @EJB
    private UserFacade userFacade;

    /**
     * The instance variable 'pinFacade' is annotated with the @EJB annotation.
     * This means that the GlassFish application server, at runtime, will inject
     * in this instance variable a reference to the @Stateless session bean
     * PhotoFacade.
     */
    @EJB
    private PinFacade pinFacade;
    
     /**
     * The instance variable 'photoFacade' is annotated with the @EJB annotation.
     * This means that the GlassFish application server, at runtime, will inject
     * in this instance variable a reference to the @Stateless session bean
     * PhotoFacade.
     */
    @EJB
    private PhotoFacade photoFacade;

    public PhotoFacade getPhotoFacade() {
        return photoFacade;
    }

    public void setPhotoFacade(PhotoFacade photoFacade) {
        this.photoFacade = photoFacade;
    }

    public PinManager() {
        filterDistance = "10.0";
        distanceFilterStyle = NOT_DISPLAYED;
        keywordFilterStyle = NOT_DISPLAYED;
        keywordFilterInput = "";
        // Set to initially filter by popularity.
        filterOption = "pop";
        selectedPin = new Pin();
        selectedPin.setTimePosted(0); 
    }

    public PinFacade getPinFacade() {
        return pinFacade;
    }

    public void setPinFacade(PinFacade pinFacade) {
        this.pinFacade = pinFacade;
    }

    // Returns the uploaded file
    public UploadedFile getFile() {
        return file;
    }

    // Obtains the uploaded file
    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public String getNewPinTitle() {
        return newPinTitle;
    }

    public void setNewPinTitle(String newPinTitle) {
        this.newPinTitle = newPinTitle;
    }

    public String getNewPinDescription() {
        return newPinDescription;
    }

    public void setNewPinDescription(String newPinDescription) {
        this.newPinDescription = newPinDescription;
    }

    public boolean isNewPinAnonymous() {
        return newPinAnonymous;
    }

    public void setNewPinAnonymous(boolean newPinAnonymous) {
        this.newPinAnonymous = newPinAnonymous;
    }

    public boolean isNewPinPhotoExists() {
        return newPinPhotoExists;
    }

    public void setNewPinPhotoExists(boolean newPinPhotoExists) {
        this.newPinPhotoExists = newPinPhotoExists;
    }

    public String getFilterDistance() {
        return filterDistance;
    }

    public void setFilterDistance(String filterDistance) {

        if (filterDistance.isEmpty()) {
            this.filterDistance = "10.0";
            return;
        }

        /* Attempt to parse the string*/
        try {
            Double.parseDouble(filterDistance);
        } catch (NumberFormatException | NullPointerException e) {
            this.filterDistance = "10.0";
            return;
        }

        this.filterDistance = filterDistance;
    }

    public int getSelectedPinId() {
        return selectedPinId;
    }

    public void setSelectedPinId(int selectedPinId) {
        this.selectedPinId = selectedPinId;
        this.selectedPin = pinFacade.findPinWithId(selectedPinId);
    }

    public String getKeywordFilterInput() {
        return keywordFilterInput;
    }

    public void setKeywordFilterInput(String keywordFilterInput) {
        this.keywordFilterInput = keywordFilterInput;
    }

    public Pin getSelectedPin() {
        return selectedPin;
    }

    public void setSelectedPin(Pin selectedPin) {
        this.selectedPin = selectedPin;
    }

    public String getKeywordFilterStyle() {
        return keywordFilterStyle;
    }

    public void setKeywordFilterStyle(String keywordFilterStyle) {
        this.keywordFilterStyle = keywordFilterStyle;
    }
    
    public String createPin() {
        
        User user = userFacade.find(FacesContext.getCurrentInstance().
                        getExternalContext().getSessionMap().get("user_id"));

        String[] latAndLong = this.getParsedUserLoc();

        // Generate timestamp for pin posting time
        int timestamp = (int) (new Date().getTime() / 1000);

        try {
            
            pin = new Pin();
            pin.setUsername(user.getFirstName() + " " + user.getLastName());
            pin.setAnonymous(this.newPinAnonymous);

            // If the pin is not anonymous and a User is currently logged
            // in, set the associated User id.
            if (!newPinAnonymous) {
                pin.setUserId(user.getId());
            } else {
                // Otherwise, set the id to a row in the User table associated
                // with all anonymous users (i.e. users are anonymous with id
                // = 1)
                pin.setUserId(1);
            }

            pin.setDescription(this.newPinDescription);
            pin.setDownvotes(0);
            pin.setUpvotes(0);
            //pin.setId(1); // no need for this if pin id is auto increment
            pin.setTitle(this.newPinTitle);
            pin.setLatitude(Float.parseFloat(latAndLong[0]));
            pin.setLongitude(Float.parseFloat(latAndLong[1]));
            pin.setTimePosted(timestamp);
            pin.setType("Some_pin_type");
            pin.setReports(0);
            pinFacade.create(pin);
            if (file.getSize() != 0) {
                pin.setPhoto(true);
                copyPhotoFile(file);
            } else {
                pin.setPhoto(false);
            }
            return "index?faces-redirect=true";
        } catch (EJBException e) {
            //TODO: Print useful error message somehow
        }
        return "";
    }
    
    /**
     * Grabs the image uploaded by the user and displays it somewhere.
     * 1. If the user is anonymous - Grabs a random default photo from the storage
     * 2. If the user has a profile photo but did not upload a photo on the pin - 
     * then we will display that profile photo instead.
     * 3. If the user has a pin photo uploaded to that pin ID - then we will use this
     * photo instead (whether they have a profile pic or not).
     * 4. If the user is anonymous and they have uploaded a photo - then we will 
     * use the image that was uploaded file name
     * @return String - file path to the photo
     */
    public String getImageFromPin()
    {
        String resultFilePath = "";
        
        // If the user has selected the anonymous checbox in Create Pin
        if (this.newPinAnonymous)
        {
            // If the user has not uploaded any images
            if (file.getSize() == 0) {
                // Initialize the String array of directory path with picture names
                String[] defaultDirectoryNames = {"default-1.png", "default-2.png", 
                    "default-3.png","default-4.png","default-5.png"};

                Random randomProfileDefaultPicture = new Random();

                // Grabs the random index of the photo
                int index = randomProfileDefaultPicture.nextInt(defaultDirectoryNames.length);

                // Grabs the source of the path file
                Path source = Paths.get(Constants.ROOT_DIRECTORY + "/" + defaultDirectoryNames[index]);

                // If the files exist inside the directory
                if (Files.exists(source))
                {
                    // Stores the file path onto the string variable to return
                    resultFilePath = defaultDirectoryNames[index];
                }
            }
            else
            {
                // Grabs the latest pin ID that was created
                int pinID = pinFacade.findLastID();
                
                // Grabs the file path using the pinID
                Path source = Paths.get(Constants.ROOT_DIRECTORY + "/p_" + pinID + ".png");
                
                // If the file from the source exists inside the directory
                if (Files.exists(source))
                {
                    // Grabs the source file as a string and stores it inside a variable
                    resultFilePath = "p_" + pinID + ".png";
                } 
            }
        }
        
        else 
        {
            // Grabs the user name from the FacesContext
            String user_name = (String) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("username");
            
            // Finds the user from their username if they are signed in
            User user = userFacade.findByUsername(user_name);
            
            // If the user has uploaded a photo onto the pin
            // pin.getPhoto() method is a boolean value that returns true if the
            // user has created a photo under the new pin
            if (pin.getPhoto())
            {
                // Grabs the latest pin ID that was created
                int pinID = pinFacade.findLastID();
                
                // Grabs the file path using the pinID
                Path source = Paths.get(Constants.ROOT_DIRECTORY + "/p_" + pinID + ".png");
                
                // If the file from the source exists inside the directory
                if (Files.exists(source))
                {
                    // Grabs the source file as a string and stores it inside a variable
                    resultFilePath = "p_" + pinID + ".png";
                }
            }
            
            // If the user has a default photo AND they have not uploaded a picture
            // Then we will use their default photo as the image on the sidebar
            else if (!pin.getPhoto())
            {
                // Grabs the source directory of that file
                Path source = Paths.get(Constants.ROOT_DIRECTORY + "u_" + user.getId() + ".png");
                
                // Only stores the file source in the variable if it exists
                if (Files.exists(source))
                {
                    resultFilePath = "u_" + user.getId() + ".png";
                }
            }
            
        }
        
        return resultFilePath;
    }
    

    public FacesMessage copyPhotoFile(UploadedFile file) {
        try {
            InputStream in = file.getInputstream();

            File tempFile = inputStreamToFile(in, Constants.TEMP_FILE);
            in.close();

            FacesMessage resultMsg;

            int pinID = pinFacade.findLastID();

            Pin pin = pinFacade.findPinWithId(pinID);

            String extension = file.getContentType();
            extension = extension.startsWith("image/") ? extension.subSequence(6, extension.length()).toString() : "png";

            in = file.getInputstream();
            inputStreamToFile(in, "p_" + pinID + ".png");
            // use uploadedFile if we want to make thumbnails
            resultMsg = new FacesMessage("Success!", "File Successfully Uploaded!");
            return resultMsg;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FacesMessage("Upload failure!",
                "There was a problem reading the image file. Please try again with a new photo file.");
    }
    
    public String upvotePin(){
        Pin pinToUpdate = this.getSelectedPin();
        pinToUpdate.setUpvotes(pinToUpdate.getUpvotes()+1);
        pinFacade.edit(pinToUpdate);
        return "";
    }
    
    public String downvotePin(){
        Pin pinToUpdate = this.getSelectedPin();
        pinToUpdate.setDownvotes(pinToUpdate.getDownvotes()+1);
        pinFacade.edit(pinToUpdate);
        return "";
    }

    /**
     *
     * @param pin
     * @return
     */
    public String getFormattedDate(Pin pin) {
        SimpleDateFormat format = new SimpleDateFormat("MMM d");
        return format.format(new Date(((long) pin.getTimePosted()) * 1000L));
    }
    
    public String getFullFormattedDate(Pin pin) {
        SimpleDateFormat format = new SimpleDateFormat();
        return format.format(new Date(((long) pin.getTimePosted()) * 1000L));
    }

    public List<Pin> getMapMenuPins() {
        return mapMenuPins;
    }

    public void setMapMenuPins(List<Pin> mapMenuPins) {
        this.mapMenuPins = mapMenuPins;
    }

    public String getFilterOption() {
        return filterOption;
    }

    public void setFilterOption(String filterOption) {
        if (filterOption == null || filterOption.isEmpty()) {
            return;
        }
        
        switch (filterOption) {
            case "dist":
                this.filterByDistance();
                this.setDistanceFilterStyle("");
                this.setKeywordFilterStyle(HIDDEN);
                break;
            case "pop":
                this.filterByPopularity();
                this.setDistanceFilterStyle(HIDDEN);
                this.setKeywordFilterStyle(HIDDEN);
                break;
            case "new":
                this.filterByNewest();
                this.setDistanceFilterStyle(HIDDEN);
                this.setKeywordFilterStyle(HIDDEN);
                break;
            case "key":
                //this.filterByKeyword();
                this.setDistanceFilterStyle(HIDDEN);
                this.setKeywordFilterStyle("");
            default:
                break;
        }
        this.filterOption = filterOption;
    }

    public String getDistanceFilterStyle() {
        return distanceFilterStyle;
    }

    public void setDistanceFilterStyle(String distanceFilterStyle) {
        this.distanceFilterStyle = distanceFilterStyle;
    }

    public String[] getParsedUserLoc() {
        // Parse out latitiude and longitude from container
        String locData = (String) FacesContext.getCurrentInstance().
                getExternalContext().getSessionMap().get("userLoc");
        // If locData could not be retrieved, stop parsing
        if (locData == null) {
            return null;
        }
        locData = locData.replace("(", "");
        locData = locData.replace(")", "");
        return locData.split(", ");
    }

    /**
     *
     */
    public void filterByKeyword() {

        List<Pin> keywordPins;
        if (keywordFilterInput == null || keywordFilterInput.isEmpty()) {
            mapMenuPins = pinFacade.findAll();
            return;           
        } else if (keywordFilterInput.length() < 3) {            
            return;
        } else {
            keywordPins = mapMenuPins;
        }

        // We want to find all pins in MapMenuPins matching the keyword so:
        for (int i = 0; i < keywordPins.size(); i++) {
            Pin pin = keywordPins.get(i);
            if (!pin.getTitle().contains(keywordFilterInput)
                    && !pin.getDescription().contains(keywordFilterInput)) {
                keywordPins.remove(pin);
            }
        }      
        this.setMapMenuPins(keywordPins);
    }

    /**
     *
     */
    public void filterByDistance() {
        String[] currentUserLoc = this.getParsedUserLoc();
        Double filterDist = Double.parseDouble(this.filterDistance);

        if (currentUserLoc != null) {
            float userLat = Float.valueOf(currentUserLoc[0]);
            float userLong = Float.valueOf(currentUserLoc[1]);
            
            List<Pin> distancePins;
            distancePins = pinFacade.findAll();
            
            for (int i = 0; i < distancePins.size(); i++) {
                Pin pin = distancePins.get(i);
                double distanceInMiles = this.getDistanceFromLatLongInMiles(userLat, userLong,
                        pin.getLatitude(), pin.getLongitude());
                if (!(distanceInMiles <= filterDist)) {
                    distancePins.remove(i);
                }
            }
            this.setMapMenuPins(distancePins);
        }
    }

    /**
     *
     */
    public void filterByNewest() {  
        
        this.sortPins(pinFacade.findAll(), "time");
        this.setMapMenuPins(this.pinValues);
    }

    /**
     *
     */
    public void filterByPopularity() {       

        this.sortPins(pinFacade.findAll(), "popularity");
        this.setMapMenuPins(this.pinValues);
    }
    
    /**
     * Pre populates variables for faster filtering and sets the initial state
     * of the side menu to filter all pins by popularity.
     */
    public void prePopulateMenu() {
        this.filterByPopularity();       
    }

    /**
     *
     * @param lat1
     * @param long1
     * @param lat2
     * @param long2
     * @return
     */
    public double getDistanceFromLatLongInMiles(float lat1, float long1,
            float lat2, float long2) {
        double R = 3958.756; // Mean radius of the earth in miles
        double dLat = deg2rad(lat2 - lat1);
        double dLon = deg2rad(long2 - long1);
        double a
                = ((Math.sin(dLat / 2) * Math.sin(dLat / 2))
                + (Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2)));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (R * c); // Distance in miles
    }

    /**
     *
     * @param deg
     * @return
     */
    private float deg2rad(float deg) {
        return (float) (deg * (Math.PI / 180));
    }

    /**
     * Quicksort implementation for sorting pins depending on the sort type.
     *
     * @param pins
     * @param sortType
     */
    private void sortPins(List<Pin> pins, String sortType) {
        // check for empty or null array
        if (pins == null || pins.isEmpty()
                || sortType == null || sortType.isEmpty()) {
            return;
        }

        this.pinValues = pins;
        if (sortType.equals("time")) {
            quicksortByTimePosted(0, (pinValues.size()) - 1);
        } else if (sortType.equals("popularity")) {
            quicksortByPopularity(0, (pinValues.size()) - 1);
        }
    }

    /**
     *
     * @param low
     * @param high
     */
    private void quicksortByTimePosted(int low, int high) {
        int i = low, j = high;

        int pivot = pinValues.get(low + (high - low) / 2).getTimePosted();

        while (i <= j) {

            while (pinValues.get(i).getTimePosted() > pivot) {
                i++;
            }

            while (pinValues.get(j).getTimePosted() < pivot) {
                j--;
            }

            if (i <= j) {
                exchange(i, j);
                i++;
                j--;
            }
        }

        if (low < j) {
            quicksortByTimePosted(low, j);
        }
        if (i < high) {
            quicksortByTimePosted(i, high);
        }
    }

    /**
     *
     * @param low
     * @param high
     */
    private void quicksortByPopularity(int low, int high) {
        int i = low, j = high;

        int pivot = pinValues.get(low + (high - low) / 2).getScore();

        while (i <= j) {

            while (pinValues.get(i).getScore() > pivot) {
                i++;
            }

            while (pinValues.get(j).getScore() < pivot) {
                j--;
            }

            if (i <= j) {
                exchange(i, j);
                i++;
                j--;
            }
        }

        if (low < j) {
            quicksortByPopularity(low, j);
        }
        if (i < high) {
            quicksortByPopularity(i, high);
        }
    }

    /**
     * Generic swap for two Pin objects in a list. Helper method for quicksort
     * implementation.
     *
     * @param i
     * @param j
     */
    private void exchange(int i, int j) {
        Pin temp = pinValues.get(i);
        pinValues.set(i, pinValues.get(j));
        pinValues.set(j, temp);
    }

    private File inputStreamToFile(InputStream inputStream, String childName)
            throws IOException {
        // Read in the series of bytes from the input stream
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);

        // Write the series of bytes on file.
        File targetFile = new File(Constants.ROOT_DIRECTORY, childName);

        OutputStream outStream;
        outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
        outStream.close();

        // Save reference to the current image.
        return targetFile;
    }
}
