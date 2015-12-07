package com.github.kaiwinter.activerecord;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import com.github.kaiwinter.activerecord.ar.PersonAR;
import com.github.kaiwinter.activerecord.db.SetupDbUtil;

/**
 * Saves a Person and reloads it by the ID, the name and as list of all saved entities.
 */
public final class Main {

    public static void main(String[] args)
            throws ActiveRecordException, ClassNotFoundException, SQLException, IOException {

        // Create in-memory database
        SetupDbUtil.setupDb();

        // Save entity
        PersonAR person = new PersonAR();
        person.setName("First name");
        person.setSurname("Last name");
        person.save();
        System.out.println("Saved Person with ID: " + person.getId());

        // Load by ID
        PersonAR findById = PersonAR.findById(PersonAR.class, 1);
        System.out.println("Person by ID: " + findById);

        // Load by custom field
        Collection<PersonAR> findByField = PersonAR.findAllByColumn(PersonAR.class, "name", "First name");
        System.out.println("Person by Name: " + findByField);

        // Load all
        Collection<PersonAR> findAll = PersonAR.findAll(PersonAR.class);
        System.out.println("All Persons: " + findAll);
    }
}
