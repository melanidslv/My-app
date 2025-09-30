package com.example.usercrud.repo;

import com.example.usercrud.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

  @Autowired
  private UserRepository repo;

  @Test
  @DisplayName("save & find by email")
  void saveAndFindByEmail() {
    User u = new User();
    u.setFirstName("Ada");
    u.setLastName("Lovelace");
    u.setEmail("ada@example.com");
    repo.save(u);

    assertThat(repo.findByEmail("ada@example.com")).isPresent();
    assertThat(repo.existsByEmail("ada@example.com")).isTrue();
  }

  @Test
  @DisplayName("unique email constraint")
  void uniqueEmail() {
    User a = new User();
    a.setFirstName("A");
    a.setLastName("A");
    a.setEmail("dupe@example.com");
    repo.saveAndFlush(a);

    User b = new User();
    b.setFirstName("B");
    b.setLastName("B");
    b.setEmail("dupe@example.com");

    assertThatThrownBy(() -> {
      repo.saveAndFlush(b);
    }).isInstanceOf(DataIntegrityViolationException.class);
  }
}
