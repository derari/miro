package org.cthul.miro.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDB {
    
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

    public static void clear() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("DELETE FROM Addresses");
            stmt.execute("DELETE FROM People");
            stmt.execute("DELETE FROM Friends");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertAddress(int id, String street, String city) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO Addresses(id,street,city) VALUES (?,?,?)")) {
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
                "INSERT INTO People(id,first_name,last_name,address_id) VALUES (?,?,?,?)")) {
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
                "INSERT INTO Friends(id1,id2) VALUES (?,?)")) {
            ps.setInt(1, id1);
            ps.setInt(2, id2);
            ps.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("DROP TABLE People IF EXISTS");
            stmt.execute("DROP TABLE Friends IF EXISTS");
            stmt.execute("DROP TABLE Addresses IF EXISTS");
            stmt.execute(
                    "CREATE TABLE Addresses ("
                    + "id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,"
                    + "street VARCHAR(64),"
                    + "city VARCHAR(64)"
                    + ")");
            stmt.execute(
                    "CREATE TABLE People ("
                    + "id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,"
                    + "first_name VARCHAR(64),"
                    + "last_name VARCHAR(64),"
                    + "address_id INTEGER"
                    + ")");
            stmt.execute(
                    "CREATE TABLE Friends ("
                    + "id1 INTEGER,"
                    + "id2 INTEGER"
                    + ")");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }    
}