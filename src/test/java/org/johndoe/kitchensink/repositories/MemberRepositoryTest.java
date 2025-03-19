package org.johndoe.kitchensink.repositories;

import org.johndoe.kitchensink.config.MongoTestConfig;
import org.johndoe.kitchensink.documents.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(MongoTestConfig.class)  // ✅ Import Testcontainers MongoDB
class MemberRepositoryTest {

    private static final MongoDBContainer mongoDBContainer;

    static {
        mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:5.0"));
        mongoDBContainer.start();
    }

    @Autowired
    private MemberRepository memberRepository;
    private Member testMember;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();

        testMember = new Member();
        testMember.setMemberId(1L);
        testMember.setUsername("john_doe");
        testMember.setEmail("john.doe@email.com");
        testMember.setPhoneNumber("1234567890");

        memberRepository.save(testMember);
    }

    @Test
    void testFindByMemberId() {
        Optional<Member> member = memberRepository.findByMemberId(1L);
        assertThat(member).isPresent();
        assertThat(member.get().getUsername()).isEqualTo("john_doe");
    }

    @Test
    void testFindByEmail() {
        Optional<Member> member = memberRepository.findByEmail("john.doe@email.com");
        assertThat(member).isPresent();
        assertThat(member.get().getUsername()).isEqualTo("john_doe");
    }

    @Test
    void testFindByPhoneNumber() {
        Optional<Member> member = memberRepository.findByPhoneNumber("1234567890");
        assertThat(member).isPresent();
    }

    @Test
    void testFindByUsername() {
        Optional<Member> member = memberRepository.findByUsername("john_doe");
        assertThat(member).isPresent();
    }

    @Test
    void testFindTopByOrderByMemberIdDesc() {
        Member anotherMember = new Member();
        anotherMember.setMemberId(2L);
        anotherMember.setUsername("jane_doe");
        anotherMember.setEmail("jane.doe@email.com");
        anotherMember.setPhoneNumber("0987654321");

        memberRepository.save(anotherMember);

        Optional<Member> topMember = memberRepository.findTopByOrderByMemberIdDesc();
        assertThat(topMember).isPresent();
        assertThat(topMember.get().getMemberId()).isEqualTo(2L);
    }

    @Test
    void testDeleteByMemberId() {
        memberRepository.deleteByMemberId(1L);
        assertThat(memberRepository.findByMemberId(1L)).isEmpty();
    }
}