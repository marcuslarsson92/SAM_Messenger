package model;
import javax.swing.*;


public class User {
    private String username;
    private Icon profilePic;

    public User (String username, Icon profilePic) {
        this.username = username;
        this.profilePic = profilePic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Icon getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(Icon profilePic) {
        this.profilePic = profilePic;
    }
}
