package dealership;

import java.util.Locale;

public class Vehicle implements VehicleInterface, Comparable<Vehicle> {

    public static enum FuelType { PETROL, DIESEL, HYBRID }

    private static final double DEPRECIATION_PER_YEAR = 150_000;
    private static final double MIN_PRICE = 200_000;

    private String brand;
    private String model;
    private double basePrice;
    private int age;
    private double km;
    private FuelType fuelType;

    public Vehicle(String brand, String model, double basePrice, int age, double km, FuelType fuelType) {
        this.brand = brand;
        this.model = model;
        this.basePrice = basePrice;
        this.age = age;
        this.km = km;
        this.fuelType = fuelType;
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getKm() { return km; }
    public void setKm(double km) { this.km = km; }

    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }

    @Override
    public void applyDiscount(int percentage) {
        basePrice -= basePrice * percentage / 100.0;
    }

    @Override
    public int getFinalPrice() {
        double price = Math.max(basePrice - age * DEPRECIATION_PER_YEAR, MIN_PRICE);
        if (fuelType == FuelType.HYBRID) {
            price *= 1.15;
        } else if (fuelType == FuelType.DIESEL) {
            price *= 1.05;
        }
        return (int) Math.round(price);
    }

    @Override
    public double averageKmPerYear() {
        return age == 0 ? km : km / age;
    }

    // Ascending by final price. Brand and model break ties, otherwise
    // the TreeSet would treat two cars with the same price as duplicates.
    @Override
    public int compareTo(Vehicle other) {
        int byPrice = Integer.compare(getFinalPrice(), other.getFinalPrice());
        if (byPrice != 0) {
            return byPrice;
        }
        int byBrand = brand.compareTo(other.brand);
        return byBrand != 0 ? byBrand : model.compareTo(other.model);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "%s %s | base price: %.0f | age: %d years | km: %.0f | fuel: %s | final price: %d | avg km/year: %.1f",
                brand, model, basePrice, age, km, fuelType, getFinalPrice(), averageKmPerYear());
    }
}
