package com.github.kaiwinter.activerecord.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.kaiwinter.activerecord.db.SequenceGenerator;

/**
 * Annotates a database table. The class name will be used as database table name (this can be overridden by defining an
 * alias).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Table {
    /**
     * The name of the table in the database. Set this alias if the name is different from the class name.
     * 
     * @return the name of the table in the database
     */
    String alias() default "";

    /**
     * Every database table which is represented by an Active Record must have an ID column. If this ID column is an
     * auto increment column use {@link SequenceGenerator#DATABASE}. Otherwise the sequence can be handled by this
     * framework using {@link SequenceGenerator#INTERNAL}.
     * 
     * @return the {@link SequenceGenerator} to get the ID for new records
     */
    SequenceGenerator sequenceGenerator();
}
