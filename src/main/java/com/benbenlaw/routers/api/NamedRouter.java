package com.benbenlaw.routers.api;

public interface NamedRouter {
    int MAX_NAME_LENGTH = 32;

    String getRouterName();

    void setRouterName(String name);

    static String clean(String name) {
        return name.length() > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH) : name;
    }
}
