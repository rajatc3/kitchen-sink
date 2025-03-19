package org.johndoe.kitchensink.repositories;

import org.johndoe.kitchensink.config.MongoTestConfig;
import org.johndoe.kitchensink.documents.Comment;
import org.johndoe.kitchensink.documents.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(MongoTestConfig.class)  // ✅ Import Testcontainers MongoDB
class CommentRepositoryTest {

    private static final MongoDBContainer mongoDBContainer;

    static {
        mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:5.0"));
        mongoDBContainer.start();
    }

    @Autowired
    private CommentRepository commentRepository;
    private Member testMember;
    private Comment comment1, comment2;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();

        testMember = new Member();
        testMember.setId("member1");

        comment1 = new Comment();
        comment1.setCommentId("comment1");
        comment1.setPostId("post1");
        comment1.setMember(testMember);
        comment1.setCreatedAt(LocalDateTime.now().minusSeconds(3600));

        comment2 = new Comment();
        comment2.setCommentId("comment2");
        comment2.setPostId("post1");
        comment2.setMember(testMember);
        comment2.setCreatedAt(LocalDateTime.now());

        commentRepository.saveAll(List.of(comment1, comment2));
    }

    @Test
    void testFindByPostId() {
        List<Comment> comments = commentRepository.findByPostId("post1", Sort.by(Sort.Direction.ASC, "createdAt"));
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getCommentId()).isEqualTo("comment1");
    }

    @Test
    void testFindByMember() {
        List<Comment> comments = commentRepository.findByMember(testMember);
        assertThat(comments).hasSize(2);
    }

    @Test
    void testCountByMember() {
        long count = commentRepository.countByMember(testMember);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testCountByPostId() {
        int count = commentRepository.countByPostId("post1");
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testDeleteById() {
        commentRepository.deleteByCommentId("comment1");
        assertThat(commentRepository.findById("comment1")).isEmpty();
    }

    @Test
    void testDeleteByIdAndMember() {
        commentRepository.deleteByCommentIdAndMember("comment2", testMember);
        assertThat(commentRepository.findById("comment2")).isEmpty();
    }
}
