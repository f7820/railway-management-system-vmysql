package com.railway.railway_management_system.repository;

import com.railway.railway_management_system.model.Passenger;
import com.railway.railway_management_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String username);

    boolean existsByUsername(String defaultUsername);
}
