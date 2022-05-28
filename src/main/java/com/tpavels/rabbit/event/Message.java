package com.tpavels.rabbit.event;

import java.time.LocalDateTime;

public record Message (
        String id,
        LocalDateTime createdDate
){}
