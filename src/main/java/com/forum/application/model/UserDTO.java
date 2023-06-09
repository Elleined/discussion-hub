package com.forum.application.model;

import lombok.Builder;

@Builder
public record UserDTO(String name, String email, String picture) { }
