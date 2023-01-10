package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Transactional
    @Modifying
    @Query("update User u set u.name = ?1 where u.id = ?2")
    void updateUserName(String name, Integer id);

    @Transactional
    @Modifying
    @Query("update User u set u.email = ?1 where u.id = ?2")
    void updateUserEmail(String email, Integer id);

    @Query("select distinct u from User u where u.id in :currentIds")
    Collection<User> findByUserIds(@Param("currentIds") Collection<Integer> userIds);
}
