package org.cthul.miro.migrate;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.migrate.at.MigrationMethodScanner;
import org.cthul.objects.instance.InstanceMap;

/**
 *
 * @param <DB>
 */
public abstract class MigrationSet<DB> {
    
    private final SortedSet<Migration<? super DB>> migSet = new TreeSet<>();
    private final InstanceMap cfg = new InstanceMap();

    protected abstract List<Version> readInstalledVersions() throws MiException;
    
    protected abstract void logUp(String version) throws MiException;
    
    public void add(Iterable<? extends Migration<? super DB>> migrations) {
        migrations.forEach(migSet::add);
    }
    
    public void add(Migration<? super DB>... migrations) {
        add(Arrays.asList(migrations));
    }
    
    public void add(Object migrations) {
        add(new MigrationMethodScanner<DB>(cfg).read(migrations));
    }
    
    public void add(Class<?> migrations) {
        add(new MigrationMethodScanner<DB>(cfg).read(migrations));
    }
    
    public void migrateUpToLatest() throws MiException {
        migrateUpTo(LATEST);
    }
    
    public void migrateUpTo(Version version) throws MiException {
        List<Version> installedList = readInstalledVersions();
        Iterator<Version> installed = installedList.iterator();
        Iterator<Migration<? super DB>> migrations = migSet.iterator();
        while (installed.hasNext()) {
            Version v = installed.next();
            if (!migrations.hasNext()) {
                throw unknownInstalled(v);
            }
            Migration<?> mig = migrations.next();
            int versionCompare = v.compareTo(mig.getVersion());
            if (versionCompare < 0) {
                throw unknownInstalled(v);
            } else if (versionCompare > 0) {
                throw missingVersion(mig.getVersion(), v);
            }
        }
        migrateRemainingUpTo(migrations, version);
    }

    private MiException unknownInstalled(Version version) {
        return new MiException("Unknown installed version: " + version);
    }

    private MiException missingVersion(Version missing, Version found) {
        return new MiException("Missing version " + missing + " before " + found);
    }
    
    protected abstract DB getModel();
    
    protected abstract void close(DB db) throws MiException;

    protected void migrateRemainingUpTo(Iterator<Migration<? super DB>> migrations, Version version) throws MiException {
        DB db = getModel();
        while (migrations.hasNext()) {
            Migration<? super DB> mig = migrations.next();
            if (version != LATEST && version.compareTo(mig.getVersion()) < 0) break;
            mig.up(db);
        }
        close(db);
    }
    
    private static final Version LATEST = new Version("**LATEST**");
}
