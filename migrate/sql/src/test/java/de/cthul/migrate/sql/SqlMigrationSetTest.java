package de.cthul.migrate.sql;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.jdbc.JdbcConnection;
import org.cthul.miro.db.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.migrate.MigrationSet;
import org.cthul.miro.migrate.sql.SqlMigrationSet;
import org.junit.Test;

/**
 *
 */
public class SqlMigrationSetTest {
    
    @Test
    public void test_all() throws MiException {
        TestDB.reset();
        Syntax syntax = new AnsiSqlSyntax();
        MiConnection cnn = new JdbcConnection(TestDB::getConnection, syntax);
        MigrationSet<?> migrations = new SqlMigrationSet(cnn);
        migrations.add(TestMigrations.class);
        migrations.migrateUpToLatest();
        TestDB.scenario1();
    }
}
