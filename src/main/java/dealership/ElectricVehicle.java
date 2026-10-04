package dealership;

public class ElectricVehicle extends Vehicle implements ElectricInterface {

    private static final int BATTERY_COST_PER_KWH = 25_000;

    private int batteryCapacity;
    private boolean hasHeatPump;

    public ElectricVehicle(String brand, String model, double basePrice, int age, double km,
                           int batteryCapacity, boolean hasHeatPump) {
        super(brand, model, basePrice, age, km, FuelType.HYBRID);
        this.batteryCapacity = batteryCapacity;
        this.hasHeatPump = hasHeatPump;
    }

    public int getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(int batteryCapacity) { this.batteryCapacity = batteryCapacity; }

    public boolean hasHeatPump() { return hasHeatPump; }
    public void setHasHeatPump(boolean hasHeatPump) { this.hasHeatPump = hasHeatPump; }

    @Override
    public int getFinalPrice() {
        double price = super.getFinalPrice();
        if (batteryCapacity >= 60) {
            price *= 1.10;
        }
        if (hasHeatPump) {
            price *= 1.05;
        }
        return (int) Math.round(price);
    }

    @Override
    public boolean hasSameRangeCategory(Vehicle other) {
        if (!(other instanceof ElectricVehicle)) {
            return false;
        }
        ElectricVehicle otherElectric = (ElectricVehicle) other;
        return Math.abs(batteryCapacity - otherElectric.batteryCapacity) <= 5;
    }

    @Override
    public int batteryCostShare() {
        return batteryCapacity * BATTERY_COST_PER_KWH;
    }

    @Override
    public String toString() {
        return super.toString() + " | battery: " + batteryCapacity + " kWh | heat pump: " + (hasHeatPump ? "yes" : "no");
    }
}
