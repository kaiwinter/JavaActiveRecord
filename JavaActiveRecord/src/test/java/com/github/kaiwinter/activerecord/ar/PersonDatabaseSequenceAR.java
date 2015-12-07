package com.github.kaiwinter.activerecord.ar;

import com.github.kaiwinter.activerecord.BaseAR;
import com.github.kaiwinter.activerecord.annotation.Column;
import com.github.kaiwinter.activerecord.annotation.Table;
import com.github.kaiwinter.activerecord.db.SequenceGenerator;

@Table(alias = "person_with_db_sequence", sequenceGenerator = SequenceGenerator.DATABASE)
public class PersonDatabaseSequenceAR extends BaseAR {

    @Column
    private String name;
    @Column
    private String surname;

    public PersonDatabaseSequenceAR() {
        // empty constructor necessary
    }

    public PersonDatabaseSequenceAR(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname
     *            the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }
}
