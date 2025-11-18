package dao;

import dto.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User create(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    List<User> findAllOrderByIdAsc();

    List<User> findAllOrderByUsernameAsc();

    boolean update(User user);

    boolean delete(Long id);
}