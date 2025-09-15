package lk.javainstitute.wadapola.model;

public class WorkerData {
    private String id;
    private String email;
    private String mobile;
    private String fName;
    private String lName;
    private String service_category;
    private String verify_status;
    private String status;

    private String price;
    private String latitude;
    private String longitude;
private String province;

    public WorkerData() {
    }

    public WorkerData(String id, String email, String mobile, String fName, String lName, String service_category, String verify_status, String status, String price, String latitude, String longitude) {
        this.id = id;
        this.email = email;
        this.mobile = mobile;
        this.fName = fName;
        this.lName = lName;
        this.service_category = service_category;
        this.verify_status = verify_status;
        this.status = status;
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public WorkerData(String id, String email, String mobile, String fName, String lName, String service_category, String verify_status, String status, String price, String latitude, String longitude, String province) {
        this.id = id;
        this.email = email;
        this.mobile = mobile;
        this.fName = fName;
        this.lName = lName;
        this.service_category = service_category;
        this.verify_status = verify_status;
        this.status = status;
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
        this.province = province;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getfName() {
        return fName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
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

    public String getService_category() {
        return service_category;
    }

    public void setService_category(String service_category) {
        this.service_category = service_category;
    }

    public String getVerify_status() {
        return verify_status;
    }

    public void setVerify_status(String verify_status) {
        this.verify_status = verify_status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
