package lk.javainstitute.wadapola.model;

public class ServiceCategoryData {
    private String name;

    private int resourceId;

    public ServiceCategoryData() {
    }

    public ServiceCategoryData(String name, int resourceId) {
        this.name = name;
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }
}
