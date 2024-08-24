package model;

import java.util.ArrayList;
import java.util.List;

public class ContactManager {
    private List<User> contacts;

    public ContactManager() {
        this.contacts = new ArrayList<>();
    }

    public List<User> getContacts() {
        return contacts;
    }

    public void addContact(User user) {
        if (!contacts.contains(user)) {
            contacts.add(user);
        }
    }

    public void removeContact(User user) {
        contacts.remove(user);
    }

    public boolean isContact(User user) {
        return contacts.contains(user);
    }
}
