package com.fabriciolfj.person.controller;

import com.fabriciolfj.person.pojo.Person;
import com.fabriciolfj.person.pojo.PersonId;
import com.fabriciolfj.person.repository.PersonRepositoryAsyncAwait;
import io.smallrye.common.annotation.RunOnVirtualThread;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

@Path("/persons")
public class PersonController {

    @Inject
    private PersonRepositoryAsyncAwait repository;
    @Inject
    Logger logger;

    @POST
    @RunOnVirtualThread
    public PersonId addPerson(final Person person) {
        final Long result = repository.save(person);

        return new PersonId(result);
    }

    @GET
    @RunOnVirtualThread
    public List<Person> findAll() {
        return repository.findAll();
    }

    @GET
    @Path("/name/{name}")
    @RunOnVirtualThread
    public List<Person> getPersonsByName(@PathParam("name") final String name) {
        return repository.findByName(name);
    }

    @GET
    @Path("/age/{age}")
    @RunOnVirtualThread
    public List<Person> getPersonAgeGreather(@PathParam("age") final Integer age) {
        return repository.findByAgeGreaterThan(age);
    }

    @GET
    @Path("/{id}")
    @RunOnVirtualThread
    public Person getPersonById(@PathParam("id") final Long id) {
        logger.info("Find by id " + id + ",thread " + Thread.currentThread());
        return repository.findById(id);
    }
}
