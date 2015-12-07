package com.github.kaiwinter.activerecord.ar;

import com.github.kaiwinter.activerecord.BaseAR;
import com.github.kaiwinter.activerecord.annotation.Column;
import com.github.kaiwinter.activerecord.annotation.Table;
import com.github.kaiwinter.activerecord.db.SequenceGenerator;

@Table(alias = "mountain", sequenceGenerator = SequenceGenerator.INTERNAL)
public class MountainAR extends BaseAR {

    @Column
    private String name;
    @Column
    private Long height;

    private String unattachedField;

    public MountainAR() {
        // empty constructor necessary
    }

    public MountainAR(String name, Long height) {
        this.name = name;
        this.height = height;
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
     * @return the height
     */
    public Long getHeight() {
        return height;
    }

    /**
     * @param height
     *            the height to set
     */
    public void setHeight(Long height) {
        this.height = height;
    }
}
