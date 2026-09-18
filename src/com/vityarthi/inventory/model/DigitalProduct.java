package com.vityarthi.inventory.model;

/**
 * Concrete digital license or downloadable good with 0 shipping and 18% tax.
 */
public class DigitalProduct extends Product {
    private static final long serialVersionUID = 1L;

    private String downloadUrl;
    private String licenseKeyFormat;

    public DigitalProduct(String id, String name, String category, double unitPrice,
                          int stockQuantity, int minThreshold, String downloadUrl, String licenseKeyFormat) {
        super(id, name, category, unitPrice, stockQuantity, minThreshold);
        this.downloadUrl = (downloadUrl == null) ? "https://delivery.vityarthi.com/dl" : downloadUrl.trim();
        this.licenseKeyFormat = (licenseKeyFormat == null) ? "STANDARD-KEY" : licenseKeyFormat.trim();
    }

    @Override
    public double calculateTaxRate() {
        return 0.18; // 18% digital services tax
    }

    @Override
    public String getProductType() {
        return "DIGITAL ";
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getLicenseKeyFormat() {
        return licenseKeyFormat;
    }

    public void setLicenseKeyFormat(String licenseKeyFormat) {
        this.licenseKeyFormat = licenseKeyFormat;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | URL: %s | KeyFmt: %s", downloadUrl, licenseKeyFormat);
    }
}
