package ru.seller_support.assignment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.adapter.postgres.entity.ArticlePromoInfoEntity;
import ru.seller_support.assignment.controller.dto.request.CommentDeleteRequest;
import ru.seller_support.assignment.controller.dto.request.CommentSaveRequest;
import ru.seller_support.assignment.controller.dto.request.CommentUpdateRequest;
import ru.seller_support.assignment.controller.dto.response.comments.CommentsResponseDto;
import ru.seller_support.assignment.controller.mapper.CommentMapper;
import ru.seller_support.assignment.domain.enums.ConditionOperator;
import ru.seller_support.assignment.domain.enums.FieldForCondition;
import ru.seller_support.assignment.domain.enums.GroupLogic;
import ru.seller_support.assignment.service.comment.CommentRequestValidator;
import ru.seller_support.assignment.service.comment.CommentService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper mapper;
    private final CommentRequestValidator validator;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CommentsResponseDto> getComments() {
        return commentService.findAll()
                .stream()
                .map(comment -> CommentsResponseDto.builder()
                        .id(comment.getId())
                        .value(comment.getValue())
                        .conditions(mapper.toListConditionForCommentDto(comment.getConditions()))
                        .conditionFields(FieldForCondition.getFieldsForCondition().stream().map(FieldForCondition::getViewValue).toList())
                        .conditionOperators(ConditionOperator.getOperators().stream().map(ConditionOperator::getValue).toList())
                        .logicGroupValues(GroupLogic.getGroupLogicSymbols().stream().map(GroupLogic::getValue).toList())
                        .articlesName(comment.getArticles().stream().map(ArticlePromoInfoEntity::getName).collect(Collectors.toSet()))
                        .build())
                .toList();
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveComment(@RequestBody @Valid CommentSaveRequest request) {
        validator.validate(request);
        commentService.save(request);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateComment(@RequestBody @Valid CommentUpdateRequest request) {
        validator.validate(request);
        commentService.update(request);
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteComment(@RequestBody @Valid CommentDeleteRequest request) {
        commentService.deleteById(request.getId());
    }
}
