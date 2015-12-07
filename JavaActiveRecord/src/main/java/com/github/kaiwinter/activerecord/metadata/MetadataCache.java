package com.github.kaiwinter.activerecord.metadata;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.activerecord.BaseAR;
import com.github.kaiwinter.activerecord.annotation.Column;
import com.github.kaiwinter.activerecord.annotation.Table;
import com.github.kaiwinter.activerecord.metadata.TableMetadata.ColumnWithAlias;

public final class MetadataCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataCache.class.getSimpleName());

    /** The name of the ID column in all database tables. */
    private static final String ID_COLUMN_NAME = "id";

    /** Cache reflection information. */
    private static Map<Class<?>, TableMetadata> tables = new HashMap<>();

    /**
     * If eager initialization is set to <code>true</code> the classpath is scanned for any sub-classes of
     * {@link BaseAR}. All found Active Records are evaluated and the metadata is added to the cache. If eager
     * initialization is set to <code>false</code> the metadata for an Active Records gets build and cached on first
     * access by {@link #getTableMetadata(Class)}.
     */
    private static boolean EAGER_INITIALIZATION = true;

    static {
        if (EAGER_INITIALIZATION) {
            // Scan classpath for @Table annotated classes and store metadata in a static Map.
            Set<Class<? extends BaseAR>> subtypes = new Reflections().getSubTypesOf(BaseAR.class);
            for (Class<? extends BaseAR> clazz : subtypes) {
                if (!clazz.isAnnotationPresent(Table.class)) {
                    // Might be ExtendedBaseAR which doesn't have Table annotation
                    continue;
                }
                TableMetadata tableMetadata = cacheMetadataForActiveRecord(clazz);
                tables.put(clazz, tableMetadata);
            }
        }
    }

    private static TableMetadata cacheMetadataForActiveRecord(Class<? extends BaseAR> clazz) {
        LOGGER.debug("Building Metadata for AR '{}'", clazz.getSimpleName());
        TableMetadata metadata = new TableMetadata();
        metadata.tableAnnotation = clazz.getDeclaredAnnotation(Table.class);
        metadata.columnAnnotatedFields = getColumnFields(clazz);

        String tableName = getTableName(clazz, metadata.tableAnnotation);
        String commaSeparatedFields = getCommaSeparatedColumns(metadata.columnAnnotatedFields);

        LOGGER.debug("... table name: '{}', columns: {}", tableName, metadata.columnAnnotatedFields);

        metadata.selectQuery = "SELECT " + commaSeparatedFields //
                + " FROM " + tableName //
                + " WHERE " + ID_COLUMN_NAME + "=?";

        metadata.selectAllQuery = "SELECT " + commaSeparatedFields + ", id FROM " + tableName;

        String commaSeparatedKeyValuePairs = getCommaSeparatedKeyValuePairs(metadata.columnAnnotatedFields);
        metadata.updateQuery = "UPDATE " + tableName //
                + " SET " + commaSeparatedKeyValuePairs //
                + " WHERE " + ID_COLUMN_NAME + "=?";

        String questionMarkList = getQuestionMarkList(metadata.columnAnnotatedFields);
        metadata.insertQueryInternalSequence = "INSERT INTO " + tableName //
                + " (" + commaSeparatedFields + ", " + ID_COLUMN_NAME //
                + ") VALUES (" + questionMarkList + ", ?)";

        metadata.insertQueryDatabaseSequence = "INSERT INTO " + tableName //
                + " (" + commaSeparatedFields //
                + ") VALUES (" + questionMarkList + ")";

        metadata.deleteQuery = "DELETE FROM " + tableName + " WHERE " + ID_COLUMN_NAME + "=?";

        return metadata;
    }

    /**
     * Returns the {@link Field}s of the passed <code>clazz</code> which are annotated by {@link Column}.
     * 
     * @param clazz
     *            the class to check
     * @return {@link Column}-annotated {@link Field}s as {@link List}
     */
    private static ColumnWithAlias[] getColumnFields(Class<? extends BaseAR> clazz) {
        ColumnWithAlias[] columnFields = Arrays.stream(clazz.getDeclaredFields()) //
                .filter(field -> field.getDeclaredAnnotation(Column.class) != null) //
                .map(ColumnWithAlias::create) //
                .toArray(ColumnWithAlias[]::new);

        return columnFields;
    }

    /**
     * @return comma separated column names
     */
    public static String getCommaSeparatedColumns(ColumnWithAlias[] columnAnnotatedFields) {
        String commaSeparatedColumns = Arrays.stream(columnAnnotatedFields) //
                .map(columnWithAlias -> columnWithAlias.columnName) //
                .collect(Collectors.joining(", "));
        return commaSeparatedColumns;
    }

    /**
     * @return comma separated question marks, one for each column
     */
    private static String getQuestionMarkList(ColumnWithAlias[] columnAnnotatedFields) {
        String val = "";
        for (int i = 0; i < columnAnnotatedFields.length; i++) {
            if (val.length() > 0) {
                val += ", ";
            }
            val += "?";
        }
        return val;
    }

    /**
     * @return comma separated "key=?" pairs.
     */
    private static String getCommaSeparatedKeyValuePairs(ColumnWithAlias[] columnAnnotatedFields) {
        String commaSeparatedKeyValuePairs = Arrays.stream(columnAnnotatedFields) //
                .map(columnWithAlias -> columnWithAlias.columnName) //
                .collect(Collectors.joining("=?, ", "", "=?"));
        return commaSeparatedKeyValuePairs;
    }

    /**
     * Returns the table name for this Active Records. Either the alias is returned (if set) or the simple name of the
     * class.
     * 
     * @param clazz
     *            Class to get the SQL table name for
     * @return The name of the table to use in an SQL query.
     */
    public static String getTableName(Class<? extends BaseAR> clazz) {
        Table tableAnnotation = tables.get(clazz).tableAnnotation;
        return getTableName(clazz, tableAnnotation);
    }

    /**
     * Returns the table name for this Active Records. Either the alias is returned (if set) or the simple name of the
     * class.
     * 
     * @param clazz
     *            Class to get the SQL table name for
     * @return The name of the table to use in an SQL query.
     */
    public static String getTableName(Class<? extends BaseAR> clazz, Table tableAnnotation) {
        String alias = tableAnnotation.alias();
        if (alias.isEmpty()) {
            return clazz.getSimpleName();
        }
        return alias;
    }

    /**
     * Returns the metadata for the passed Active Record class.
     * 
     * @param clazz
     *            the Active Record class
     * @return the metadata
     */
    public static TableMetadata getTableMetadata(Class<? extends BaseAR> clazz) {
        TableMetadata tableMetadata = tables.get(clazz);
        if (tableMetadata == null) {
            tableMetadata = cacheMetadataForActiveRecord(clazz);
            tables.put(clazz, tableMetadata);
        }

        return tableMetadata;
    }
}
