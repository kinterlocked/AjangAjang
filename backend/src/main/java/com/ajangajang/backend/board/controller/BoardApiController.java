package com.ajangajang.backend.board.controller;

import com.ajangajang.backend.board.model.dto.*;
import com.ajangajang.backend.board.model.entity.Board;
import com.ajangajang.backend.board.model.service.BoardLikeService;
import com.ajangajang.backend.board.model.service.BoardService;
import com.ajangajang.backend.elasticsearch.model.document.BoardDocument;
import com.ajangajang.backend.elasticsearch.model.service.BoardSearchService;
import com.ajangajang.backend.oauth.model.dto.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BoardApiController {

    private final BoardService boardService;
    private final BoardLikeService boardLikeService;
    private final BoardSearchService boardSearchService;

    @PostMapping("/board")
    public ResponseEntity<?> saveBoard(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                       @RequestPart("board") CreateBoardDto createBoardDto,
                                       @RequestPart(value = "media", required = false) List<MultipartFile> files) {
        String username = customOAuth2User.getUsername();
        Board board = boardService.save(username, createBoardDto, files);
        boardSearchService.save(board);
        Long boardId = board.getId();
        return ResponseEntity.ok(Map.of("boardId", boardId));
    }

    @GetMapping("/board/{id}")
    public ResponseEntity<?> getBoard(@PathVariable("id") Long id) {
        BoardDto result = boardService.findById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/board/all")
    public ResponseEntity<?> getAllBoards(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                          @RequestParam String type) {
        String username = customOAuth2User.getUsername();
        List<BoardListDto> result = boardService.findAllInRange(username, type);
        return ResponseEntity.ok(Map.of("data", result));
    }

    @PutMapping("/board/{id}")
    public ResponseEntity<?> updateBoard(@PathVariable("id") Long id,
                            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                            @RequestPart(value = "board", required = false) UpdateBoardDto updateParam,
                            @RequestPart(value = "media", required = false) List<MultipartFile> files) {
        String username = customOAuth2User.getUsername();
        boardService.update(id, username, updateParam, files);
        Board board = boardService.findBoardById(id);
        boardSearchService.save(board);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/board/{id}")
    public ResponseEntity<?> deleteBoard(@PathVariable("id") Long id,
                                         @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        String username = customOAuth2User.getUsername();
        boardService.delete(id, username);
        boardSearchService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/board/search")
    public ResponseEntity<?> searchBoard(@RequestBody SearchBoardDto searchBoardDto) {
        Page<Board> searchResult = boardSearchService.search(searchBoardDto);
        return ResponseEntity.ok(Map.of("data", searchResult));
    }

//    @GetMapping("/board/filter")
//    public ResponseEntity<?> searchFilter(@RequestParam(value = "tag") String tag) {
//        List<BoardListDto> result = boardService.filterByTag(tag);
//        return ResponseEntity.ok(Map.of("data", result));
//    }

    @GetMapping("/user/{id}/boards")
    public ResponseEntity<?> getUserBoards(@PathVariable("id") Long id) {
        List<BoardListDto> result = boardService.findAllByUserId(id);
        return ResponseEntity.ok(Map.of("data", result));
    }

    @PostMapping("/board/{id}/likes")
    public ResponseEntity<?> saveLike(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                       @PathVariable("id") Long id) {
        String username = customOAuth2User.getUsername();
        boardLikeService.likeBoard(username, id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/board/{id}/likes")
    public ResponseEntity<?> deleteLike(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                       @PathVariable("id") Long id) {
        String username = customOAuth2User.getUsername();
        boardLikeService.unlikeBoard(username, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
