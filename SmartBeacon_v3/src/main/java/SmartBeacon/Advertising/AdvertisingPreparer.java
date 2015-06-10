package SmartBeacon.Advertising;

import SmartBeacon.Utils.HexConverter;
import com.google.common.base.Splitter;
import java.util.ArrayList;

/**
 * Class to prepare the commands for advertising beacon and additional data
 * @author stucn3, wermp2
 */
public class AdvertisingPreparer {
    
    private String longitude = null;
    private String latitude = null;
    private String altitude = null;
    private String data = null;
    private String uuid = null;
    private String minor = null;
    private String major = null;
    private final int LENGTH = 11;
    
           
    /**
     * Creates the command which sets the UUID, major and minor values of the Beacon
     * @return the command
     */
    public String createBeaconDataCommand(){
        //create command for beacon data out of uuid, major and minor
        String command = "sudo hcitool -i hci0 cmd 0x08 0x0008 1E 02 01 1A 1A FF 4C 00 02 15 " + this.uuid + " " + this.major + " " + this.minor + " C8 00";
        return command;
    }
    
    /**
     * Creates the commands to advertise GPS and additional data
     * @return an arraylist of commands used for advertising
     */
    public ArrayList<String> createAdvertisingDataCommmands(){
        ArrayList<String>  partList = new ArrayList<>();
        ArrayList<String> commands = new ArrayList<>();
        
        //if there is a GPS coordinate
        if(longitude != null){
            //create advertising parts and add them to an array
            String partLongitude = "{\"1\":{\"long\":\""+longitude+"\"}}";
            System.out.println(partLongitude);
            partList.add(partLongitude);

            String partLatitude = "{\"2\":{\"lat\":\""+latitude+"\"}}";
            System.out.println(partLatitude);
            partList.add(partLatitude);

            String partAltitude = "{\"3\":{\"alt\":\""+altitude+"\"}}";
            System.out.println(partAltitude);
            partList.add(partAltitude);
        }
        //if there is any additional data
        if (data != null) 
        {
            //split data to parts of max. 12 characters  
            for(final String token : Splitter.fixedLength(LENGTH).split(data)) 
            {     
                //create advertising parts and add them to array
                String partData = "{\""+(partList.size()+1)+"\":{\"data\":\""+token+"\"}}";
                System.out.println(partData);
                partList.add(partData);           
            }
        }
        //add the end of the advertisement
        partList.add("{\""+(partList.size()+1)+"\":\"end of advertisement\"}");
        
        //go through the array        
        for(String part : partList){
            //get the lenght of the part and add 1
            int length = part.length()+1;
            //convert length into hex
            String lengthString = HexConverter.intToHex(length);
            //add 1 to length
            length = length+1;
            //convert new lenght into hex
            String lengthFullString= HexConverter.intToHex(length);
            //convert advertising part into hex
            String partHex = HexConverter.stringToHex(part);
            //create command out of lengths and part
            String command = "sudo hcitool -i hci0 cmd 0x08 0x0009 "+lengthFullString+" "+lengthString+" 09 "+partHex+"00";
            
            commands.add(command);            
        }
        return commands;
    }
    
    /**
     * Set GPS longitude
     * @param longitude the longitude to set
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     * Set GPS latitude
     * @param latitude the latitude to set
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * Set GPS altitude
     * @param altitude the altitude to set
     */
    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    /**
     * Set additional data
     * @param data the data to set
     */
    public void setData(String data) {
        this.data = data;
    }
    
     /**
     * Set UUID of the beacon 
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Set minor value of the beacon
     * @param minor the minor to set
     */
    public void setMinor(String minor) {
        this.minor = minor;
    }

    /**
     * Set major value of the beacon
     * @param major the major to set
     */
    public void setMajor(String major) {
        this.major = major;
    }
}
