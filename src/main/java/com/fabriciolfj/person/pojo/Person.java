package com.fabriciolfj.person.pojo;

import io.vertx.mutiny.sqlclient.Row;

public record Person(Long id, String name, Integer age, String gender) {

    public static Person from(final Row row) {
        return new Person(row.getLong("id"), row.getString("name"), row.getInteger("age"), row.getString("gender"));
    }
}
