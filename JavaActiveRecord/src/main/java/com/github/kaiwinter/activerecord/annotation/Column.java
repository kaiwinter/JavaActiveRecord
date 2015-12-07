package com.github.kaiwinter.activerecord.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a database column. The variable name will be used for INSERT, UPDATE and DELETE statements (this can be
 * overridden by defining an alias).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Column {
    /**
     * The name of the column in the database. Set this alias if the name is different from the variable name.
     * 
     * @return the name of the column in the database
     */
    String alias() default "";
}
