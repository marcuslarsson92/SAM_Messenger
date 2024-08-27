package client.Control;

import client.Entity.User;

import java.util.List;

public interface UserListListener {
    void onUserListUpdated(List<User> userList);
}
