package org.johndoe.kitchensink.repositories;

import org.johndoe.kitchensink.config.MongoTestConfig;
import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.documents.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(MongoTestConfig.class)  // ✅ Import Testcontainers MongoDB
class PostRepositoryTest {

    private static final MongoDBContainer mongoDBContainer;

    static {
        mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:5.0"));
        mongoDBContainer.start();
    }

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private MemberRepository memberRepository;
    private Member testMember;
    private Post post1, post2;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        postRepository.deleteAll(); // Clean existing posts
        memberRepository.deleteAll(); // Clean existing members

        testMember = new Member();
        testMember.setMemberId(1L);
        testMember.setUsername("john_doe");
        testMember.setEmail("john.doe@email.com");
        testMember.setPhoneNumber("1234567890");
        testMember.setFirstName("John");
        testMember.setLastName("Doe");

        memberRepository.save(testMember);

        post1 = new Post();
        post1.setPostId("post1");
        post1.setTitle("First Post");
        post1.setMember(testMember);
        post1.setCreatedAt(LocalDateTime.now().minusHours(2));

        post2 = new Post();
        post2.setPostId("post2");
        post2.setTitle("Second Post with keyword");
        post2.setMember(testMember);
        post2.setCreatedAt(LocalDateTime.now());

        postRepository.saveAll(List.of(post1, post2));
    }

    @Test
    void testFindByTitleContainingIgnoreCase() {
        List<Post> posts = postRepository.findByTitleContainingIgnoreCase("keyword");
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getTitle()).containsIgnoringCase("keyword");
    }

    @Test
    void testDeleteByPostId() {
        postRepository.deleteByPostId("post1");
        assertThat(postRepository.findByPostId("post1")).isEmpty();
    }

    @Test
    void testDeleteByPostIdAndMember() {
        postRepository.deleteByPostIdAndMember("post2", testMember);
        assertThat(postRepository.findByPostId("post2")).isEmpty();
    }

    @Test
    void testFindByPostId() {
        assertThat(postRepository.findByPostId("post1")).isPresent();
    }

    @Test
    void testFindByMemberOrderByCreatedAtDesc() {
        List<Post> posts = postRepository.findByMemberOrderByCreatedAtDesc(testMember);
        assertThat(posts).hasSize(2);
        assertThat(posts.get(0).getPostId()).isEqualTo("post2"); // Most recent first
    }
}
