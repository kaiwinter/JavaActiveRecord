package com.github.kaiwinter.activerecord;

import static com.github.kaiwinter.activerecord.db.Db.INSTANCE;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.activerecord.annotation.Column;
import com.github.kaiwinter.activerecord.annotation.Table;
import com.github.kaiwinter.activerecord.db.InternalSequenceGenerator;
import com.github.kaiwinter.activerecord.db.SequenceGenerator;
import com.github.kaiwinter.activerecord.metadata.MetadataCache;
import com.github.kaiwinter.activerecord.metadata.TableMetadata;
import com.github.kaiwinter.activerecord.metadata.TableMetadata.ColumnWithAlias;

/**
 * Base class for all Active Records. This class defines an id {@link Column} which is mandatory for all database
 * tables.
 * 
 * <p>
 * Example for INSERTing a new database row. Assume you have a PersonAR which extends {@link BaseAR}:
 * 
 * <pre>
 * PersonAR person = new PersonAR(name, surname);
 * person.save()
 * </pre>
 * 
 * After calling {@link #save()} the <code>person</code> contains the id of the database entity.
 * </p>
 * 
 * <p>
 * Example for SELECTing all persons from the database:
 * 
 * <pre>
 * Collection&lt;Person&gt; persons = PersonAR.findAll(PersonAR.class);
 * </pre>
 * </p>
 */
public class BaseAR {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseAR.class.getSimpleName());

    /** The name of the ID column in all database tables. */
    protected static final String ID_COLUMN_NAME = "id";

    @Column
    private Long id;

    /**
     * Loads all records from the table which is associated with the passed Active Record class.
     * 
     * @param clazz
     *            the Active Record class
     * @return {@link Collection} of all table records
     * @throws ActiveRecordException
     *             when an SQL error or an internal error occurs, see the wrapped cause for details.
     */
    public static <T extends BaseAR> Collection<T> findAll(Class<T> clazz) throws ActiveRecordException {
        String query = MetadataCache.getTableMetadata(clazz).selectAllQuery;
        LOGGER.debug(query);
        Collection<T> records = new ArrayList<>();
        try (PreparedStatement statement = INSTANCE.getConnection().prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                T activeRecord = resultSet2ActiveRecord(clazz, resultSet);
                activeRecord.setId(resultSet.getLong(ID_COLUMN_NAME));
                records.add(activeRecord);
            }

        } catch (SQLException e) {
            throw new ActiveRecordException("Could not query record", e);
        }
        return records;
    }

    /**
     * Loads the record from the table which is associated with the passed Active Record with the passed id.
     * 
     * @param clazz
     *            the Active Record class
     * @param id
     *            the id of the record
     * @return the Active Record or <code>null</code> if no record exists with that id
     * @throws ActiveRecordException
     *             when an SQL error or an internal error occurs, see the wrapped cause for details.
     */
    public static <T extends BaseAR> T findById(Class<T> clazz, long id) throws ActiveRecordException {
        TableMetadata metadata = MetadataCache.getTableMetadata(clazz);
        LOGGER.debug(metadata.selectQuery);
        try (PreparedStatement statement = INSTANCE.getConnection().prepareStatement(metadata.selectQuery)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    T activeRecord = resultSet2ActiveRecord(clazz, resultSet);
                    activeRecord.setId(id);
                    return activeRecord;
                }
            }

        } catch (SQLException e) {
            throw new ActiveRecordException("Could not query all records", e);
        }
        return null;
    }

    /**
     * Loads all records from the table with the given <code>value</code> in the given column.
     * 
     * @param clazz
     *            the Active Record class
     * @param columnName
     *            the name of the database column, there is no sanity check you have to be sure it exists
     * @param value
     *            the value to query the database field
     * @return Collection of found Active Records (empty when no entries match)
     * @throws ActiveRecordException
     *             when an SQL error or an internal error occurs, see the wrapped cause for details.
     */
    public static <T extends BaseAR> Collection<T> findAllByColumn(Class<T> clazz, String columnName, Object value)
            throws ActiveRecordException {
        TableMetadata metadata = MetadataCache.getTableMetadata(clazz);
        String query = metadata.selectAllQuery + " WHERE " + columnName + "=?";
        LOGGER.debug(query);
        Collection<T> records = new ArrayList<>();
        try (PreparedStatement statement = INSTANCE.getConnection().prepareStatement(query)) {
            statement.setObject(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    T activeRecord = resultSet2ActiveRecord(clazz, resultSet);
                    activeRecord.setId(resultSet.getLong(ID_COLUMN_NAME));
                    records.add(activeRecord);
                }
            }

        } catch (SQLException e) {
            throw new ActiveRecordException("Could not query all records by field", e);
        }
        return records;
    }

    protected static <T extends BaseAR> T resultSet2ActiveRecord(Class<T> clazz, ResultSet resultSet)
            throws SQLException, ActiveRecordException {
        try {
            T activeRecord = clazz.newInstance();
            TableMetadata tableMetadata = MetadataCache.getTableMetadata(clazz);
            // Iterate Column-annotated fields and set values
            for (ColumnWithAlias field : tableMetadata.columnAnnotatedFields) {
                Field fieldInArInstance = activeRecord.getClass()
                        .getDeclaredField(field.columnAnnotatedField.getName());
                fieldInArInstance.setAccessible(true);

                // Not supported by sqlite-jdbc-3.8.11
                // Object object = resultSet.getObject(field.columnName, fieldInArInstance.getType());
                Object object = resultSet.getObject(field.columnName);

                boolean assignableFrom = fieldInArInstance.getType().isAssignableFrom(object.getClass());
                if (!assignableFrom) {
                    object = TypeConverter.convertToType(object, fieldInArInstance.getType());
                }
                fieldInArInstance.set(activeRecord, object);
            }
            return activeRecord;
        } catch (InstantiationException e) {
            String message = "Cannot instantiate '" + clazz.getName() + "', is there a default constructor?";
            throw new ActiveRecordException(message, e);
        } catch (IllegalAccessException | NoSuchFieldException | SecurityException | IllegalArgumentException e) {
            throw new ActiveRecordException(e.getMessage(), e);
        }
    }

    /**
     * Saves a new or updated Active Record.
     * 
     * @throws ActiveRecordException
     *             when an insert or update fails, see the wrapped cause for details
     */
    public void save() throws ActiveRecordException {
        try {
            if (id == null) {
                insert();
            } else {
                update();
            }
        } catch (SQLException e) {
            throw new ActiveRecordException("Could not save Active Record", e);
        }
    }

    private void insert() throws ActiveRecordException, SQLException {
        Table declaredAnnotation = MetadataCache.getTableMetadata(getClass()).tableAnnotation;
        SequenceGenerator sequenceGenerator = declaredAnnotation.sequenceGenerator();
        switch (sequenceGenerator) {
            case DATABASE :
                insertWithDatabaseSequence();
                break;
            case INTERNAL :
                insertWithInternalSequence();
                break;
            default :
                throw new IllegalArgumentException("Unknown sequence generator: " + sequenceGenerator);
        }
    }

    /**
     * Inserts the AR to the database. Doesn't write the ID column, relies on the database to set an appropriate value.
     * The driver have to return the generated ID which gets set in the AR.
     */
    private void insertWithDatabaseSequence() throws ActiveRecordException, SQLException {
        TableMetadata metadata = MetadataCache.getTableMetadata(getClass());
        LOGGER.debug(metadata.insertQueryDatabaseSequence);
        try (PreparedStatement statement = INSTANCE.getConnection()
                .prepareStatement(metadata.insertQueryDatabaseSequence, Statement.RETURN_GENERATED_KEYS)) {
            setParameterInStatement(statement);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Insert failed, could not acquire sequence number");
            } else {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Set generated ID in AR
                        id = generatedKeys.getLong(1);
                    }
                }
            }

        }
    }

    /**
     * Inserts the AR to the database. Uses the {@link InternalSequenceGenerator} to fill the ID column.
     */
    private void insertWithInternalSequence() throws ActiveRecordException, SQLException {
        TableMetadata metadata = MetadataCache.getTableMetadata(getClass());
        LOGGER.debug(metadata.insertQueryInternalSequence);
        try (PreparedStatement statement = INSTANCE.getConnection()
                .prepareStatement(metadata.insertQueryInternalSequence)) {
            id = INSTANCE.getNextSequenceNumber(getClass());
            statement.setLong(metadata.columnAnnotatedFields.length + 1, id);
            setParameterInStatement(statement);

            statement.execute();
        }
    }

    private void update() throws ActiveRecordException, SQLException {
        TableMetadata metadata = MetadataCache.getTableMetadata(getClass());
        LOGGER.debug(metadata.updateQuery);
        try (PreparedStatement statement = INSTANCE.getConnection().prepareStatement(metadata.updateQuery)) {
            statement.setLong(metadata.columnAnnotatedFields.length + 1, id);
            setParameterInStatement(statement);

            int count = statement.executeUpdate();
            LOGGER.debug("Updated {} entries", count);
        }
    }

    /**
     * Sets the values of this Active Record in the passed statement. The values are set in a specific order, here the
     * same order is used as for building the statement with the column names.
     * 
     * @param statement
     *            the {@link PreparedStatement} to set values on
     * @throws ActiveRecordException
     *             if an error occurs while reflectively reading data from the AR or setting the value on the statement
     *             fails
     */
    private void setParameterInStatement(PreparedStatement statement) throws ActiveRecordException {
        TableMetadata tableMetadata = MetadataCache.getTableMetadata(getClass());
        int count = 1;
        for (ColumnWithAlias field : tableMetadata.columnAnnotatedFields) {
            // mark as accessible for read
            field.columnAnnotatedField.setAccessible(true);

            // Trust in built-in type conversion
            try {
                statement.setObject(count++, field.columnAnnotatedField.get(this));
            } catch (IllegalArgumentException | IllegalAccessException | SQLException e) {
                throw new ActiveRecordException("Could not set parameter in statement", e);
            }
        }
    }

    /**
     * Deletes this record from the database.
     * 
     * @throws ActiveRecordException
     *             when a delete fails, see the wrapped cause for details
     */
    public void delete() throws ActiveRecordException {
        TableMetadata metadata = MetadataCache.getTableMetadata(getClass());
        LOGGER.debug(metadata.deleteQuery);
        try (PreparedStatement statement = INSTANCE.getConnection().prepareStatement(metadata.deleteQuery)) {
            statement.setLong(1, id);
            int count = statement.executeUpdate();
            LOGGER.debug("Deleted {} entries", count);
        } catch (SQLException e) {
            throw new ActiveRecordException("Could not delete Active Record", e);
        }
    }

    /**
     * This method should be called by the framework only.
     * 
     * @param id
     *            the id to set
     */
    void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
}
