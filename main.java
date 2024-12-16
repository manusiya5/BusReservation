package Bus_Ticket_Booking;
import java.util.*;
import java.sql.*;
import java.util.Scanner;

public class BusReservation {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/BusReservation";
    private static final String DB_USER = "root"; // Replace with your MySQL username
    private static final String DB_PASSWORD = "Dharshu@11"; // Replace with your MySQL password

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n=== Bus Ticket Reservation System ===");
                System.out.println("1. View Buses");
                System.out.println("2. Book Tickets");
                System.out.println("3. View Bookings");
                System.out.println("4. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        viewBuses(connection);
                        break;
                    case 2:
                        bookTickets(connection, scanner);
                        break;
                    case 3:
                        viewBookings(connection);
                        break;
                    case 4:
                        System.out.println("Thank you for using the Bus Ticket Reservation System!");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewBuses(Connection connection) throws SQLException {
        String query = "SELECT * FROM buses";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\nAvailable Buses:");
            while (rs.next()) {
                System.out.println("Bus ID: " + rs.getInt("bus_id") +
                        ", Name: " + rs.getString("bus_name") +
                        ", From: " + rs.getString("source") +
                        ", To: " + rs.getString("destination") +
                        ", Seats Available: " + rs.getInt("seats_available"));
            }
        }
    }

    private static void bookTickets(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter your name: ");
        scanner.nextLine(); // Consume newline
        String passengerName = scanner.nextLine();

        System.out.print("Enter Bus ID to book: ");
        int busId = scanner.nextInt();

        System.out.print("Enter number of seats to book: ");
        int seatsToBook = scanner.nextInt();

        String checkSeatsQuery = "SELECT seats_available FROM buses WHERE bus_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSeatsQuery)) {
            pstmt.setInt(1, busId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int availableSeats = rs.getInt("seats_available");
                    if (seatsToBook <= availableSeats) {
                        String updateSeatsQuery = "UPDATE buses SET seats_available = seats_available - ? WHERE bus_id = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateSeatsQuery)) {
                            updateStmt.setInt(1, seatsToBook);
                            updateStmt.setInt(2, busId);
                            updateStmt.executeUpdate();
                        }

                        String bookingQuery = "INSERT INTO bookings (passenger_name, bus_id, seats_booked) VALUES (?, ?, ?)";
                        try (PreparedStatement bookingStmt = connection.prepareStatement(bookingQuery)) {
                            bookingStmt.setString(1, passengerName);
                            bookingStmt.setInt(2, busId);
                            bookingStmt.setInt(3, seatsToBook);
                            bookingStmt.executeUpdate();
                        }

                        System.out.println("Booking successful! Seats booked: " + seatsToBook);
                    } else {
                        System.out.println("Not enough seats available. Try booking fewer seats.");
                    }
                } else {
                    System.out.println("Invalid Bus ID.");
                }
            }
        }
    }

    private static void viewBookings(Connection connection) throws SQLException {
        String query = "SELECT b.booking_id, b.passenger_name, bs.bus_name, b.seats_booked " +
                       "FROM bookings b JOIN buses bs ON b.bus_id = bs.bus_id";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\nBookings:");
            while (rs.next()) {
                System.out.println("Booking ID: " + rs.getInt("booking_id") +
                        ", Passenger: " + rs.getString("passenger_name") +
                        ", Bus: " + rs.getString("bus_name") +
                        ", Seats Booked: " + rs.getInt("seats_booked"));
            }
        }
    }
}
