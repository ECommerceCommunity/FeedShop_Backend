package com.cMall.feedShop;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.entity.FeedVote;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.domain.repository.FeedVoteRepository;
import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.order.domain.repository.OrderRepository;
import com.cMall.feedShop.product.domain.enums.CategoryType;
import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.DiscountType;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.ImageType;
import com.cMall.feedShop.product.domain.enums.Size;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.CategoryRepository;
import com.cMall.feedShop.product.domain.repository.ProductImageRepository;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.store.domain.repository.StoreRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * nGrinder 부하 테스트용 고정 데이터셋. {@code perf} 프로필에서만 동작하며,
 * {@code perf_user_001}이 이미 있으면 전체를 건너뜁니다.
 */
@Component("perfDataInitializer")
@Profile("perf")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private static final String PERF_USER_001_LOGIN = "perf_user_001";
    private static final int PERF_USER_COUNT = 50;
    private static final int PERF_EVENT_COUNT = 20;
    private static final int FEEDS_PER_PERF_EVENT = 10;
    private static final int VOTES_PER_USER = 20;

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final OrderRepository orderRepository;
    private final EventRepository eventRepository;
    private final FeedRepository feedRepository;
    private final FeedVoteRepository feedVoteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByLoginId(PERF_USER_001_LOGIN)) {
            log.info("[perf] 초기 데이터가 이미 존재합니다 ({}). DataInitializer 전체 skip.", PERF_USER_001_LOGIN);
            return;
        }

        String encodedPassword = passwordEncoder.encode("Test1234!");
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= PERF_USER_COUNT; i++) {
            String suffix = String.format("%03d", i);
            User user = User.builder()
                    .loginId("perf_user_" + suffix)
                    .password(encodedPassword)
                    .email("perf_user_" + suffix + "@test.com")
                    .role(UserRole.USER)
                    .build();
            user.setStatus(UserStatus.ACTIVE);
            users.add(user);
        }
        userRepository.saveAll(users);
        userRepository.flush();

        User seller = userRepository.findByLoginId(PERF_USER_001_LOGIN).orElseThrow();

        Store store = storeRepository.save(Store.builder()
                .storeName("성능테스트스토어")
                .sellerId(seller.getId())
                .description("nGrinder 테스트용 스토어")
                .logo(null)
                .build());

        Category category = categoryRepository.save(new Category(CategoryType.SNEAKERS, "테스트카테고리"));

        Product product = productRepository.save(Product.builder()
                .name("테스트상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .discountType(DiscountType.NONE)
                .discountValue(null)
                .description(null)
                .build());

        ProductOption productOption = productOptionRepository.save(
                new ProductOption(Gender.UNISEX, Size.SIZE_270, Color.WHITE, 9999, product));

        ProductImage productImage = productImageRepository.save(
                new ProductImage("https://test-image.com/perf-test.jpg", ImageType.MAIN, product));

        Order order = Order.builder()
                .user(seller)
                .status(OrderStatus.ORDERED)
                .totalPrice(new BigDecimal("50000"))
                .finalPrice(new BigDecimal("50000"))
                .deliveryFee(BigDecimal.ZERO)
                .usedPoints(0)
                .earnedPoints(0)
                .deliveryAddress("서울시 테스트구 테스트동")
                .deliveryDetailAddress("101호")
                .postalCode("12345")
                .recipientName("테스트수령인")
                .recipientPhone("010-0000-0000")
                .deliveryMessage(null)
                .paymentMethod("CARD")
                .cardNumber(null)
                .cardExpiry(null)
                .cardCvc(null)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .productOption(productOption)
                .productImage(productImage)
                .quantity(1)
                .totalPrice(new BigDecimal("50000"))
                .finalPrice(new BigDecimal("50000"))
                .build();
        order.addOrderItem(orderItem);
        orderRepository.save(order);
        orderRepository.flush();

        LocalDate today = LocalDate.now();
        LocalDate perfStart = today.minusDays(7);
        LocalDate perfEnd = today.plusDays(7);

        List<Event> perfEvents = new ArrayList<>();
        for (int i = 1; i <= PERF_EVENT_COUNT; i++) {
            EventType type = (i % 2 == 1) ? EventType.BATTLE : EventType.RANKING;
            Event event = Event.builder()
                    .status(EventStatus.ONGOING)
                    .type(type)
                    .maxParticipants(100_000)
                    .createdUser(seller)
                    .createdBy(LocalDateTime.now())
                    .deletedAt(null)
                    .build();

            EventDetail detail = EventDetail.builder()
                    .title("테스트이벤트상세_" + i)
                    .description("성능테스트용이벤트상세_" + i)
                    .eventStartDate(perfStart)
                    .eventEndDate(perfEnd)
                    .purchaseStartDate(null)
                    .purchaseEndDate(null)
                    .announcement(null)
                    .participationMethod(null)
                    .selectionCriteria(null)
                    .precautions(null)
                    .imageUrl(null)
                    .build();
            event.setEventDetail(detail);
            perfEvents.add(eventRepository.save(event));
        }

        Event concurrencyEvent = Event.builder()
                .status(EventStatus.ONGOING)
                .type(EventType.BATTLE)
                .maxParticipants(100_000)
                .createdUser(seller)
                .createdBy(LocalDateTime.now())
                .deletedAt(null)
                .build();
        EventDetail concurrencyDetail = EventDetail.builder()
                .title("동시성테스트이벤트")
                .description("성능테스트용이벤트상세_21")
                .eventStartDate(today)
                .eventEndDate(today)
                .purchaseStartDate(null)
                .purchaseEndDate(null)
                .announcement(null)
                .participationMethod(null)
                .selectionCriteria(null)
                .precautions(null)
                .imageUrl(null)
                .build();
        concurrencyEvent.setEventDetail(concurrencyDetail);
        eventRepository.save(concurrencyEvent);

        Feed firstPerfFeed = null;
        int feedNum = 1;
        for (Event perfEvent : perfEvents) {
            for (int n = 0; n < FEEDS_PER_PERF_EVENT; n++) {
                User feedUser = users.get((feedNum - 1) % PERF_USER_COUNT);
                Feed feed = Feed.builder()
                        .event(perfEvent)
                        .orderItem(orderItem)
                        .user(feedUser)
                        .title("테스트피드_" + feedNum)
                        .content(null)
                        .instagramId(null)
                        .build();
                Feed saved = feedRepository.save(feed);
                if (feedNum == 1) {
                    firstPerfFeed = saved;
                }
                feedNum++;
            }
        }

        Feed concurrencyFeed = feedRepository.save(Feed.builder()
                .event(concurrencyEvent)
                .orderItem(orderItem)
                .user(seller)
                .title("동시성테스트피드")
                .content(null)
                .instagramId(null)
                .build());

        if (firstPerfFeed == null) {
            throw new IllegalStateException("성능 테스트용 첫 피드가 생성되지 않았습니다.");
        }

        List<FeedVote> votes = new ArrayList<>(PERF_USER_COUNT * VOTES_PER_USER);
        for (User voter : users) {
            for (int v = 0; v < VOTES_PER_USER; v++) {
                votes.add(FeedVote.builder()
                        .event(firstPerfFeed.getEvent())
                        .feed(firstPerfFeed)
                        .voter(voter)
                        .build());
            }
        }
        feedVoteRepository.saveAll(votes);
        feedVoteRepository.flush();

        for (int i = 0; i < votes.size(); i++) {
            firstPerfFeed.incrementVoteCount();
        }

        String scenarioCVoterLoginIds = IntStream.rangeClosed(1, PERF_USER_COUNT)
                .mapToObj(i -> String.format("perf_user_%03d", i))
                .collect(Collectors.joining(","));

        log.info("[perf] 초기화 완료 — 시나리오 B용 고정 feedId: {}", firstPerfFeed.getId());
        log.info("[perf] 초기화 완료 — 시나리오 C용 이벤트 ID: {}", concurrencyEvent.getId());
        log.info("[perf] 초기화 완료 — 시나리오 C용 피드 ID: {}", concurrencyFeed.getId());
        log.info("[perf] 초기화 완료 — 시나리오 C용 투표자 loginId 목록: {}", scenarioCVoterLoginIds);
    }
}
