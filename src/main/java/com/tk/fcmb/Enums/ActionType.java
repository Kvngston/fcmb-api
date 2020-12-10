package com.tk.fcmb.Enums;

public enum ActionType {

    ACTIVATE("Activation"),
    DEACTIVATE("DeActivation");


    private String name;

    ActionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
