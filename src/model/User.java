package model;

import javax.swing.*;

public class User {
    private String name;
    private Icon icon;

    public User(String name, Icon icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public Icon getIcon() {
        return icon;
    }
}
