package client.Control;

import client.Entity.User;

import java.util.List;

/**
 * The interface User list listener.
 */
public interface UserListListener {
    /**
     * On user list updated.
     *
     * @param userList the user list
     */
    void onUserListUpdated(List<User> userList);
}
