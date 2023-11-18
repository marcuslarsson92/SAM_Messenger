package model;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.*;

public class User {
    private String name;
    private Icon icon;

    public User(String name, String iconPath) {
        this.name = name;
        this.icon = createIcon(iconPath);
    }

    public String getName() {
        return name;
    }

    public Icon getIcon() {
        return icon;
    }

    private Icon createIcon(String iconPath) {
        // Load the image from the specified file path
        ImageIcon imageIcon = new ImageIcon(iconPath);

        // Check if the image was loaded successfully
        if (imageIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            return imageIcon;
        } else {
            // Handle the case where the image couldn't be loaded
            System.err.println("Error loading image: " + iconPath);
            return null;
        }
    }

}
