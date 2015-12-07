## Example
`PersonAR` is an Active Record which extends `BaseAR`. For a running example see the `JavaActiveRecord-Example`-Project or the unit tests of `JavaActiveRecord`.
```java
// Saving an entity
PersonAR person = new PersonAR();
person.setName("First name");
person.setSurname("Last name");
person.save();
System.out.println("Saved Person with ID: " + person.getId());

// Loading by ID
PersonAR findById = PersonAR.findById(PersonAR.class, 1);
System.out.println("Person by ID: " + findById);

// Loading by custom field
Collection<PersonAR> findByField = PersonAR.findAllByField(PersonAR.class, "name", "First name");
System.out.println("Person by Name: " + findByField);

// Loading all
Collection<PersonAR> findAll = PersonAR.findAll(PersonAR.class);
System.out.println("All Persons: " + findAll);
```
