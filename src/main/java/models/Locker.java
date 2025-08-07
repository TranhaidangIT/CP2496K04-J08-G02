package models;

public class Locker {
    private int lockerId;
    private String lockerNumber;
    private String locationInfo;
    private String status;

    public Locker(int lockerId, String lockerNumber, String locationInfo, String status) {
        this.lockerId = lockerId;
        this.lockerNumber = lockerNumber;
        this.locationInfo = locationInfo;
        this.status = status;
    }

    public int getLockerId() {
        return lockerId;
    }

    public void setLockerId(int lockerId) {
        this.lockerId = lockerId;
    }

    public String getLockerNumber() {
        return lockerNumber;
    }

    public void setLockerNumber(String lockerNumber) {
        this.lockerNumber = lockerNumber;
    }

    public String getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(String locationInfo) {
        this.locationInfo = locationInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}