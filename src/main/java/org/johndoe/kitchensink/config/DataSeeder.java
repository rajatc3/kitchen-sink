package org.johndoe.kitchensink.config;

import lombok.AllArgsConstructor;
import org.johndoe.kitchensink.documents.Comment;
import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.documents.Post;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.repositories.CommentRepository;
import org.johndoe.kitchensink.repositories.MemberRepository;
import org.johndoe.kitchensink.repositories.PostRepository;
import org.johndoe.kitchensink.services.KeycloakAuthService;
import org.johndoe.kitchensink.utils.ApplicationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Component responsible for seeding member data into the database.
 * Implements CommandLineRunner to execute the seeding logic on application startup.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    /**
     * Logger for DataSeeder.
     */
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    /**
     * Repository for member data.
     */
    private final MemberRepository memberRepository;

    /**
     * Service for managing Keycloak authentication.
     */
    private final KeycloakAuthService keycloakAuthService;

    /**
     * Repository for post data.
     */
    private final PostRepository postRepository;

    /**
     * Repository for comment data.
     */
    private final CommentRepository commentRepository;

    /**
     * Flag to indicate if the database should be refreshed.
     */
    @Value("${app.refresh.database:true}")
    boolean refreshDatabase;

    /**
     * Constructs a new DataSeeder with the given repositories and service.
     *
     * @param memberRepository    the member repository
     * @param keycloakAuthService  the KeycloakAuthService
     * @param postRepository      the post repository
     * @param commentRepository   the comment repository
     */
    public DataSeeder(MemberRepository memberRepository, KeycloakAuthService keycloakAuthService,
                      PostRepository postRepository, CommentRepository commentRepository) {
        this.memberRepository = memberRepository;
        this.keycloakAuthService = keycloakAuthService;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    /**
     * Executes the seeding logic on application startup.
     * This method is called after the application context is loaded and
     * right before the Spring Application run method is completed.
     *
     * @param args command line arguments
     * @throws Exception if an error occurs during the seeding process
     */
    @Override
    public void run(String... args) throws Exception {
        seedMemberData();
    }

    /**
     * Seeds the member data into the database. If the `refreshDatabase` flag is set to true,
     * it will truncate the database before seeding the data. It inserts a predefined list of members
     * into the database if they do not already exist.
     */
    void seedMemberData() {

        if (refreshDatabase) {
            log.warn("Refreshing database as per user configuration. Deleting all members.");
            memberRepository.deleteAll();
            postRepository.deleteAll();
            commentRepository.deleteAll();
        }

        List<MemberDto> memberDto = List.of(
                new MemberDto("john.doe", "John", "Doe", "john.doe@email.com", "9876543210", "admin".toCharArray(), "admin".toCharArray(), ApplicationConstants.ROLES.ADMIN.name().toLowerCase()),
                new MemberDto("jane.doe", "Jane", "Doe", "jane.doe@email.com", "9876543211", "user".toCharArray(), "user".toCharArray(), ApplicationConstants.ROLES.USER.name().toLowerCase())
        );

        memberDto.forEach(member -> memberRepository.findByUsername(member.getUsername())
                .ifPresentOrElse(
                        existingMember -> log.info("Member already exists, skipping: {}", member.getUsername()),
                        () -> {
                            log.info("Inserting member: {}", member.getUsername());
                            keycloakAuthService.register(member);
                            generateDummyPostsAndComments(member);
                        }
                ));
    }

    void generateDummyPostsAndComments(MemberDto member) {
        Member memberEntity = memberRepository.findByUsername(member.getUsername()).orElseThrow(
                () -> new IllegalStateException("Member not found: " + member.getUsername()));

        // Creating dummy posts
        List<Post> posts = List.of(
                new Post(null, memberEntity, "First Post by " + member.getUsername(), "This is a sample post content.", new ArrayList<>()),
                new Post(null, memberEntity, "Another Post by " + member.getUsername(), "Exploring content creation.", new ArrayList<>())
        );
        postRepository.saveAll(posts);

        Random random = new Random();
        List<String> randomComments = List.of(
                "This is a great post!",
                "Thanks for sharing your thoughts.",
                "Very insightful!",
                "I totally agree!",
                "Could you elaborate more on this?",
                "Interesting perspective!"
        );

        posts.forEach(post -> {
            // Creating dummy comments
            List<Comment> comments = List.of(
                    new Comment(null, memberEntity, post.getPostId(), randomComments.get(random.nextInt(randomComments.size()))),
                    new Comment(null, memberEntity, post.getPostId(), randomComments.get(random.nextInt(randomComments.size())))
            );
            commentRepository.saveAll(comments);

            // Attach comment IDs to the post and update
            post.setCommentIds(comments.stream().map(Comment::getCommentId).collect(Collectors.toList()));
            postRepository.save(post);
        });

        log.info("Generated dummy posts and comments for user: {}", member.getUsername());
    }
}
