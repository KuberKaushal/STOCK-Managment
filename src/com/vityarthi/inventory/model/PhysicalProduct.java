package com.vityarthi.inventory.model;

/**
 * Concrete physical tangible product with weight and shipping fees.
 */
public class PhysicalProduct extends Product {
    private static final long serialVersionUID = 1L;

    private double weightKg;
    private double shippingFee;

    public PhysicalProduct(String id, String name, String category, double unitPrice,
                           int stockQuantity, int minThreshold, double weightKg, double shippingFee) {
        super(id, name, category, unitPrice, stockQuantity, minThreshold);
        if (weightKg < 0) throw new IllegalArgumentException("Weight cannot be negative.");
        if (shippingFee < 0) throw new IllegalArgumentException("Shipping fee cannot be negative.");
        this.weightKg = weightKg;
        this.shippingFee = shippingFee;
    }

    @Override
    public double calculateTaxRate() {
        return 0.12; // 12% standard goods tax
    }

    @Override
    public String getProductType() {
        return "PHYSICAL";
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Wt: %.2fkg | Ship: $%.2f", weightKg, shippingFee);
    }
}
