package com.officedubac.project.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Mansour DIOUF aka Mike Delta",
                        email = "msdiouf93@gmail.com",
                        url = "https://www.linkedin.com/in/mike-delta007/"
                ),
                description = "Documentation des APIs de la plateforme Portail BAC",
                title = "Catalogue REST API",
                version = "1.0"
        ),
        servers = {
                @Server(
                        description = "TEST",
                        url = "http://localhost:8080/ob/"
                )
        }
)
public class SwaggerConfig
{

}
