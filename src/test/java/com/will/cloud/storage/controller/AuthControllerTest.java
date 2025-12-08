package com.will.cloud.storage.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.utility.TestcontainersConfiguration;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql(scripts = "/sql/create-test-user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired SecurityContextLogoutHandler logoutHandler;

    @Test
    void signUp() throws Exception {
        this.mockMvc
                .perform(
                        MockMvcRequestBuilders.post("/api/v1/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                    {
                                        "username": "Will",
                                        "password": "Salas"
                                    }
                                """))
                .andExpectAll(
                        status().isCreated(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content()
                                .json(
                                        """
                                    {
                                        "username": "Will"
                                    }
                                """));
    }

    @Test
    void signIn() throws Exception {
        this.mockMvc
                .perform(
                        MockMvcRequestBuilders.post("/api/v1/auth/sign-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                    {
                                        "username": "Logged In",
                                        "password": "Salas"
                                    }
                                """))
                .andExpectAll(
                        status().isOk(),
                        header().exists("Set-Cookie"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content()
                                .json(
                                        """
                                    {
                                        "username": "Logged In"
                                    }
                                """));
    }
}
