package com.github.kaiwinter.activerecord.db;

public enum SequenceGenerator {
    /**
     * Use the {@link InternalSequenceGenerator} for the ID column.
     */
    INTERNAL,

    /**
     * Don't set the value of the ID column. The database will set its value. Use this for a SQLite database.
     */
    DATABASE;
}
