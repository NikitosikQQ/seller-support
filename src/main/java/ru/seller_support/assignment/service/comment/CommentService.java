package ru.seller_support.assignment.service.comment;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.ArticlePromoInfoEntity;
import ru.seller_support.assignment.adapter.postgres.entity.CommentEntity;
import ru.seller_support.assignment.adapter.postgres.repository.CommentRepository;
import ru.seller_support.assignment.controller.dto.request.comment.CommentSaveRequest;
import ru.seller_support.assignment.controller.dto.request.comment.CommentUpdateRequest;
import ru.seller_support.assignment.controller.mapper.CommentMapper;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.comment.ConditionForCommentModel;
import ru.seller_support.assignment.domain.comment.RuleForCommentModel;
import ru.seller_support.assignment.domain.enums.FieldForCondition;
import ru.seller_support.assignment.service.ArticlePromoInfoService;
import ru.seller_support.assignment.service.comment.handler.ConditionForCommentHandler;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final List<ConditionForCommentHandler> conditionHandlers;
    private final ArticlePromoInfoService articleService;
    private final CommentRepository repository;
    private final CommentMapper mapper;

    @Transactional
    public void save(CommentSaveRequest request) {
        CommentEntity comment = mapper.toEntity(request);
        if (Objects.nonNull(request.getArticlesName()) && !request.getArticlesName().isEmpty()) {
            Set<ArticlePromoInfoEntity> articles = articleService.findAllByNames(request.getArticlesName());
            comment.setArticles(articles);
        }
        repository.save(comment);
    }

    @Transactional
    public List<CommentEntity> findAll() {
        return repository.findAllByOrderByCreatedAtAsc();
    }

    @Transactional
    public CommentEntity findById(UUID id) {
        return repository.findById(id).orElseThrow(() ->
                new ValidationException(String.format("Comment с id %s не найден", id)));
    }

    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Transactional
    public void update(CommentUpdateRequest request) {
        CommentEntity comment = findById(request.getId());
        CommentEntity updated = mapper.updateEntity(request, comment);
        if (Objects.nonNull(request.getArticlesName()) && !request.getArticlesName().isEmpty()) {
            Set<ArticlePromoInfoEntity> articles = articleService.findAllByNames(request.getArticlesName());
            updated.setArticles(articles);
        } else {
            updated.setArticles(null);
        }
        repository.save(comment);
    }

    @Transactional
    public void addCommentsIfNecessary(List<PostingInfoModel> postings) {
        List<ArticlePromoInfoEntity> articles = articleService.findAllWithComments();
        List<String> promoNames = articles.stream().map(ArticlePromoInfoEntity::getName).toList();
        Map<String, List<PostingInfoModel>> groupedPostings = postings.stream()
                .filter(posting -> promoNames.contains(posting.getProduct().getPromoName()))
                .collect(Collectors.groupingBy(it -> it.getProduct().getPromoName()));

        groupedPostings.forEach((promoName, listOfPostings) ->
                articles.stream()
                        .filter(article -> article.getName().equalsIgnoreCase(promoName))
                        .findFirst()
                        .ifPresent(article -> {
                            Set<CommentEntity> comments = article.getComments();
                            comments.forEach(comment ->
                                    writeComments(comment.getConditions(), comment.getValue(), listOfPostings)
                            );
                        })
        );
    }

    private ConditionForCommentHandler getConditionForCommentHandlerByFieldType(FieldForCondition field) {
        return conditionHandlers.stream().filter(handler -> handler.getField() == field)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Не найдено обработчика комментария для поля типа " + field));

    }

    private void writeComments(List<ConditionForCommentModel> conditionsWithComments,
                               String commentValue,
                               List<PostingInfoModel> postings) {
        if (Objects.isNull(conditionsWithComments) || conditionsWithComments.getFirst().getRules().isEmpty()) {
            setCommentWithoutConditions(postings, commentValue);
            return;
        }
        conditionsWithComments.forEach(conditionWithComment ->
                checkConditionsAndSetComment(conditionWithComment, postings, commentValue));
    }

    private void checkConditionsAndSetComment(ConditionForCommentModel conditionWithComment,
                                              List<PostingInfoModel> postings,
                                              String commentValue) {

        postings.forEach(posting -> {
            List<RuleForCommentModel> rules = conditionWithComment.getRules();
            int passed = 0;
            for (RuleForCommentModel rule : rules) {
                FieldForCondition field = rule.getField();
                ConditionForCommentHandler conditionHandler = getConditionForCommentHandlerByFieldType(field);
                boolean resultOfRule = conditionHandler.checkConditions(rule, posting);
                passed = resultOfRule ? passed + 1 : passed;
            }
            var anyConditionHandler = conditionHandlers.getFirst();
            boolean conditionCompleted = anyConditionHandler.checkPassedWithGroupLogic(conditionWithComment, passed);
            String currentComment = posting.getProduct().getComment();
            posting.getProduct().setComment(anyConditionHandler.prepareComment(currentComment, commentValue, conditionCompleted));
        });
    }

    private void setCommentWithoutConditions(List<PostingInfoModel> postings, String commentValue) {
        FieldForCondition field = FieldForCondition.WITHOUT_CONDITIONS;
        ConditionForCommentHandler conditionHandler = getConditionForCommentHandlerByFieldType(field);
        postings.forEach(posting -> {
            String newComment = conditionHandler.prepareComment(posting.getProduct().getComment(), commentValue, true);
            posting.getProduct().setComment(newComment);
        });
    }
}
