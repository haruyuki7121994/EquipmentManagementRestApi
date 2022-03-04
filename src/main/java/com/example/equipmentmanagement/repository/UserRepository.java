package com.example.equipmentmanagement.repository;

import org.springframework.data.domain.Page;
import com.example.equipmentmanagement.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    @Query(value = "select u.* from users u join user_roles ur on ur.user_id = u.id join roles r on r.id = ur.role_id where r.name = :role", nativeQuery = true)
    Page<User> getByRole(@Param("role") String role, Pageable pageable);
    @Query(value = "select u.* from users u join user_roles ur on ur.user_id = u.id join roles r on r.id = ur.role_id where r.name != 'ROLE_GUEST' and u.id = :id", nativeQuery = true)
    Optional<User> findByUsernameAndRolesNotContainGuest(@Param("id") String id);
}
