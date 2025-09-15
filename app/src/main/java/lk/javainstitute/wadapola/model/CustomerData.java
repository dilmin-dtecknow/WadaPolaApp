package lk.javainstitute.wadapola.model;

public class CustomerData {
private String id;
private String email;
private String mobile;
private String fName;
private String lName;
private String status;
private String verifyStatus;

    public CustomerData() {
    }

    public CustomerData(String id, String email, String mobile, String fName, String lName, String status, String verifyStatus) {
        this.id = id;
        this.email = email;
        this.mobile = mobile;
        this.fName = fName;
        this.lName = lName;
        this.status = status;
        this.verifyStatus = verifyStatus;
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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(String verifyStatus) {
        this.verifyStatus = verifyStatus;
    }
}
