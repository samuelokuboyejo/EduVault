package com.eduvault.user.repo;

import com.eduvault.user.User;
import com.eduvault.user.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByMatricNumber(String matricNumber);
    Optional<User> findByMatricNumber(String matricNumber);


    @Transactional
    @Modifying
    @Query("update User u set u.password = ?2 where u.email = ?1")
    void updatePassword(String email, String password);

    Optional<User> findByRole(UserRole userRole);

    List<User> findByRoleIn(List<UserRole> roles);

}
