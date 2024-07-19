package com.ajangajang.backend.board.model.dto;

import com.ajangajang.backend.board.model.entity.DeliveryType;
import com.ajangajang.backend.board.model.entity.Status;
import com.ajangajang.backend.user.model.dto.UserProfileDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class BoardDto {

    private UserProfileDto writer;

    private String title;
    private Integer price;
    private String content;
    private DeliveryType deliveryType;
    private String tag;
    private Status status;
    private List<BoardMediaDto> mediaList = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}