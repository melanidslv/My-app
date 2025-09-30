package com.example.usercrud.service;

import com.example.usercrud.domain.User;
import com.example.usercrud.repo.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
public class UserService {

  private final UserRepository repo;

  public UserService(UserRepository repo) {
    this.repo = repo;
  }

  public User create(User u) {
    u.setId(null);
    try {
      return repo.save(u);
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(CONFLICT, "email already exists");
    }
  }

  @Transactional(readOnly = true)
  public User get(UUID id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user not found"));
  }

  @Transactional(readOnly = true)
  public List<User> list() {
    return repo.findAll();
  }

  public User update(UUID id, User patch) {
    User existing = get(id);
    existing.setFirstName(patch.getFirstName());
    existing.setLastName(patch.getLastName());
    existing.setEmail(patch.getEmail());
    try {
      return repo.save(existing);
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(CONFLICT, "email already exists");
    }
  }

  public void delete(UUID id) {
    if (!repo.existsById(id)) {
      throw new ResponseStatusException(NOT_FOUND, "user not found");
    }
    repo.deleteById(id);
  }
}
