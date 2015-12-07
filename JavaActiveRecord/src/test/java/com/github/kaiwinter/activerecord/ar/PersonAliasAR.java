package com.github.kaiwinter.activerecord.ar;

import com.github.kaiwinter.activerecord.BaseAR;
import com.github.kaiwinter.activerecord.annotation.Column;
import com.github.kaiwinter.activerecord.annotation.Table;
import com.github.kaiwinter.activerecord.db.SequenceGenerator;

@Table(alias = "person", sequenceGenerator = SequenceGenerator.INTERNAL)
public class PersonAliasAR extends BaseAR {

    @Column(alias = "name")
    private String nameValue;

    @Column(alias = "surname")
    private String surnameValue;

    private String unattachedField;

    public PersonAliasAR() {
        // empty constructor necessary
    }

    public PersonAliasAR(String nameValue, String surnameValue) {
        this.nameValue = nameValue;
        this.surnameValue = surnameValue;
    }

    /**
     * @return the nameValue
     */
    public String getNameValue() {
        return nameValue;
    }

    /**
     * @return the surnameValue
     */
    public String getSurnameValue() {
        return surnameValue;
    }
}
