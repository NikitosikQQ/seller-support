--liquibase formatted sql
--changeset n.akimov:create_commits_articles_mapping

CREATE TABLE IF NOT EXISTS comments_articles_mapping (
   comment_id UUID NOT NULL,
   article_id UUID NOT NULL,
   PRIMARY KEY (comment_id, article_id),
   FOREIGN KEY (comment_id) REFERENCES comment(id) ON DELETE CASCADE,
   FOREIGN KEY (article_id) REFERENCES article_promo_info(id) ON DELETE CASCADE
);

COMMENT ON TABLE comments_articles_mapping IS 'Связь комментариев с артикулами';