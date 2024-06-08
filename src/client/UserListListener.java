package client;

import model.User;

import java.util.List;

public interface UserListListener {
    void onUserListUpdated(List<User> userList);
}
