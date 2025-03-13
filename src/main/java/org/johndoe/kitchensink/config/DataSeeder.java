package org.johndoe.kitchensink.config;

import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.repositories.MemberRepository;
import org.johndoe.kitchensink.services.KeycloakAuthService;
import org.johndoe.kitchensink.utils.ApplicationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.johndoe.kitchensink.dtos.MemberDto.Mapper.fromEntity;

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
     * Flag to indicate if the database should be refreshed.
     */
    @Value("${app.refresh.database}")
    private boolean refreshDatabase;

    /**
     * Constructor for er.
     *
     * @param memberRepository the member repository
     * @param keycloakAuthService the keycloak auth service
     */
    public DataSeeder(MemberRepository memberRepository, KeycloakAuthService keycloakAuthService) {
        this.memberRepository = memberRepository;
        this.keycloakAuthService = keycloakAuthService;
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
                        }
                ));
    }
}
