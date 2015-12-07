package com.github.kaiwinter.activerecord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.github.kaiwinter.activerecord.ar.MountainAR;
import com.github.kaiwinter.activerecord.ar.PersonAR;
import com.github.kaiwinter.activerecord.ar.PersonAliasAR;
import com.github.kaiwinter.activerecord.ar.PersonDatabaseSequenceAR;
import com.github.kaiwinter.activerecord.db.DbTestUtil;

public class ArTest {

    @Before
    public void setup() throws ClassNotFoundException, SQLException, IOException, InterruptedException {
        DbTestUtil.setupDb();
    }

    /**
     * Saves a Person and tries to reload it.
     */
    @Test
    public void testInsert() throws ActiveRecordException {
        PersonAR person = new PersonAR("name", "surname");
        person.save();

        person = reload(person);
        assertNotNull(person.getId()); // expected ID cannot be tested as it is not stable over test executions
        assertEquals("name", person.getName());
        assertEquals("surname", person.getSurname());
    }

    /**
     * Saves a Person, reloads and updates it.
     */
    @Test
    public void testUpdate() throws ActiveRecordException {
        PersonAR person = new PersonAR("name", "surname");
        person.save();

        person = reload(person);
        person.setName("new name");
        person.save();

        person = reload(person);
        assertEquals("new name", person.getName());
    }

    /**
     * Saves a Person and deletes it.
     */
    @Test
    public void testDelete() throws ActiveRecordException {
        PersonAR person = new PersonAR("name", "surname");
        person.save();

        person.delete();
        person = reload(person);
        assertNull(person);
    }

    /**
     * Tests the alias definition for a column.
     */
    @Test
    public void testAlias() throws ActiveRecordException {
        PersonAliasAR personAliasAR = new PersonAliasAR("name", "surname");
        personAliasAR.save();

        personAliasAR = PersonAliasAR.findById(PersonAliasAR.class, personAliasAR.getId());
        assertNotNull(personAliasAR.getId());
        assertEquals("name", personAliasAR.getNameValue());
        assertEquals("surname", personAliasAR.getSurnameValue());
    }

    /**
     * Tests loading a Collection of entities.
     */
    @Test
    public void testAll() throws ActiveRecordException {
        new PersonAR("name1", "surname1").save();
        new PersonAR("name2", "surname2").save();
        Collection<PersonAR> persons = PersonAR.findAll(PersonAR.class);

        assertEquals(2, persons.size());
    }

    /**
     * Test if the current sequence numbers are kept by entity.
     * <ul>
     * <li>ID of person1 should be 1</li>
     * <li>ID of person2 should be 2</li>
     * <li>ID of mountain shout be 1</li>
     * </ul>
     */
    @Test
    public void testInternalSequence() throws ActiveRecordException {
        PersonAR person1 = new PersonAR("name1", "surname1");
        person1.save();

        PersonAR person2 = new PersonAR("name1", "surname1");
        person2.save();
        assertTrue(person2.getId() > person1.getId());

        MountainAR mountain = new MountainAR("mountain1", 100L);
        mountain.save();

        assertTrue(mountain.getId() < person2.getId());
    }

    /**
     * Tests if the saved Persons got an ID by the database sequence generator.
     */
    @Test
    public void testDatabaseSequence() throws ActiveRecordException {
        PersonDatabaseSequenceAR person1 = new PersonDatabaseSequenceAR("name1", "surname1");
        person1.save();
        assertNotNull(person1.getId());

        PersonDatabaseSequenceAR person2 = new PersonDatabaseSequenceAR("name2", "surname2");
        person2.save();
        assertNotNull(person2.getId());
    }

    /**
     * Reloads the passed Person by its ID.
     * 
     * @param person
     *            the Person to reload
     * @return the Person
     */
    private PersonAR reload(PersonAR person) throws ActiveRecordException {
        return PersonAR.findById(PersonAR.class, person.getId());
    }

    /**
     * Tests the findAllByField method.
     */
    @Test
    public void testFindByFieldOne() throws ActiveRecordException {
        new MountainAR("mountain 1", 100L).save();
        new MountainAR("mountain 2", 200L).save();
        new MountainAR("mountain 2", 200L).save();

        Collection<MountainAR> records = MountainAR.findAllByColumn(MountainAR.class, "name", "mountain 1");
        assertEquals(1, records.size());

        assertEquals(100L, records.iterator().next().getHeight().longValue());
    }
}
