package lk.javainstitute.wadapola.model;

import com.google.firebase.Timestamp;

public class PaymentHistoryData {
    private String pay_id;
    private String cus_id;
    private String worker_id;
    private String work_hors;
    private String price;
    private Timestamp date_time;
    private String payment_status;
    private String fName;
    private String lName;
    private String mobile;
    private float rating;

    private String type;
    private String email;

    public PaymentHistoryData(String pay_id, String cus_id, String worker_id, String work_hors, String price, Timestamp date_time, String payment_status, String fName, String lName, String mobile, float rating) {
        this.pay_id = pay_id;
        this.cus_id = cus_id;
        this.worker_id = worker_id;
        this.work_hors = work_hors;
        this.price = price;
        this.date_time = date_time;
        this.payment_status = payment_status;
        this.fName = fName;
        this.lName = lName;
        this.mobile = mobile;
        this.rating = rating;
    }

    public String getPay_id() {
        return pay_id;
    }

    public void setPay_id(String pay_id) {
        this.pay_id = pay_id;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public PaymentHistoryData(String pay_id, String cus_id, String worker_id, String work_hors, String price, Timestamp date_time, String payment_status, String fName, String lName, String mobile, float rating, String type, String email) {
        this.pay_id = pay_id;
        this.cus_id = cus_id;
        this.worker_id = worker_id;
        this.work_hors = work_hors;
        this.price = price;
        this.date_time = date_time;
        this.payment_status = payment_status;
        this.fName = fName;
        this.lName = lName;
        this.mobile = mobile;
        this.rating = rating;
        this.type = type;
        this.email = email;
    }

    public PaymentHistoryData(String pay_id, String cus_id, String worker_id, String work_hors, String price, Timestamp date_time, String payment_status, String fName, String lName, String mobile, float rating, String type) {
        this.pay_id = pay_id;
        this.cus_id = cus_id;
        this.worker_id = worker_id;
        this.work_hors = work_hors;
        this.price = price;
        this.date_time = date_time;
        this.payment_status = payment_status;
        this.fName = fName;
        this.lName = lName;
        this.mobile = mobile;
        this.rating = rating;
        this.type = type;
    }

    public PaymentHistoryData(String cus_id, String worker_id, String work_hors, String price, Timestamp date_time, String payment_status, String fName, String lName, String mobile, float rating) {
        this.cus_id = cus_id;
        this.worker_id = worker_id;
        this.work_hors = work_hors;
        this.price = price;
        this.date_time = date_time;
        this.payment_status = payment_status;
        this.fName = fName;
        this.lName = lName;
        this.mobile = mobile;
        this.rating = rating;
    }

    public PaymentHistoryData(String cus_id, String worker_id, String work_hors, String price, Timestamp date_time, String payment_status, String fName, String lName, String mobile) {
        this.cus_id = cus_id;
        this.worker_id = worker_id;
        this.work_hors = work_hors;
        this.price = price;
        this.date_time = date_time;
        this.payment_status = payment_status;
        this.fName = fName;
        this.lName = lName;
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCus_id() {
        return cus_id;
    }

    public void setCus_id(String cus_id) {
        this.cus_id = cus_id;
    }

    public String getWorker_id() {
        return worker_id;
    }

    public void setWorker_id(String worker_id) {
        this.worker_id = worker_id;
    }

    public String getWork_hors() {
        return work_hors;
    }

    public void setWork_hors(String work_hors) {
        this.work_hors = work_hors;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Timestamp getDate_time() {
        return date_time;
    }

    public void setDate_time(Timestamp date_time) {
        this.date_time = date_time;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public PaymentHistoryData() {
    }
}
