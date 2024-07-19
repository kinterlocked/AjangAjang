package com.ajangajang.backend.board.model.service;

import com.ajangajang.backend.board.model.dto.*;
import com.ajangajang.backend.board.model.entity.Board;
import com.ajangajang.backend.board.model.entity.BoardMedia;
import com.ajangajang.backend.board.model.entity.MediaType;
import com.ajangajang.backend.board.model.repository.BoardMediaRepository;
import com.ajangajang.backend.board.model.repository.BoardRepository;
import com.ajangajang.backend.user.model.dto.UserProfileDto;
import com.ajangajang.backend.user.model.entity.User;
import com.ajangajang.backend.user.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardMediaRepository boardMediaRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    public Long save(CreateBoardDto dto, List<MultipartFile> files) {
        Board board = new Board(dto.getTitle(), dto.getPrice(), dto.getContent(),
                                dto.getTag(), dto.getDeliveryType(), dto.getStatus());
        User writer = userRepository.findById(dto.getWriterId()).orElse(null);

        setBoardMedia(files, board); // file upload, media save
        board.setWriter(writer);
        return boardRepository.save(board).getId();
    }

    public BoardDto findById(Long id) {
        Board findBoard = boardRepository.findById(id).orElse(null);

        List<BoardMediaDto> mediaDtoList = findBoard.getMediaList().stream()
                .map(media -> new BoardMediaDto(media.getId(), media.getMediaType(), media.getMediaUrl(),
                        media.getCreatedAt()))
                .collect(Collectors.toList());

        User findWriter = findBoard.getWriter();
        UserProfileDto userProfileDto = new UserProfileDto(findWriter.getId(), findWriter.getNickname(), findWriter.getProfileImg());

        return new BoardDto(userProfileDto, findBoard.getTitle(), findBoard.getPrice(),
                            findBoard.getContent(), findBoard.getDeliveryType(), findBoard.getTag(),
                            findBoard.getStatus(), mediaDtoList, findBoard.getCreatedAt(),
                            findBoard.getUpdatedAt());
    }

    public List<BoardListDto> findAll() {
        List<Board> boards = boardRepository.findAllWithWriter();
        List<BoardListDto> result = new ArrayList<>();
        for (Board board : boards) {
            User writer = board.getWriter();
            UserProfileDto profile = new UserProfileDto(writer.getId(), writer.getNickname(),
                                                        writer.getProfileImg());
            result.add(new BoardListDto(board.getId(), profile, board.getTitle(), board.getPrice(),
                                        board.getDeliveryType(), board.getTag(), board.getStatus()));
        }
        return result;
    }

    public void update(Long id, UpdateBoardDto updateParam, List<MultipartFile> files) {
        Board findBoard = boardRepository.findById(id).orElse(null);
        // 내용 업데이트
        if (updateParam != null) {
            findBoard.setTitle(updateParam.getTitle());
            findBoard.setPrice(updateParam.getPrice());
            findBoard.setContent(updateParam.getContent());
            findBoard.setTag(updateParam.getTag());
            findBoard.setStatus(updateParam.getStatus());
            findBoard.setDeliveryType(updateParam.getDeliveryType());
        }
        // 새 파일 추가
        setBoardMedia(files, findBoard);
        // 파일 삭제
        deleteFiles(updateParam.getDeleteFileIds());
    }

    public void delete(Long id) {
        List<String> fileUrls = boardRepository.findById(id).orElse(null).getMediaList().stream()
                .map(media -> media.getMediaUrl())
                .collect(Collectors.toList());
        fileService.deleteFiles(fileUrls);
        boardRepository.deleteById(id);
    }

    private void setBoardMedia(List<MultipartFile> files, Board board) {
        if (files != null && !files.isEmpty()) {
            // Url + fileName, mediaType
            Map<String, MediaType> filesInfo = fileService.uploadFile(files);
            for (Map.Entry<String, MediaType> entry : filesInfo.entrySet()) {
                BoardMedia media = new BoardMedia(entry.getValue(), entry.getKey());
                board.addMedia(media);
                boardMediaRepository.save(media);
            }
        }
    }

    private void deleteFiles(List<Long> deleteFileIds) {
        if (!deleteFileIds.isEmpty()) {
            for (Long deleteFileId : deleteFileIds) {
                String deleteFileUrl = boardMediaRepository.findById(deleteFileId).orElse(null).getMediaUrl();
                fileService.delete(deleteFileUrl);
                boardMediaRepository.deleteById(deleteFileId);
            }
        }
    }

}