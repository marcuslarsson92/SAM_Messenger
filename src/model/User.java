package model;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.*;
import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String icon;

    public User(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public ImageIcon getIcon() {
        return (ImageIcon) createIcon(icon);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Icon createIcon(String iconPath) {
        ImageIcon imageIcon = new ImageIcon(iconPath);

        if (imageIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            return imageIcon;
        } else {
            System.err.println("Error loading image: " + iconPath);
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return name.equals(user.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
