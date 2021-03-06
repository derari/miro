package de.cthul.miro.sql.migrate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDB {
    
    /*
     * select("street").from(ADDRESSES).where().inCity("Berlin");
     * 
     * insertInto(ADDRESSES, "street", "city").values(
     *      "Street 1", "City",
     *      "Street 2", "City",
     *      new Address("Street 3", "City"));
     * 
     * insertInto(ADDRESSES, "street", "city")
     *      .select("streetName", "town")
     *      .from(LOCATIONS)
     *      .where().inCountry("Germany");
     * 
     * update(ADDRESSES).set("street", "city").valuesById(a1, a2)
     * 
     * update(ADDRESSES)
     *      .set("street = x.streetName",
     *           "city = x.town")
     *      .from(LOCATIONS)
     * 
     * delete().from(ADDRESSES).where().
     *  
     */

    private static Connection c;

    public static synchronized Connection getConnection() {
        try {
            if (c == null || c.isClosed()) {
                String url = "jdbc:hsqldb:mem:test_db";
                String user = "sa";
                String password = "";
                c = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return c;
    }
    
    public static void reset() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("DROP TABLE \"Addresses\" IF EXISTS");
            stmt.execute("DROP TABLE \"People\" IF EXISTS");
            stmt.execute("DROP TABLE \"Friends\" IF EXISTS");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void clear() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("DELETE FROM \"Addresses\"");
            stmt.execute("DELETE FROM \"People\"");
            stmt.execute("DELETE FROM \"Friends\"");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertAddress(int id, String street, String city) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO \"Addresses\"(\"id\",\"street\",\"city\") VALUES (?,?,?)")) {
            ps.setInt(1, id);
            ps.setString(2, street);
            ps.setString(3, city);
            ps.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertPerson(int id, String firstName, String lastName, int addressId) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO \"People\"(\"id\",\"first_name\",\"last_name\",\"address_id\") VALUES (?,?,?,?)")) {
            ps.setInt(1, id);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setInt(4, addressId);
            ps.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertFriend(int id1, int id2) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO \"Friends\"(\"id1\",\"id2\") VALUES (?,?)")) {
            ps.setInt(1, id1);
            ps.setInt(2, id2);
            ps.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void scenario1() {
        clear();
        insertAddress(1, "Street 1", "City 1");
        insertAddress(2, "Street 2", "City 2");
        insertPerson(1, "John", "Doe", 1);
        insertPerson(2, "Jane", "Doe", 2);
        insertPerson(3, "Bob", "Brown", 1);
        insertFriend(1, 2);
    }
}
