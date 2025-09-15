package lk.javainstitute.wadapola.model;

import com.google.firebase.Timestamp;
import com.google.type.DateTime;

public class BookingData {
private String customer_id;
private String worker_id;
private String booking_status;
private Timestamp date_time;
private String fname;
private String lname;
private String service_category;

private String latitude;
private String longitude;

private String mobile;
private String price;

    public BookingData(String customer_id, String worker_id, String booking_status, Timestamp date_time, String fname, String lname, String service_category, String latitude, String longitude, String mobile, String price) {
        this.customer_id = customer_id;
        this.worker_id = worker_id;
        this.booking_status = booking_status;
        this.date_time = date_time;
        this.fname = fname;
        this.lname = lname;
        this.service_category = service_category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mobile = mobile;
        this.price = price;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public BookingData() {
    }



    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getWorker_id() {
        return worker_id;
    }

    public void setWorker_id(String worker_id) {
        this.worker_id = worker_id;
    }

    public String getBooking_status() {
        return booking_status;
    }

    public void setBooking_status(String booking_status) {
        this.booking_status = booking_status;
    }

    public Timestamp getDate_time() {
        return date_time;
    }

    public void setDate_time(Timestamp date_time) {
        this.date_time = date_time;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getService_category() {
        return service_category;
    }

    public void setService_category(String service_category) {
        this.service_category = service_category;
    }
}
