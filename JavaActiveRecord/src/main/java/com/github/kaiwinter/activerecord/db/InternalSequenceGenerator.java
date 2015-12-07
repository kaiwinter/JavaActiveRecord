package com.github.kaiwinter.activerecord.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.github.kaiwinter.activerecord.BaseAR;
import com.github.kaiwinter.activerecord.metadata.MetadataCache;

/**
 * Sequence generator which increments IDs by one starting at the highest existing ID. This assumes that no one else
 * writes new rows to the table. IDs won't be re-used and gaps won't be filled.
 */
public final class InternalSequenceGenerator {

    /**
     * Caches the last used ID for each database table (= AR class).
     */
    private Map<Class<? extends BaseAR>, Long> table2lastSequenceNumber = new HashMap<>();

    private Long initLastSequenceNumber(Class<? extends BaseAR> clazz) throws SQLException {
        String initLastSequence = "SELECT MAX(id) from " + MetadataCache.getTableName(clazz);

        try (PreparedStatement statement = Db.INSTANCE.getConnection().prepareStatement(initLastSequence);
                ResultSet resultSet = statement.executeQuery()) {
            Long lastSequenceNumber;
            if (resultSet.next()) {
                lastSequenceNumber = resultSet.getLong(1);
            } else {
                lastSequenceNumber = 0L;
            }
            return lastSequenceNumber;
        }
    }

    /**
     * Returns the next ID based on the previous given. It is not checked if the ID is not used.
     * 
     * @param clazz
     *            The AR class to get an ID for
     * @return the next ID
     * @throws SQLException
     *             when the current sequence number cannot be loaded from the database
     */
    public synchronized long getNextSequenceNumber(Class<? extends BaseAR> clazz) throws SQLException {
        Long lastSequenceNumber = table2lastSequenceNumber.get(clazz);
        if (lastSequenceNumber == null) {
            lastSequenceNumber = initLastSequenceNumber(clazz);
            table2lastSequenceNumber.put(clazz, ++lastSequenceNumber);
        } else {
            table2lastSequenceNumber.put(clazz, ++lastSequenceNumber);
        }
        return lastSequenceNumber;
    }
}
