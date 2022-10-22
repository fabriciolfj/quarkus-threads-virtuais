# Uso de threads virtuais em api rest

- com o quarkus, apenas precisamos anotar o método participante do nosso controller:
```
    @GET
    @Path("/{id}")
    @RunOnVirtualThread
    public Person getPersonById(@PathParam("id") final Long id) {
        logger.info("Find by id " + id + ",thread " + Thread.currentThread());
        return repository.findById(id);
    }
```

- para efetuar o teste de carga, execute o arquivo k6.sh