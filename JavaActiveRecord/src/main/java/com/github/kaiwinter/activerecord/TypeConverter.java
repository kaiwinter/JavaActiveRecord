package com.github.kaiwinter.activerecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reading columns from SQL sometimes needs some adaptation, especially because different SQL drivers behaves different.
 * If a value cannot be set in the Active Record this class tries to convert that type to the actual type of the AR
 * field.
 * <p>
 * Example:<br/>
 * The current SQLite driver (3.8.11.1) returns the smallest possible Java type for an INTEGER data column. Which means
 * either an {@link Integer} or a {@link Long} will be returned. The value in the Active Record is typed as {@link Long}
 * but the SQL driver returns an {@link Integer}. Reflection will fail here, so first the {@link Integer} has to be
 * converted to a {@link Long}.
 * </p>
 */
public final class TypeConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeConverter.class.getSimpleName());

    private TypeConverter() {
        // intentionally
    }
    
    /**
     * Converts the passed <code>object</code> to the passed <code>targetType</code>.
     * 
     * @param object
     *            the object to convert
     * @param targetType
     *            the target type
     * @return the value of <code>object</code> in the <code>targetType</code>
     */
    public static Object convertToType(Object object, Class<?> targetType) {
        LOGGER.debug("Converting {} to {}", object.getClass().getName(), targetType);

        if (targetType == Long.class) {
            if (object instanceof Number) {
                return ((Number) object).longValue();
            }
        } // to be continued...

        throw new IllegalArgumentException("Cannot convert type " + object.getClass().getName() + " to " + targetType);
    }
}
