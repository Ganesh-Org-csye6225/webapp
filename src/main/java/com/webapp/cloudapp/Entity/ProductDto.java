package com.webapp.cloudapp.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ProductDto {

    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("manufacturer")
    private String manufacturer;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("date_added")
    private String date_added;

    @JsonProperty("date_last_updated")
    private String date_last_updated;

    @JsonProperty("owner_user_id")
    private int owner_user_id;

    public ProductDto(int id, String name, String description, String sku, String manufacturer, int quantity,
            String date_added, String date_last_updated, int owner_user_id) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.manufacturer = manufacturer;
        this.quantity = quantity;
        this.date_added = date_added;
        this.date_last_updated = date_last_updated;
        this.owner_user_id = owner_user_id;
    }

}
