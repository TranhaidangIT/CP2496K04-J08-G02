package models;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Service {
    private final IntegerProperty serviceId;
    private final StringProperty serviceName;
    private final ObjectProperty<BigDecimal> price;
    private final IntegerProperty categoryId;
    private final StringProperty categoryName; // For display in UI
    private final StringProperty img;
    private final IntegerProperty quantity;
    private final ObjectProperty<LocalDateTime> createdAt;

    // Constructors
    public Service() {
        this.serviceId = new SimpleIntegerProperty();
        this.serviceName = new SimpleStringProperty();
        this.price = new SimpleObjectProperty<>(BigDecimal.ZERO);
        this.categoryId = new SimpleIntegerProperty();
        this.categoryName = new SimpleStringProperty();
        this.img = new SimpleStringProperty();
        this.quantity = new SimpleIntegerProperty(0);
        this.createdAt = new SimpleObjectProperty<>(LocalDateTime.now());
    }

    public Service(int serviceId, String serviceName, BigDecimal price, int quantity,
                   int categoryId, String categoryName, String img, LocalDateTime createdAt) {
        this.serviceId = new SimpleIntegerProperty(serviceId);
        this.serviceName = new SimpleStringProperty(serviceName);
        this.price = new SimpleObjectProperty<>(price);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.categoryId = new SimpleIntegerProperty(categoryId);
        this.categoryName = new SimpleStringProperty(categoryName);
        this.img = new SimpleStringProperty(img);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
    }

    public Service(String serviceName, BigDecimal price, int categoryId) {
        this(0, serviceName, price, 0, categoryId, "", null, LocalDateTime.now());
    }

    // Getters & Setters with Property accessors
    public int getServiceId() { return serviceId.get(); }
    public void setServiceId(int serviceId) { this.serviceId.set(serviceId); }
    public IntegerProperty serviceIdProperty() { return serviceId; }

    public String getServiceName() { return serviceName.get(); }
    public void setServiceName(String serviceName) { this.serviceName.set(serviceName); }
    public StringProperty serviceNameProperty() { return serviceName; }

    public BigDecimal getPrice() { return price.get(); }
    public void setPrice(BigDecimal price) { this.price.set(price); }
    public ObjectProperty<BigDecimal> priceProperty() { return price; }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public IntegerProperty quantityProperty() { return quantity; }

    public int getCategoryId() { return categoryId.get(); }
    public void setCategoryId(int categoryId) { this.categoryId.set(categoryId); }
    public IntegerProperty categoryIdProperty() { return categoryId; }

    public String getCategoryName() { return categoryName.get(); }
    public void setCategoryName(String categoryName) { this.categoryName.set(categoryName); }
    public StringProperty categoryNameProperty() { return categoryName; }

    public String getImg() { return img.get(); }
    public void setImg(String img) { this.img.set(img); }
    public StringProperty imgProperty() { return img; }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    // Extra methods
    public BigDecimal getTotal() {
        return price.get().multiply(BigDecimal.valueOf(quantity.get()));
    }

    @Override
    public String toString() {
        return "Service{" +
                "serviceId=" + getServiceId() +
                ", serviceName='" + getServiceName() + '\'' +
                ", price=" + getPrice() +
                ", quantity=" + getQuantity() +
                ", categoryId=" + getCategoryId() +
                ", categoryName='" + getCategoryName() + '\'' +
                ", img='" + getImg() + '\'' +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
