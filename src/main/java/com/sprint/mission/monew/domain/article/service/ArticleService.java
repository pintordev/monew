package com.sprint.mission.monew.domain.article.service;

import com.sprint.mission.monew.common.dto.CursorPageResponse;
import com.sprint.mission.monew.domain.article.dto.ArticleResponse;
import com.sprint.mission.monew.domain.article.dto.ArticleQueryCondition;
import com.sprint.mission.monew.domain.article.dto.ArticleViewResponse;
import com.sprint.mission.monew.domain.article.entity.Article;
import com.sprint.mission.monew.domain.article.entity.ArticleView;
import com.sprint.mission.monew.domain.article.exception.ArticleNotFoundException;
import com.sprint.mission.monew.domain.article.mapper.ArticleMapper;
import com.sprint.mission.monew.domain.article.mapper.ArticleViewMapper;
import com.sprint.mission.monew.domain.article.repository.ArticleRepository;
import com.sprint.mission.monew.domain.article.repository.ArticleViewRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ArticleService {

  private final ArticleRepository articleRepository;
  private final ArticleViewRepository articleViewRepository;
  private final ArticleMapper articleMapper;
  private final ArticleViewMapper articleViewMapper;

  public CursorPageResponse<ArticleResponse> search(ArticleQueryCondition condition, UUID requestUserId) {
    return articleRepository.search(condition, requestUserId);
  }

  public ArticleResponse getArticle(UUID articleId, UUID requestUserId) {
    Article article = articleRepository.findById(articleId)
        .filter(a -> !a.isDeleted())
        .orElseThrow(() -> ArticleNotFoundException.withId(articleId));
    boolean viewedByMe = articleViewRepository.existsByArticleIdAndUserId(articleId, requestUserId);
    return articleMapper.toResponse(article, viewedByMe);
  }

  @Transactional
  public void hardDelete(UUID articleId) {
    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> ArticleNotFoundException.withId(articleId));
    articleRepository.delete(article);
  }

  @Transactional
  public void softDelete(UUID articleId) {
    Article article = articleRepository.findById(articleId)
        .filter(a -> !a.isDeleted())
        .orElseThrow(() -> ArticleNotFoundException.withId(articleId));
    article.softDelete();
  }

  @Transactional
  public ArticleViewResponse registerView(UUID articleId, UUID userId) {
    Article article = articleRepository.findById(articleId)
        .filter(a -> !a.isDeleted())
        .orElseThrow(() -> ArticleNotFoundException.withId(articleId));

    return articleViewRepository.findByArticleIdAndUserId(articleId, userId)
        .map(articleViewMapper::toResponse)
        .orElseGet(() -> {
          article.incrementViewCount();
          ArticleView saved = articleViewRepository.save(ArticleView.create(userId, article));
          return articleViewMapper.toResponse(saved);
        });
  }
}
