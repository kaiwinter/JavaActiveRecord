package com.github.kaiwinter.activerecord.metadata;

import java.lang.reflect.Field;

import com.github.kaiwinter.activerecord.annotation.Column;
import com.github.kaiwinter.activerecord.annotation.Table;

/**
 * TableMetadata for an Active Record. Holds the {@link Table} annotation and all {@link Column} annotated fields of the
 * class. Also SQL queries are cached here.
 */
public final class TableMetadata {

    /** The annotation of the Active Record. */
    public Table tableAnnotation;

    /** The {@link Column}s of the Active Record. */
    public ColumnWithAlias[] columnAnnotatedFields;

    public String selectQuery;
    public String selectAllQuery;
    public String updateQuery;
    public String insertQueryInternalSequence;
    public String insertQueryDatabaseSequence;
    public String deleteQuery;

    /**
     * One {@link Column} with it's database column name (either field name or alias defined by {@link Column}).
     */
    public static final class ColumnWithAlias {

        /** The {@link Field} annotated by {@link Column}. */
        public Field columnAnnotatedField;

        /** Field name or column alias. */
        public String columnName;

        /**
         * Constructs a new {@link ColumnWithAlias} from a {@link Field}.
         * 
         * @param field
         *            the {@link Field}
         * @return an initialized {@link ColumnWithAlias}
         */
        public static final ColumnWithAlias create(Field field) {
            ColumnWithAlias columnWithAlias = new ColumnWithAlias();
            columnWithAlias.columnAnnotatedField = field;
            columnWithAlias.columnName = getColumnName(field);
            return columnWithAlias;
        }

        /**
         * Returns the name of a DB column. Either the field name is returned or an alias if it is set in the
         * {@link Column} annotation.
         * 
         * @param field
         *            The {@link Column}-annotated {@link Field}
         * @return the name of the DB column
         */
        private static String getColumnName(Field field) {
            Column declaredAnnotation = field.getDeclaredAnnotation(Column.class);
            String alias = declaredAnnotation.alias();
            if (alias.isEmpty()) {
                return field.getName();
            }
            return alias;
        }

        @Override
        public String toString() {
            return columnName;
        }
    }
}