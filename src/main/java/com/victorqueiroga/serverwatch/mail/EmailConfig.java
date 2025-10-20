package com.victorqueiroga.serverwatch.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "spring.mail")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EmailConfig {

    private String host;
    private int port;
    private String username;
    private String password;
    private String from;

}
