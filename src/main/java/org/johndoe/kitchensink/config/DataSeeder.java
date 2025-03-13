package org.johndoe.kitchensink.config;

import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.repositories.MemberRepository;
import org.johndoe.kitchensink.utils.ApplicationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

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
     * Flag to indicate if the database should be refreshed.
     */
    @Value("${app.refresh.database}")
    private boolean refreshDatabase;

    /**
     * Constructor for er.
     *
     * @param memberRepository the member repository
     */
    public DataSeeder(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
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
    private void seedMemberData() {

        if (refreshDatabase) {
            log.warn("Refreshing database as per user configuration. Deleting all members.");
            memberRepository.deleteAll();
        }

        List<Member> members = List.of(
                new Member(1L, "john.doe", "John", "Doe", "john.doe@email.com", "9876543210", ApplicationConstants.ROLES.ADMIN.name()),
                new Member(2L, "jane.doe", "Jane", "Doe", "jane.doe@email.com", "8976543210", ApplicationConstants.ROLES.USER.name())
        );

        members.forEach(member -> memberRepository.findByMemberId(member.getMemberId())
                .ifPresentOrElse(
                        existingMember -> log.info("Member already exists, skipping: {}", member.getUsername()),
                        () -> {
                            log.info("Inserting member: {}", member.getUsername());
                            memberRepository.save(member);
                        }
                ));
    }
}
