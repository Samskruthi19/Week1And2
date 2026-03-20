import java.util.*;

class Vehicle {
    String licensePlate;
    long entryTime;

    Vehicle(String licensePlate, long entryTime) {
        this.licensePlate = licensePlate;
        this.entryTime = entryTime;
    }
}

enum SpotStatus {
    EMPTY,
    OCCUPIED,
    DELETED
}

class ParkingSpot {
    SpotStatus status = SpotStatus.EMPTY;
    Vehicle vehicle = null;
}

class ParkingLot {

    private final ParkingSpot[] spots;
    private final int capacity;
    private int totalProbes = 0;
    private int totalParked = 0;
    private Map<Integer, Integer> hourlyOccupancy = new HashMap<>();

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        spots = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) {
            spots[i] = new ParkingSpot();
        }
    }

    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    public synchronized String parkVehicle(String licensePlate) {
        long entryTime = System.currentTimeMillis();
        int preferredSpot = hash(licensePlate);
        int probes = 0;

        for (int i = 0; i < capacity; i++) {
            int spotIndex = (preferredSpot + i) % capacity;
            if (spots[spotIndex].status != SpotStatus.OCCUPIED) {
                spots[spotIndex].status = SpotStatus.OCCUPIED;
                spots[spotIndex].vehicle = new Vehicle(licensePlate, entryTime);
                totalProbes += probes;
                totalParked++;
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                hourlyOccupancy.put(hour, hourlyOccupancy.getOrDefault(hour, 0) + 1);
                return "Assigned spot #" + spotIndex + " (" + probes + " probes)";
            } else {
                probes++;
            }
        }
        return "Parking full!";
    }

    public synchronized String exitVehicle(String licensePlate) {
        for (int i = 0; i < capacity; i++) {
            if (spots[i].status == SpotStatus.OCCUPIED &&
                    spots[i].vehicle.licensePlate.equals(licensePlate)) {
                long exitTime = System.currentTimeMillis();
                long durationMillis = exitTime - spots[i].vehicle.entryTime;
                double durationHours = durationMillis / 3600000.0;
                double fee = Math.ceil(durationHours) * 5.0;

                spots[i].status = SpotStatus.DELETED;
                spots[i].vehicle = null;
                totalParked--;

                long hours = durationMillis / 3600000;
                long minutes = (durationMillis / 60000) % 60;
                return "Spot #" + i + " freed, Duration: " + hours + "h " + minutes +
                        "m, Fee: $" + String.format("%.2f", fee);
            }
        }
        return "Vehicle not found!";
    }

    public synchronized String getStatistics() {
        double occupancy = (totalParked * 100.0) / capacity;
        double avgProbes = totalParked == 0 ? 0 : totalProbes * 1.0 / totalParked;

        int peakHour = hourlyOccupancy.entrySet()
                .stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(-1);

        return String.format("Occupancy: %.1f%%, Avg Probes: %.2f, Peak Hour: %s",
                occupancy, avgProbes, peakHour == -1 ? "N/A" : peakHour + ":00");
    }
}

public class week1and2 {
    public static void main(String[] args) throws InterruptedException {
        ParkingLot lot = new ParkingLot(500);

        System.out.println(lot.parkVehicle("ABC-1234"));
        System.out.println(lot.parkVehicle("ABC-1235"));
        System.out.println(lot.parkVehicle("XYZ-9999"));

        Thread.sleep(5000);

        System.out.println(lot.exitVehicle("ABC-1234"));

        System.out.println(lot.getStatistics());
    }
}