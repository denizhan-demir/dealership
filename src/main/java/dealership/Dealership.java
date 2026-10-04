package dealership;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class Dealership {

    private static final String INPUT_FILE = "vehicles.txt";
    private static final String OUTPUT_FILE = "outputDealership.txt";

    private final TreeSet<Vehicle> vehicles = new TreeSet<>();

    public TreeSet<Vehicle> getVehicles() {
        return vehicles;
    }

    // ---------- Reading ----------

    public void loadVehicles(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                addFromLine(line.trim());
            }
        } catch (IOException e) {
            System.out.println("Could not read " + fileName + ": " + e.getMessage());
        }
        if (vehicles.isEmpty()) {
            System.out.println("No vehicles loaded from file, using predefined vehicles.");
            loadPredefinedVehicles();
        }
    }

    private void addFromLine(String line) {
        if (line.isEmpty()) {
            return;
        }
        try {
            vehicles.add(parseVehicle(line.split("#")));
        } catch (RuntimeException e) {
            System.out.println("Skipping invalid line: " + line);
        }
    }

    // VEHICLE#brand model#basePrice#age#km#fuelType
    // ELECTRIC#brand model#basePrice#age#km#fuelType#batteryCapacity#hasHeatPump(yes/no)
    private Vehicle parseVehicle(String[] parts) {
        String name = parts[1].trim();
        int space = name.indexOf(' ');
        String brand = space < 0 ? name : name.substring(0, space);
        String model = space < 0 ? "" : name.substring(space + 1);
        double basePrice = Double.parseDouble(parts[2].trim());
        int age = Integer.parseInt(parts[3].trim());
        double km = Double.parseDouble(parts[4].trim());

        String type = parts[0].trim();
        if (type.equals("VEHICLE")) {
            Vehicle.FuelType fuelType = Vehicle.FuelType.valueOf(parts[5].trim());
            return new Vehicle(brand, model, basePrice, age, km, fuelType);
        }
        if (type.equals("ELECTRIC")) {
            int batteryCapacity = Integer.parseInt(parts[6].trim());
            boolean hasHeatPump = parts[7].trim().equalsIgnoreCase("yes");
            return new ElectricVehicle(brand, model, basePrice, age, km, batteryCapacity, hasHeatPump);
        }
        throw new IllegalArgumentException("Unknown vehicle type: " + type);
    }

    private void loadPredefinedVehicles() {
        vehicles.add(new Vehicle("Toyota", "Corolla", 5_500_000, 4, 75_000, Vehicle.FuelType.HYBRID));
        vehicles.add(new Vehicle("Volkswagen", "Golf", 4_200_000, 6, 120_000, Vehicle.FuelType.DIESEL));
        vehicles.add(new Vehicle("Ford", "Focus", 3_100_000, 8, 145_000, Vehicle.FuelType.PETROL));
        vehicles.add(new Vehicle("BMW", "320d", 7_800_000, 3, 60_000, Vehicle.FuelType.DIESEL));
        vehicles.add(new Vehicle("Opel", "Astra", 2_200_000, 11, 190_000, Vehicle.FuelType.PETROL));
        vehicles.add(new ElectricVehicle("Nissan", "Leaf", 4_800_000, 5, 65_000, 40, false));
        vehicles.add(new ElectricVehicle("Tesla", "Model 3", 11_500_000, 2, 38_000, 75, true));
        vehicles.add(new ElectricVehicle("Renault", "Zoe", 3_900_000, 6, 82_000, 52, true));
        vehicles.add(new ElectricVehicle("Kia", "EV6", 13_200_000, 1, 15_000, 77, true));
        vehicles.add(new ElectricVehicle("VW", "ID.3", 7_100_000, 4, 54_000, 58, false));
    }

    // ---------- Queries ----------

    public double averageKm() {
        double sum = 0;
        for (Vehicle v : vehicles) {
            sum += v.getKm();
        }
        return sum / vehicles.size();
    }

    public Vehicle cheapestVehicle() {
        return vehicles.first(); // the TreeSet is sorted by final price
    }

    public ElectricVehicle topElectricVehicle() {
        for (Vehicle v : vehicles.descendingSet()) { // from the most expensive downwards
            if (v instanceof ElectricVehicle) {
                return (ElectricVehicle) v;
            }
        }
        return null;
    }

    public long totalInventoryValue() {
        long total = 0;
        for (Vehicle v : vehicles) {
            total += v.getFinalPrice();
        }
        return total;
    }

    public double averageFinalPrice() {
        return (double) totalInventoryValue() / vehicles.size();
    }

    public List<Vehicle> hybridsNotAboveAveragePrice() {
        double averagePrice = averageFinalPrice();
        List<Vehicle> result = new ArrayList<>();
        for (Vehicle v : vehicles) {
            if (v.getFuelType() == Vehicle.FuelType.HYBRID && v.getFinalPrice() <= averagePrice) {
                result.add(v);
            }
        }
        return result;
    }

    public int youngVehicleCount() {
        int count = 0;
        for (Vehicle v : vehicles) {
            if (v.getAge() < 5) {
                count++;
            }
        }
        return count;
    }

    public Vehicle findVehicle(String brand, String model) {
        for (Vehicle v : vehicles) {
            if (v.getBrand().equals(brand) && v.getModel().equals(model)) {
                return v;
            }
        }
        return null;
    }

    // The price decides the position in the TreeSet, so the vehicle is removed
    // before the discount and added back afterwards to keep the set correctly sorted.
    public void applyDiscount(Vehicle vehicle, int percentage) {
        vehicles.remove(vehicle);
        vehicle.applyDiscount(percentage);
        vehicles.add(vehicle);
    }

    // ---------- Output ----------

    public void printReport(PrintWriter out) {
        print(out, "1. Fleet average mileage: " + format(averageKm()) + " km");

        print(out, "2. Cheapest vehicle: " + cheapestVehicle());

        ElectricVehicle top = topElectricVehicle();
        if (top == null) {
            print(out, "3. Top electric vehicle: none");
        } else {
            print(out, "3. Top electric vehicle: " + top.getBrand() + " " + top.getModel()
                    + " | battery: " + top.getBatteryCapacity() + " kWh"
                    + " | avg km/year: " + format(top.averageKmPerYear()));
        }

        print(out, "4. Total fleet inventory value: " + totalInventoryValue());

        print(out, "5. Hybrid vehicles with a final price not above the average (" + format(averageFinalPrice()) + "):");
        for (Vehicle v : hybridsNotAboveAveragePrice()) {
            print(out, "   " + v);
        }

        print(out, "6. Vehicles younger than 5 years: " + youngVehicleCount());

        Vehicle selected = findVehicle("Toyota", "Corolla");
        if (selected == null) {
            selected = cheapestVehicle();
        }
        int priceBefore = selected.getFinalPrice();
        applyDiscount(selected, 10);
        print(out, "7. Discount simulation (10%) on " + selected.getBrand() + " " + selected.getModel()
                + ": " + priceBefore + " -> " + selected.getFinalPrice());
    }

    private static void print(PrintWriter out, String text) {
        System.out.println(text);
        out.println(text);
    }

    private static String format(double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    public static void main(String[] args) {
        Dealership dealership = new Dealership();
        dealership.loadVehicles(INPUT_FILE);
        try (PrintWriter out = new PrintWriter(new FileWriter(OUTPUT_FILE))) {
            dealership.printReport(out);
        } catch (IOException e) {
            System.out.println("Could not write " + OUTPUT_FILE + ": " + e.getMessage());
        }
    }
}
