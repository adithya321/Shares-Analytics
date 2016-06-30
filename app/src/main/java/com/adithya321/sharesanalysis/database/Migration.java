package com.adithya321.sharesanalysis.database;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;

public class Migration implements RealmMigration {

    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
        //No need Migration for now as there is only one version
    }
}
