package it.unisalento.bric48.backend.dto;


public class BeaconDTO {

    String id;
    String mac;
    String mserial;
    String threshold;


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getMac() {
        return mac;
    }
    public void setMac(String mac) {
        this.mac = mac;
    }
    public String getMserial() {
        return mserial;
    }
    public void setMserial(String mserial) {
        this.mserial = mserial;
    }
    public String getThreshold() {
        return threshold;
    }
    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }
    
    
}
