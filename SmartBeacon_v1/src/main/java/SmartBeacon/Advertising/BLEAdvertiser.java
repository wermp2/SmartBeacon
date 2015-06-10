/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SmartBeacon.Advertising;

import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author Noemi
 */
public class BLEAdvertiser {

    private String advertisingData;
    private String uuid;
    private String major;
    private String minor;
    
    public BLEAdvertiser(String advertisingData, String uuid, String major, String minor){
        this.advertisingData = advertisingData;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;        
    }
    
    
    private void startBLEDongle(){
              
        String command = "sudo hciconfig hci0 up";
        Runtime runtime=Runtime.getRuntime();
        try {
            Process process=runtime.exec(command);
            System.out.println(process.waitFor());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } 
    }
    
    private void stopBLEDongle(){
        String command = "sudo hciconfig hci0 down";
        Runtime runtime=Runtime.getRuntime();
        try {
            Process process=runtime.exec(command);
            System.out.println(process.waitFor());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } 
    }
    
    private void setName(String advertisingData){
        String command = "sudo hciconfig hci0 name " + advertisingData;
        Runtime runtime=Runtime.getRuntime();
        try {
            Process process=runtime.exec(command);
            System.out.println(process.waitFor());                 
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } 
    }
    
    private void stopLEAdvertising(){
        String command = "sudo hciconfig hci0 noleadv";
        Runtime runtime=Runtime.getRuntime();
        try {
            Process process=runtime.exec(command);
            System.out.println(process.waitFor());
        } catch (IOException | InterruptedException e) {
             e.printStackTrace();
        }        
    }
    
    private void startLEAdvertising(){
        String command = "sudo hciconfig hci0 leadv 3";
        Runtime runtime=Runtime.getRuntime();
        try {
            Process process=runtime.exec(command);
            System.out.println(process.waitFor());
        } catch (IOException | InterruptedException e) {
             e.printStackTrace();
        }        
    }
    
    private void disableScan(){
        String command = "sudo hciconfig hci0 noscan";
        Runtime runtime=Runtime.getRuntime();
        try {
            Process process=runtime.exec(command);
            process.waitFor();                
        } catch (IOException | InterruptedException e) {
             e.printStackTrace();
        } 
        
        
    }
    
    private void setBeaconData(String uuid, String major, String minor){
        
        String command = "sudo hcitool -i hci0 cmd 0x08 0x0008 1E 02 01 1A 1A FF 4C 00 02 15 " + uuid + " " + major + " " + minor + " C8 00";
        Runtime runtime=Runtime.getRuntime();
        try {
            Process process=runtime.exec(command);
            System.out.println(process.waitFor());
        } catch (IOException | InterruptedException e) {
             e.printStackTrace();
        }          
    }
      
    
    public void startSmartBeacon(){
        String command = "tf";
        Runtime runtime=Runtime.getRuntime();
        try {
            Process process=runtime.exec(command);
            System.out.println(process.waitFor());
        } catch (IOException | InterruptedException e) {
             e.printStackTrace();
        }   
        command = "tf";
        runtime=Runtime.getRuntime();
        try {
            Process process=runtime.exec(command);
            System.out.println(process.waitFor());
        } catch (IOException | InterruptedException e) {
             e.printStackTrace();
        } 
        startBLEDongle();
        //setPIscan();
        setName(this.advertisingData);
        stopLEAdvertising();
        startLEAdvertising();
        setBeaconData(this.uuid, this.major, this.minor);           
    }
       
    public void stopAdvertising(){
        stopBLEDongle();        
    }

    /**
     * @return the advertisingData
     */
    public String getAdvertisingData() {
        return advertisingData;
    }

    /**
     * @param advertisingData the advertisingData to set
     */
    public void setAdvertisingData(String advertisingData) {
        this.advertisingData = advertisingData;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the major
     */
    public String getMajor() {
        return major;
    }

    /**
     * @param major the major to set
     */
    public void setMajor(String major) {
        this.major = major;
    }

    /**
     * @return the minor
     */
    public String getMinor() {
        return minor;
    }

    /**
     * @param minor the minor to set
     */
    public void setMinor(String minor) {
        this.minor = minor;
    } 
    
}
