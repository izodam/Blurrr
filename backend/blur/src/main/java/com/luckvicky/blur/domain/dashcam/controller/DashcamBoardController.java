package com.luckvicky.blur.domain.dashcam.controller;

import com.luckvicky.blur.domain.dashcam.model.dto.DashcamBoardDetailDto;
import com.luckvicky.blur.domain.dashcam.model.dto.DashcamBoardListDto;
import com.luckvicky.blur.domain.dashcam.model.dto.request.DashcamBoardCreateRequest;
import com.luckvicky.blur.domain.dashcam.model.dto.response.DashcamBoardCreateResponse;
import com.luckvicky.blur.domain.dashcam.model.dto.response.DashcamBoardListResponse;
import com.luckvicky.blur.domain.dashcam.model.dto.response.DashcamBoardResponse;
import com.luckvicky.blur.domain.dashcam.service.DashcamBoardService;
import com.luckvicky.blur.global.jwt.model.ContextMember;
import com.luckvicky.blur.global.model.dto.Result;
import com.luckvicky.blur.global.security.AuthUser;
import com.luckvicky.blur.global.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.luckvicky.blur.infra.aws.service.S3ImageService;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Tag(name = "블랙박스 게시글 API")
@RestController
@RequestMapping("/v1/channels/dashcams/boards")
@RequiredArgsConstructor
public class DashcamBoardController {

    private final DashcamBoardService dashcamBoardService;
    private final S3ImageService s3ImageService;

    @Operation(summary = "블랙박스 게시글 목록 조회 API")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = DashcamBoardListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "게시글 없음"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 목록 조회 실패"
            )
    })
    @Parameters({
            @Parameter(name = "pageNumber", description = "페이지 번호"),
            @Parameter(
                    name = "criteria",
                    description = "정렬 기준",
                    examples = {
                            @ExampleObject(name = "최신", value = "TIME"),
                            @ExampleObject(name = "좋아요", value = "LIKE"),
                            @ExampleObject(name = "조회수", value = "VIEW"),
                            @ExampleObject(name = "댓글", value = "COMMENT"),
                    }
            ),
    })
    @GetMapping
    public ResponseEntity getDashcamBoards(
            @RequestParam(required = false, defaultValue = "0", value = "pageNumber") int pageNumber,
            @RequestParam(required = false, defaultValue = "TIME", value = "criteria") String criteria
    ){
        List<DashcamBoardListDto> boardDtos = dashcamBoardService.getDashcamBoards(
                pageNumber,
                criteria
        );

        if (Objects.isNull(boardDtos) || boardDtos.isEmpty()) {
            return ResponseUtil.noContent(
                    Result.builder().build()
            );
        }

        return ResponseUtil.ok(
                Result.builder()
                        .data(DashcamBoardListResponse.of(boardDtos))
                        .build()
        );

    }


    @Operation(summary = "블랙박스 게시글 상세 조회 API")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = DashcamBoardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음"
            )
    })
    @GetMapping("/{boardId}")
    public ResponseEntity<DashcamBoardResponse> getDashcamBoard(
            @Parameter(description = "게시글 ID", required = true) @PathVariable("boardId") UUID id) {
        DashcamBoardDetailDto boardDto = dashcamBoardService.getDashcamBoardById(id);
        return ResponseEntity.ok(DashcamBoardResponse.of(boardDto));
    }


    @Operation(summary = "블랙박스 게시글 생성 API")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "게시글 생성 성공",
                    content = @Content(schema = @Schema(implementation = DashcamBoardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 또는 리그를 찾을 수 없음"
            )
    })
    @PostMapping
    public ResponseEntity<Result<DashcamBoardCreateResponse>> createDashcamBoard(
            @Valid
            @RequestBody DashcamBoardCreateRequest request,
            @AuthUser ContextMember contextMember) {

        return ResponseUtil.created(
                Result.<DashcamBoardCreateResponse>builder()
                        .data(dashcamBoardService.createDashcamBoard(request, contextMember.getId()))
                        .build()
        );

    }



    @Operation(summary = "비디오 presigned url 요청 API")
    @GetMapping("/aws")
    public ResponseEntity<Result<Map<String, String>>> getUrl(
            @RequestParam(name = "fileName")
            @Schema(description = "동영상파일 이름 (확장자명 포함)") String fileName) throws MalformedURLException {
        return ResponseUtil.ok(
                Result.of(s3ImageService.getPresignedUrl("videos", fileName))
        );
    }

}
