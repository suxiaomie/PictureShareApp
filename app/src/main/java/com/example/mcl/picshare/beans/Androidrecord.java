package com.example.mcl.picshare.beans;

import java.io.Serializable;

public class Androidrecord implements Serializable {
    private Integer id;
    private String name;
    private Integer count;
    private Integer fromId;

    public Androidrecord() {
    }

    public Androidrecord(String name, Integer count, Integer fromId) {
        this.name = name;
        this.count = count;
        this.fromId = fromId;
    }


    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getFromId() {
        return this.fromId;
    }

    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }
}
