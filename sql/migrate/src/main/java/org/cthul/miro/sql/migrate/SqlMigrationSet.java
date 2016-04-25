package org.cthul.miro.sql.migrate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.sql.CreateStatement;
import org.cthul.miro.sql.InsertStatement;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.migrate.MigrationSet;
import org.cthul.miro.migrate.Version;

public class SqlMigrationSet extends MigrationSet<SqlDB> {
    
    private final MiConnection cnn;
    private final QlCode migrationTable;

    public SqlMigrationSet(MiConnection cnn) {
        this(cnn, QlCode.id("MiRO_Migrations"));
    }
    
    public SqlMigrationSet(MiConnection cnn, QlCode migrationTable) {
        this.cnn = cnn;
        this.migrationTable = migrationTable;
    }

    @Override
    protected List<Version> readInstalledVersions() throws MiException {
        SelectQuery selectVersion = SelectQuery.create(cnn);
        selectVersion.select().id("version")
                .from().ql(migrationTable);
        MiResultSet rs;
        try {
            rs = selectVersion.execute();
        } catch (MiException e) {
            CreateStatement createMigrationtable = CreateStatement.create(cnn);
            createMigrationtable
                    .table(migrationTable)
                    .column("version").type("VARCHAR", 255).notNullable().primaryKey()
                    .column("timestamp").type("TIMESTAMP").notNullable();
            try {
                createMigrationtable.execute();
                return Collections.emptyList();
            } catch (MiException e2) {
                e2.addSuppressed(e);
                throw e2;
            }
        }
        Set<Version> result = new TreeSet<>();
        while (rs.next()) {
            result.add(Version.parse(rs.getString(1)));
        }
        return new ArrayList<>(result);
    }

    @Override
    protected SqlDB getModel() {
        return new SqlDB(cnn);
    }

    @Override
    protected void logUp(String version) throws MiException {
        long ts = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        InsertStatement insert = InsertStatement.create(cnn);
        insert.into().ql(migrationTable)
                .values(version, ts);
        insert.execute();
    }

    @Override
    protected void close(SqlDB db) throws MiException {
        db.flush();
    }
}
