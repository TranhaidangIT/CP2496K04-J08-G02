package models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Đại diện cho một đối tượng Dịch vụ với các thuộc tính phù hợp để liên kết dữ liệu JavaFX.
 */
public class Service {
    private final StringProperty id;
    private final StringProperty serviceName;
    private final DoubleProperty price;
    private final IntegerProperty quantity;
    private final DoubleProperty total; // Thuộc tính này sẽ được tính toán
    private final StringProperty category;

    /**
     * Xây dựng một đối tượng Dịch vụ mới.
     * @param id ID duy nhất của dịch vụ.
     * @param serviceName Tên của dịch vụ.
     * @param price Giá của một đơn vị dịch vụ.
     * @param quantity Số lượng dịch vụ.
     * @param category Danh mục của dịch vụ.
     */
    public Service(String id, String serviceName, double price, int quantity, String category) {
        this.id = new SimpleStringProperty(id);
        this.serviceName = new SimpleStringProperty(serviceName);
        this.price = new SimpleDoubleProperty(price);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.category = new SimpleStringProperty(category);
        this.total = new SimpleDoubleProperty(price * quantity); // Tính toán tổng ngay khi khởi tạo
    }

    // --- Getter thuộc tính cho liên kết dữ liệu JavaFX ---

    public StringProperty idProperty() { return id; }
    public StringProperty serviceNameProperty() { return serviceName; }
    public DoubleProperty priceProperty() { return price; }
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty totalProperty() { return total; }
    public StringProperty categoryProperty() { return category; }

    // --- Standard Getters for Property Values ---

    public String getId() { return id.get(); }
    public String getServiceName() { return serviceName.get(); }
    public double getPrice() { return price.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getTotal() { return total.get(); }
    public String getCategory() { return category.get(); }

    // --- Standard Setters for Property Values ---

    public void setId(String id) { this.id.set(id); }
    public void setServiceName(String serviceName) { this.serviceName.set(serviceName); }
    public void setPrice(double price) {
        this.price.set(price);
        updateTotal(); // Cập nhật tổng khi giá thay đổi
    }
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
        updateTotal(); // Cập nhật tổng khi số lượng thay đổi
    }
    public void setCategory(String category) { this.category.set(category); }

    // Phương thức trợ giúp để cập nhật tổng
    private void updateTotal() {
        this.total.set(this.price.get() * this.quantity.get());
    }
}