package com.example.usercrud.web;

import com.example.usercrud.domain.User;
import com.example.usercrud.repo.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper om;
  @Autowired UserRepository repo;

  @BeforeEach
  void clean() { repo.deleteAll(); }

  @Test
  void create_get_update_delete_flow() throws Exception {
    var createJson = om.writeValueAsString(Map.of(
        "firstName", "Grace",
        "lastName", "Hopper",
        "email", "grace@example.com"
    ));

    String created = mvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.email").value("grace@example.com"))
        .andReturn().getResponse().getContentAsString();

    UUID id = UUID.fromString(om.readTree(created).get("id").asText());

    mvc.perform(get("/api/users/{id}", id))
       .andExpect(status().isOk())
       .andExpect(jsonPath("$.firstName").value("Grace"));

    var updateJson = om.writeValueAsString(Map.of(
        "firstName", "Grace B.",
        "lastName", "Hopper",
        "email", "grace@example.com"
    ));
    mvc.perform(put("/api/users/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Grace B."));

    mvc.perform(delete("/api/users/{id}", id))
        .andExpect(status().isNoContent());

    mvc.perform(get("/api/users/{id}", id))
        .andExpect(status().isNotFound());
  }

  @Test
  void validation_errors() throws Exception {
    var badJson = om.writeValueAsString(Map.of(
        "firstName", "",
        "lastName", "",
        "email", "not-an-email"
    ));
    mvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(badJson))
       .andExpect(status().isBadRequest())
       .andExpect(jsonPath("$.errors.firstName").exists())
       .andExpect(jsonPath("$.errors.lastName").exists())
       .andExpect(jsonPath("$.errors.email").exists());
  }
}
