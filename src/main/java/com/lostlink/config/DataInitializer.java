package com.lostlink.config;

import com.lostlink.entity.*;
import com.lostlink.repository.ClaimRepository;
import com.lostlink.repository.ItemRepository;
import com.lostlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains data. Skipping sample data initialization.");
            return;
        }

        log.info("=================================================================");
        log.info(">>> INITIALIZING DEVELOPMENT SAMPLE DATA (DEVELOPMENT-ONLY) <<<");
        log.info("=================================================================");

        // Seed Users
        User admin = User.builder()
                .name("Campus Safety Administrator")
                .email("admin@lostlink.edu")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .active(true)
                .build();

        User user1 = User.builder()
                .name("Alex Johnson")
                .email("alex@college.edu")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .active(true)
                .build();

        User user2 = User.builder()
                .name("Sophia Martinez")
                .email("sophia@college.edu")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .active(true)
                .build();

        userRepository.saveAll(List.of(admin, user1, user2));
        log.info("Sample Admin created: admin@lostlink.edu / admin123 (DEV-ONLY)");
        log.info("Sample User 1 created: alex@college.edu / password123 (DEV-ONLY)");
        log.info("Sample User 2 created: sophia@college.edu / password123 (DEV-ONLY)");

        // Seed Items
        Item item1 = Item.builder()
                .title("Silver Apple MacBook Pro 14\"")
                .description("Space gray MacBook Pro 14-inch with M2 chip, found in Computer Science Lab room 302. Has a NASA sticker on the back casing.")
                .category("Electronics")
                .type(ItemType.FOUND)
                .date("2026-10-01")
                .location("CS Lab 302, Engineering Block")
                .imageUrl("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop&q=60")
                .status(ItemStatus.ACTIVE)
                .verificationQuestion("What distinctive sticker is located near the Apple logo, and what color is the keyboard silicone cover?")
                .verificationAnswer("NASA sticker and light blue keyboard cover")
                .userId(admin.getId())
                .postedByName("Campus Safety Administrator")
                .build();

        Item item2 = Item.builder()
                .title("Brown Leather Fossil Wallet")
                .description("Brown bifold leather wallet lost near the Student Center Cafeteria. Contains college student ID card and bus transit pass.")
                .category("Wallets & IDs")
                .type(ItemType.LOST)
                .date("2026-10-02")
                .location("Student Center Cafeteria")
                .imageUrl("https://images.unsplash.com/photo-1627123424574-724758594e93?w=800&auto=format&fit=crop&q=60")
                .status(ItemStatus.ACTIVE)
                .verificationQuestion("What is the student ID number and the expiration year printed inside the card slot?")
                .verificationAnswer("ID 984521 expiry 2027")
                .userId(user1.getId())
                .postedByName("Alex Johnson")
                .build();

        Item item3 = Item.builder()
                .title("Black Sony WH-1000XM4 Noise Canceling Headphones")
                .description("Left on the 2nd floor library study desk near the window. In a black zip case with aux adapter.")
                .category("Electronics")
                .type(ItemType.FOUND)
                .date("2026-10-02")
                .location("Central Library 2nd Floor")
                .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=60")
                .status(ItemStatus.ACTIVE)
                .verificationQuestion("What is written on the small keychain tag attached to the headphone case zipper?")
                .verificationAnswer("AJ 2024")
                .userId(user2.getId())
                .postedByName("Sophia Martinez")
                .build();

        Item item4 = Item.builder()
                .title("Set of Car & Dorm Keys with Red Lanyard")
                .description("A ring with 3 brass keys and one Honda electronic key fob on a red university alumni lanyard.")
                .category("Keys")
                .type(ItemType.LOST)
                .date("2026-09-28")
                .location("North Parking Lot near Gate 4")
                .imageUrl("https://images.unsplash.com/photo-1582139329536-e7284fece509?w=800&auto=format&fit=crop&q=60")
                .status(ItemStatus.ACTIVE)
                .verificationQuestion("What miniature souvenir metal charm is attached alongside the Honda key fob?")
                .verificationAnswer("Eiffel tower miniature charm")
                .userId(user1.getId())
                .postedByName("Alex Johnson")
                .build();

        Item item5 = Item.builder()
                .title("Calculus: Early Transcendentals Textbook (9th Ed)")
                .description("Hardcover calculus textbook by Stewart. Left on bench outside Lecture Hall B.")
                .category("Books & Stationery")
                .type(ItemType.FOUND)
                .date("2026-10-03")
                .location("Science Complex - Hall B")
                .imageUrl("https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800&auto=format&fit=crop&q=60")
                .status(ItemStatus.RESOLVED)
                .verificationQuestion("What handwritten note or formula is written on the inside front cover?")
                .verificationAnswer("Integration by parts formula with blue ink")
                .userId(admin.getId())
                .postedByName("Campus Safety Administrator")
                .build();

        itemRepository.saveAll(List.of(item1, item2, item3, item4, item5));
        log.info("Initialized {} sample lost/found items.", 5);

        // Seed Sample Claim
        Claim claim1 = Claim.builder()
                .itemId(item1.getId())
                .claimantId(user1.getId())
                .answer("NASA sticker and light blue keyboard cover")
                .status(ClaimStatus.PENDING)
                .build();

        claimRepository.save(claim1);
        log.info("Initialized 1 sample pending claim.");
        log.info("=================================================================");
    }
}
