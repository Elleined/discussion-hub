package com.forum.application.config;

import com.forum.application.service.UserService;
import com.sun.security.auth.UserPrincipal;
import jakarta.persistence.Column;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserHandshakeHandler extends DefaultHandshakeHandler {

    private final HttpSession session;
    private final UserService userService;

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String email = (String) session.getAttribute("email");
        String subscriberId = String.valueOf(userService.getIdByEmail(email));
        log.debug("User with subscriber id of {} connected to the website", subscriberId);
        return new UserPrincipal(subscriberId);
    }
}
