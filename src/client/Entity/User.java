package client.Entity;

import javax.swing.ImageIcon;
import java.io.Serializable;

/**
 * The type User.
 */
public class User implements Serializable {
    private String name;
    private String icon;
    private boolean online;

    /**
     * Instantiates a new User.
     *
     * @param name the name
     * @param icon the icon
     */
    public User(String name, String icon) {
        this.name = name;
        this.icon = icon;
        this.online = false;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets icon.
     *
     * @return the icon
     */
    public ImageIcon getIcon() {
        if (icon == null || icon.isEmpty()) {
            // Använd en standardikon om ingen ikon är angiven
            return createDefaultIcon();
        } else {
            return (ImageIcon) createIcon(icon);
        }
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets icon.
     *
     * @param icon the icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    private ImageIcon createIcon(String path) {
        java.net.URL imgURL = getClass().getClassLoader().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Error loading image: " + path);
            return createDefaultIcon(); // Återgå till standardikon vid fel
        }
    }

    private ImageIcon createDefaultIcon() {
        // Skapa eller returnera en standardikon
        java.net.URL imgURL = getClass().getClassLoader().getResource("icons/default.png");
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Default icon not found!");
            return null; // Hantera detta fall på lämpligt sätt
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
