package de.cthul.migrate.sql;

import org.cthul.miro.migrate.at.Up;
import org.cthul.miro.migrate.sql.SqlDB;

public class TestMigrations {
    
    public TestMigrations() {
    }
    
    public void up_2016_01_11_a_Create_Addresses(SqlDB db) {
        db.defaultSchema()
            .createTable("Addresses")
                .column("id").type("INTEGER").autoGenerate().primaryKey()
                .column("street").type("VARCHAR", 64)
                .column("city").type("VARCHAR", 64);
    }
    
    public void up_2016_01_11_b_Create_People(SqlDB db) {
        db.defaultSchema()
            .createTable("People")
                .column("id").type("INTEGER GENERATED BY DEFAULT AS IDENTITY").primaryKey()
                .column("first_name").type("VARCHAR", 64)
                .column("last_name").type("VARCHAR", 64)
                .column("address_id").type("INTEGER");
    }
    
    @Up("2016_01_11_c")
    public void create_Friends(SqlDB db) {
        db.defaultSchema()
            .createTable("Friends")
                .column("id1").type("INTEGER")
                .column("id2").type("INTEGER");
    }
}
