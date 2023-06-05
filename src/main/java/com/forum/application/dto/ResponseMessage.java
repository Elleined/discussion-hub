package com.forum.application.dto;


import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ResponseMessage (HttpStatus status, String message, LocalDateTime timeStamp) {
}
