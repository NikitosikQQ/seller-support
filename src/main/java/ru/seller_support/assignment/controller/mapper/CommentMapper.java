package ru.seller_support.assignment.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import ru.seller_support.assignment.adapter.postgres.entity.CommentEntity;
import ru.seller_support.assignment.controller.dto.request.comment.CommentSaveRequest;
import ru.seller_support.assignment.controller.dto.request.comment.CommentUpdateRequest;
import ru.seller_support.assignment.controller.dto.response.comments.CommentsResponseDto;
import ru.seller_support.assignment.domain.comment.ConditionForCommentModel;
import ru.seller_support.assignment.domain.comment.RuleForCommentModel;
import ru.seller_support.assignment.domain.enums.ConditionOperator;
import ru.seller_support.assignment.domain.enums.FieldForCondition;
import ru.seller_support.assignment.domain.enums.GroupLogic;

import java.util.List;

import static ru.seller_support.assignment.controller.dto.request.comment.CommentSaveRequest.ConditionForCommentSaveDto;
import static ru.seller_support.assignment.controller.dto.request.comment.CommentSaveRequest.RuleForCommentSaveDto;
import static ru.seller_support.assignment.controller.dto.response.comments.CommentsResponseDto.ConditionForCommentDto;
import static ru.seller_support.assignment.controller.dto.response.comments.CommentsResponseDto.RuleForCommentDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        imports = {GroupLogic.class, FieldForCondition.class, ConditionOperator.class})
public interface CommentMapper {

    CommentsResponseDto toCommentsDto(CommentEntity comment);

    List<ConditionForCommentDto> toListConditionForCommentDto(List<ConditionForCommentModel> conditions);

    @Mapping(target = "groupLogic", source = "groupLogic.value")
    ConditionForCommentDto toConditionDto(ConditionForCommentModel condition);

    @Mapping(target = "field", source = "field.viewValue")
    @Mapping(target = "condition", source = "condition.value")
    RuleForCommentDto toRuleDto(RuleForCommentModel rule);

    @Mapping(target = "id", ignore = true)
    CommentEntity toEntity(CommentSaveRequest request);

    CommentEntity updateEntity(CommentUpdateRequest request, @MappingTarget CommentEntity entity);

    @Mapping(target = "groupLogic", expression = "java(GroupLogic.fromSymbol(dto.getGroupLogic()))")
    ConditionForCommentModel toCondition(ConditionForCommentSaveDto dto);

    @Mapping(target = "field", expression = "java(FieldForCondition.fromSymbol(dto.getField()))")
    @Mapping(target = "condition", expression = "java(ConditionOperator.fromSymbol(dto.getCondition()))")
    RuleForCommentModel toRule(RuleForCommentSaveDto dto);
}
