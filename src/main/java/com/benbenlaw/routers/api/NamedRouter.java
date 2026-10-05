package com.benbenlaw.routers.api;

// A router the player can name from the Router Manager, so the chart reads "Smeltery feed" instead of coordinates.
public interface NamedRouter {
    int MAX_NAME_LENGTH = 32;

    String getRouterName();

    void setRouterName(String name);
}
