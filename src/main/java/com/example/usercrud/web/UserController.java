package com.example.usercrud.web;

import com.example.usercrud.domain.User;
import com.example.usercrud.service.UserService;
import com.example.usercrud.web.dto.CreateUserRequest;
import com.example.usercrud.web.dto.UpdateUserRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public User create(@Valid @RequestBody CreateUserRequest req) {
    User u = new User();
    u.setFirstName(req.firstName());
    u.setLastName(req.lastName());
    u.setEmail(req.email());
    return service.create(u);
  }

  @GetMapping("/{id}")
  public User get(@PathVariable UUID id) {
    return service.get(id);
  }

  @GetMapping
  public List<User> list() {
    return service.list();
  }

  @PutMapping("/{id}")
  public User update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest req) {
    User p = new User();
    p.setFirstName(req.firstName());
    p.setLastName(req.lastName());
    p.setEmail(req.email());
    return service.update(id, p);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
