package com.fabriciolfj.person.repository;

import com.fabriciolfj.person.pojo.Person;
import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import net.datafaker.Faker;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PersonRepositoryAsyncAwait {

    @Inject
    @ConfigProperty(name = "myapp.schema.create", defaultValue = "true")
    boolean schemaCreate;
    @Inject
    PgPool pgPool;
    @Inject
    Logger logger;

    void config(@Observes StartupEvent ev) {
        if (schemaCreate) {
            initDb();
        }
    }

    public Long save(final Person person) {
        return pgPool
                .preparedQuery("INSERT INTO person(name, age, gender) VALUES ($1, $2, $3) RETURNING id")
                .executeAndAwait()
                .iterator().next().getLong("id");
    }

    public List<Person> findAll() {
        logger.info("FindAll() " + Thread.currentThread());
        final RowSet<Row> rows = pgPool.preparedQuery("Select id, name, age, gender from Person")
                .executeAndAwait();

        return iterateAndCreate(rows);
    }

    public List<Person> findByName(final String name) {
        final RowSet<Row> rows = pgPool.preparedQuery("Select id, name, age, gender from Person where name = $1")
                .executeAndAwait(Tuple.of(name));

        return iterateAndCreate(rows);
    }

    public List<Person> findByAgeGreaterThan(final int age) {
        final RowSet<Row> rows = pgPool.preparedQuery("Select id, name, age, gender from Person where age > $1")
                .executeAndAwait(Tuple.of(age));

        return iterateAndCreate(rows);
    }

    public Person findById(final Long id) {
        final RowSet<Row> rows = pgPool
                .preparedQuery("Select id, name, age, gender from Person where id = $1")
                .executeAndAwait(Tuple.of(id));

        var result = iterateAndCreate(rows);

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }

    private List<Person> iterateAndCreate(final RowSet<Row> rows) {
        final List<Person> persons = new ArrayList<>();
        for (Row row: rows) {
            persons.add(Person.from(row));
        }

        return persons;
    }

    private void initDb() {
        List<Tuple> persons = new ArrayList<>(1000);
        Faker faker = new Faker();
        for (int i = 0; i < 1000; i++) {
            String name = faker.name().fullName();
            String gender = faker.gender().binaryTypes().toUpperCase();
            int age = faker.number().numberBetween(18, 65);
            int externalId = faker.number().numberBetween(100000, 999999);
            persons.add(Tuple.of(name, age, gender, externalId));
        }

        pgPool.query("DROP TABLE IF EXISTS person").execute()
                .flatMap(r -> pgPool.query("""
                  create table person (
                    id serial primary key,
                    name varchar(255),
                    gender varchar(255),
                    age int,
                    external_id int
                  )
                  """).execute())
                .flatMap(r -> pgPool
                        .preparedQuery("insert into person(name, age, gender, external_id) values($1, $2, $3, $4)")
                        .executeBatch(persons))
                .await().indefinitely();
    }
}
