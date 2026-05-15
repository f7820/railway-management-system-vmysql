package com.railway.railway_management_system.data;

import com.railway.railway_management_system.enums.BookingStatus;
import com.railway.railway_management_system.enums.Gender;
import com.railway.railway_management_system.enums.Role;
import com.railway.railway_management_system.enums.ScheduleStatus;
import com.railway.railway_management_system.enums.SeatClass;
import com.railway.railway_management_system.enums.TrainType;
import com.railway.railway_management_system.model.Booking;
import com.railway.railway_management_system.model.Passenger;
import com.railway.railway_management_system.model.Route;
import com.railway.railway_management_system.model.Schedule;
import com.railway.railway_management_system.model.ScheduleSeatAvailability;
import com.railway.railway_management_system.model.SeatConfig;
import com.railway.railway_management_system.model.Train;
import com.railway.railway_management_system.model.User;
import com.railway.railway_management_system.repository.BookingRepository;
import com.railway.railway_management_system.repository.PassengerRepository;
import com.railway.railway_management_system.repository.RouteRepository;
import com.railway.railway_management_system.repository.ScheduleRepository;
import com.railway.railway_management_system.repository.ScheduleSeatAvailabilityRepository;
import com.railway.railway_management_system.repository.TrainRepository;
import com.railway.railway_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final UserRepository                     userRepository;
    private final PasswordEncoder                    passwordEncoder;
    private final TrainRepository                    trainRepository;
    private final RouteRepository                    routeRepository;
    private final ScheduleRepository                 scheduleRepository;
    private final ScheduleSeatAvailabilityRepository seatAvailabilityRepository;
    private final PassengerRepository                passengerRepository;
    private final BookingRepository                  bookingRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        createDefaultUserIfNotExits();
        createDefaultAdminIfNotExits();
        seedDemoData();
    }

    // ---------- user seeding ----------

    private void createDefaultUserIfNotExits() {
        for (int i = 1; i <= 5; i++) {
            String defaultUsername = "staffuser" + i;
            if (userRepository.existsByUsername(defaultUsername)) continue;
            User user = new User();
            user.setFirstName("Staff");
            user.setLastName("Staff" + i);
            user.setUsername(defaultUsername);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole(Role.STAFF);
            userRepository.save(user);
            System.out.println("Default staff user " + i + " created successfully.");
        }
    }

    private void createDefaultAdminIfNotExits() {
        for (int i = 1; i <= 2; i++) {
            String defaultUsername = "adminuser" + i;
            if (userRepository.existsByUsername(defaultUsername)) continue;
            User user = new User();
            user.setFirstName("Admin");
            user.setLastName("Admin" + i);
            user.setUsername(defaultUsername);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole(Role.ADMINISTRATOR);
            userRepository.save(user);
            System.out.println("Default admin user " + i + " created successfully.");
        }
    }

    // ---------- demo data seeding ----------

    private void seedDemoData() {
        if (!trainRepository.findByName("Al Haramain High Speed Railway").isEmpty()) {
            return; // already seeded on a previous run
        }

        // ── Trains ──────────────────────────────────────────────────────
        Train alHaramain = seedTrain();
        Train metro1     = seedMetroTrain("Riyadh Metro Line 1 (Blue)",   40, 80);
        Train metro2     = seedMetroTrain("Riyadh Metro Line 2 (Red)",    40, 80);
        Train metro3     = seedMetroTrain("Riyadh Metro Line 3 (Orange)", 40, 80);
        Train metro4     = seedMetroTrain("Riyadh Metro Line 4 (Yellow)", 40, 80);
        Train metro5     = seedMetroTrain("Riyadh Metro Line 5 (Green)",  40, 80);
        Train metro6     = seedMetroTrain("Riyadh Metro Line 6 (Purple)", 40, 80);
        Train northTr    = seedIntercityTrain("North Train", 50, 80, 30);
        Train eastTr     = seedIntercityTrain("East Train",  50, 80, 30);

        // ── Routes ──────────────────────────────────────────────────────
        Route jeddahMakkah = seedRoute();
        Route routeM1      = seedRoute("Riyadh Metro Line 1 (Blue)",   "Alawwal Bank",          "Ad Dar Al Baida",         38.0, 25);
        Route routeM2      = seedRoute("Riyadh Metro Line 2 (Red)",    "King Saud University",  "King Fahd Sports City",   25.3, 14);
        Route routeM3      = seedRoute("Riyadh Metro Line 3 (Orange)", "Jeddah Road",           "Khashm Al-An",            40.7, 22);
        Route routeM4      = seedRoute("Riyadh Metro Line 4 (Yellow)", "KAFD",                  "Airport (Terminals 1-2)", 29.6,  9);
        Route routeM5      = seedRoute("Riyadh Metro Line 5 (Green)",  "Ministry of Education", "National Museum",         12.9, 12);
        Route routeM6      = seedRoute("Riyadh Metro Line 6 (Purple)", "KAFD",                  "An Naseem",               29.9, 11);
        Route routeDmm     = seedRoute("Riyadh-Dammam Route",          "Riyadh Railway Station","Dammam Railway Station",  449.0, 4);
        Route routeQsm     = seedRoute("Riyadh-Qurayyat Route",          "Riyadh Railway Station","Qurayyat Railway Station",  1250.0, 6);

        // ── Passengers (seed all before schedules so the full list is available) ──
        seedPassenger();
        seedPassenger(1098765432, "Ahmed",    "Al-Qahtani",  Gender.MALE,   "ahmed.alqahtani@example.sa",   "+966512345678");
        seedPassenger(1087654321, "Fatima",   "Al-Zahrani",  Gender.FEMALE, "fatima.alzahrani@example.sa",  "+966523456789");
        seedPassenger(1076543210, "Khalid",   "Al-Dosari",   Gender.MALE,   "khalid.aldosari@example.sa",   "+966534567890");
        seedPassenger(1065432109, "Nora",     "Al-Otaibi",   Gender.FEMALE, "nora.alotaibi@example.sa",     "+966545678901");
        seedPassenger(1054321098, "Abdullah", "Al-Ghamdi",   Gender.MALE,   "abdullah.alghamdi@example.sa", "+966556789012");
        seedPassenger(1043210987, "Sara",     "Al-Harbi",    Gender.FEMALE, "sara.alharbi@example.sa",      "+966567890123");
        seedPassenger(1032109876, "Omar",     "Al-Shehri",   Gender.MALE,   "omar.alshehri@example.sa",     "+966578901234");
        seedPassenger(1021098765, "Mona",     "Al-Maliki",   Gender.FEMALE, "mona.almaliki@example.sa",     "+966589012345");

        List<Passenger> pax = passengerRepository.findAll();

        // ── Al Haramain + Jeddah-Makkah  (110 seats) — 80% 90% 95% 100% 50% ──
        sf(alHaramain, jeddahMakkah, ldt(2026, 6, 1,  8, 0), ldt(2026, 6, 1,  8,45), false,  80, pax);
        sf(alHaramain, jeddahMakkah, ldt(2025,12,15,  9, 0), ldt(2025,12,15,  9,45), true,   90, pax);
        sf(alHaramain, jeddahMakkah, ldt(2025,10, 1,  9, 0), ldt(2025,10, 1,  9,45), true,   95, pax);
        sf(alHaramain, jeddahMakkah, ldt(2025,11,20, 14, 0), ldt(2025,11,20, 14,45), true,  100, pax);
        sf(alHaramain, jeddahMakkah, ldt(2026, 7, 1, 11, 0), ldt(2026, 7, 1, 11,45), false,  50, pax);

        // ── Metro Line 1 Blue  (120 seats) — 95% 90% 80% 60% 80% ────
        sf(metro1, routeM1, ldt(2025,10, 5,  7, 0), ldt(2025,10, 5,  7,45), true,   95, pax);
        sf(metro1, routeM1, ldt(2025,11,12, 12, 0), ldt(2025,11,12, 12,45), true,   90, pax);
        sf(metro1, routeM1, ldt(2026, 6,10,  8, 0), ldt(2026, 6,10,  8,45), false,  80, pax);
        sf(metro1, routeM1, ldt(2026, 6,10, 13, 0), ldt(2026, 6,10, 13,45), false,  60, pax);
        sf(metro1, routeM1, ldt(2026, 6,10, 18, 0), ldt(2026, 6,10, 18,45), false,  80, pax);

        // ── Metro Line 2 Red   (120 seats) — 80% 70% 90% 95% 40% ────
        sf(metro2, routeM2, ldt(2025,10, 8,  6,30), ldt(2025,10, 8,  7, 0), true,   80, pax);
        sf(metro2, routeM2, ldt(2025,11,20, 13, 0), ldt(2025,11,20, 13,30), true,   70, pax);
        sf(metro2, routeM2, ldt(2026, 6,15,  7, 0), ldt(2026, 6,15,  7,30), false,  90, pax);
        sf(metro2, routeM2, ldt(2026, 6,15, 11, 0), ldt(2026, 6,15, 11,30), false,  95, pax);
        sf(metro2, routeM2, ldt(2026, 6,15, 17, 0), ldt(2026, 6,15, 17,30), false,  40, pax);

        // ── Metro Line 3 Orange (120 seats) — 100% 70% 90% 95% 40% ──
        sf(metro3, routeM3, ldt(2025,10,10,  8, 0), ldt(2025,10,10,  8,50), true,  100, pax);
        sf(metro3, routeM3, ldt(2025,12, 1, 14, 0), ldt(2025,12, 1, 14,50), true,   70, pax);
        sf(metro3, routeM3, ldt(2026, 6,20,  9, 0), ldt(2026, 6,20,  9,50), false,  90, pax);
        sf(metro3, routeM3, ldt(2026, 6,20, 13, 0), ldt(2026, 6,20, 13,50), false,  95, pax);
        sf(metro3, routeM3, ldt(2026, 6,20, 18, 0), ldt(2026, 6,20, 18,50), false,  40, pax);

        // ── Metro Line 4 Yellow (120 seats) — 50% 90% 30% 95% 50% ──
        sf(metro4, routeM4, ldt(2025,10,15,  6, 0), ldt(2025,10,15,  6,35), true,   50, pax);
        sf(metro4, routeM4, ldt(2025,11,25, 11, 0), ldt(2025,11,25, 11,35), true,   90, pax);
        sf(metro4, routeM4, ldt(2026, 6,25,  7,30), ldt(2026, 6,25,  8, 5), false,  30, pax);
        sf(metro4, routeM4, ldt(2026, 6,25, 12,30), ldt(2026, 6,25, 13, 5), false,  95, pax);
        sf(metro4, routeM4, ldt(2026, 6,25, 17,30), ldt(2026, 6,25, 18, 5), false,  50, pax);

        // ── Metro Line 5 Green  (120 seats) — 80% 60% 90% 90% 50% ──
        sf(metro5, routeM5, ldt(2025,10,20,  7, 0), ldt(2025,10,20,  7,20), true,   80, pax);
        sf(metro5, routeM5, ldt(2025,12,10, 12,30), ldt(2025,12,10, 12,50), true,   60, pax);
        sf(metro5, routeM5, ldt(2026, 7, 1,  8, 0), ldt(2026, 7, 1,  8,20), false,  90, pax);
        sf(metro5, routeM5, ldt(2026, 7, 1, 12, 0), ldt(2026, 7, 1, 12,20), false,  90, pax);
        sf(metro5, routeM5, ldt(2026, 7, 1, 17, 0), ldt(2026, 7, 1, 17,20), false,  50, pax);

        // ── Metro Line 6 Purple (120 seats) — 55% 70% 90% 100% 20% ─
        sf(metro6, routeM6, ldt(2025,10,25,  6,30), ldt(2025,10,25,  7, 5), true,   55, pax);
        sf(metro6, routeM6, ldt(2025,12,15, 13, 0), ldt(2025,12,15, 13,35), true,   70, pax);
        sf(metro6, routeM6, ldt(2026, 7, 5,  7, 0), ldt(2026, 7, 5,  7,35), false,  90, pax);
        sf(metro6, routeM6, ldt(2026, 7, 5, 11, 0), ldt(2026, 7, 5, 11,35), false, 100, pax);
        sf(metro6, routeM6, ldt(2026, 7, 5, 16, 0), ldt(2026, 7, 5, 16,35), false,  20, pax);

        // ── North Train + Riyadh-Qassim (160 seats) — 50% 70% 60% 20% 40% ──
        sf(northTr, routeQsm, ldt(2025,11, 1,  7, 0), ldt(2025,11, 1, 10,30), true,   50, pax);
        sf(northTr, routeQsm, ldt(2025,12,20,  8, 0), ldt(2025,12,20, 11,30), true,   70, pax);
        sf(northTr, routeQsm, ldt(2026, 7,10,  7, 0), ldt(2026, 7,10, 10,30), false,  60, pax);
        sf(northTr, routeQsm, ldt(2026, 7,17,  7, 0), ldt(2026, 7,17, 10,30), false,  20, pax);
        sf(northTr, routeQsm, ldt(2026, 7,24,  7, 0), ldt(2026, 7,24, 10,30), false,  40, pax);

        // ── East Train + Riyadh-Dammam  (160 seats) — 80% 50% 30% 90% 20% ──
        sf(eastTr, routeDmm, ldt(2025,11, 5,  6, 0), ldt(2025,11, 5, 10,30), true,   80, pax);
        sf(eastTr, routeDmm, ldt(2025,12,25,  7, 0), ldt(2025,12,25, 11,30), true,   50, pax);
        sf(eastTr, routeDmm, ldt(2026, 7,12,  6, 0), ldt(2026, 7,12, 10,30), false,  30, pax);
        sf(eastTr, routeDmm, ldt(2026, 7,19,  6, 0), ldt(2026, 7,19, 10,30), false,  90, pax);
        sf(eastTr, routeDmm, ldt(2026, 7,26,  6, 0), ldt(2026, 7,26, 10,30), false,  20, pax);

        // ── Today + current-month schedules (6 today / 3 same-month not today) ──
        LocalDate td  = LocalDate.now();
        int       dom = td.getDayOfMonth();
        // ~7 days earlier in month (floor at day 1)
        LocalDate mb1 = td.withDayOfMonth(dom > 7 ? dom - 7 : 1);
        // ~3 days earlier in month (floor at day 1, distinct from mb1)
        LocalDate mb2 = td.withDayOfMonth(dom > 3 ? dom - 3 : Math.max(1, dom - 1));
        // ~7 days later in month (cap at last day of month)
        LocalDate ma  = td.withDayOfMonth(Math.min(td.lengthOfMonth(), dom + 7));
        // completion flag follows whether the date is strictly before today
        boolean c1 = mb1.isBefore(td), c2 = mb2.isBefore(td), ca = ma.isBefore(td);

        // ── Al Haramain (45-min trip) — 75% 85% 90% | 100% 60% 45% | 80% 95% 70% ──
        sf(alHaramain, jeddahMakkah,  td.atTime( 6, 0),  td.atTime( 6,45), true,  75, pax);
        sf(alHaramain, jeddahMakkah,  td.atTime( 8, 0),  td.atTime( 8,45), true,  85, pax);
        sf(alHaramain, jeddahMakkah,  td.atTime(10, 0),  td.atTime(10,45), true,  90, pax);
        sf(alHaramain, jeddahMakkah,  td.atTime(13, 0),  td.atTime(13,45), false,100, pax);
        sf(alHaramain, jeddahMakkah,  td.atTime(15,30),  td.atTime(16,15), false, 60, pax);
        sf(alHaramain, jeddahMakkah,  td.atTime(18, 0),  td.atTime(18,45), false, 45, pax);
        sf(alHaramain, jeddahMakkah, mb1.atTime( 8, 0), mb1.atTime( 8,45),  c1,  80, pax);
        sf(alHaramain, jeddahMakkah, mb2.atTime(10,30), mb2.atTime(11,15),  c2,  95, pax);
        sf(alHaramain, jeddahMakkah,  ma.atTime( 8, 0),  ma.atTime( 8,45),  ca,  70, pax);

        // ── Metro Line 1 Blue (45-min trip) — 90% 85% 75% | 65% 80% 45% | 95% 70% 55% ──
        sf(metro1, routeM1,  td.atTime( 6, 0),  td.atTime( 6,45), true,  90, pax);
        sf(metro1, routeM1,  td.atTime( 8, 0),  td.atTime( 8,45), true,  85, pax);
        sf(metro1, routeM1,  td.atTime(10, 0),  td.atTime(10,45), true,  75, pax);
        sf(metro1, routeM1,  td.atTime(13, 0),  td.atTime(13,45), false, 65, pax);
        sf(metro1, routeM1,  td.atTime(15,30),  td.atTime(16,15), false, 80, pax);
        sf(metro1, routeM1,  td.atTime(18, 0),  td.atTime(18,45), false, 45, pax);
        sf(metro1, routeM1, mb1.atTime( 8, 0), mb1.atTime( 8,45),  c1,  95, pax);
        sf(metro1, routeM1, mb2.atTime(10,30), mb2.atTime(11,15),  c2,  70, pax);
        sf(metro1, routeM1,  ma.atTime( 8, 0),  ma.atTime( 8,45),  ca,  55, pax);

        // ── Metro Line 2 Red (30-min trip) — 75% 65% 85% | 90% 35% 80% | 60% 95% 45% ──
        sf(metro2, routeM2,  td.atTime( 6, 0),  td.atTime( 6,30), true,  75, pax);
        sf(metro2, routeM2,  td.atTime( 8, 0),  td.atTime( 8,30), true,  65, pax);
        sf(metro2, routeM2,  td.atTime(10, 0),  td.atTime(10,30), true,  85, pax);
        sf(metro2, routeM2,  td.atTime(13, 0),  td.atTime(13,30), false, 90, pax);
        sf(metro2, routeM2,  td.atTime(15,30),  td.atTime(16, 0), false, 35, pax);
        sf(metro2, routeM2,  td.atTime(18, 0),  td.atTime(18,30), false, 80, pax);
        sf(metro2, routeM2, mb1.atTime( 7, 0), mb1.atTime( 7,30),  c1,  60, pax);
        sf(metro2, routeM2, mb2.atTime(11, 0), mb2.atTime(11,30),  c2,  95, pax);
        sf(metro2, routeM2,  ma.atTime( 7, 0),  ma.atTime( 7,30),  ca,  45, pax);

        // ── Metro Line 3 Orange (50-min trip) — 95% 65% 85% | 90% 35% 75% | 100% 80% 50% ──
        sf(metro3, routeM3,  td.atTime( 6, 0),  td.atTime( 6,50), true,  95, pax);
        sf(metro3, routeM3,  td.atTime( 8, 0),  td.atTime( 8,50), true,  65, pax);
        sf(metro3, routeM3,  td.atTime(10, 0),  td.atTime(10,50), true,  85, pax);
        sf(metro3, routeM3,  td.atTime(13, 0),  td.atTime(13,50), false, 90, pax);
        sf(metro3, routeM3,  td.atTime(15,30),  td.atTime(16,20), false, 35, pax);
        sf(metro3, routeM3,  td.atTime(18, 0),  td.atTime(18,50), false, 75, pax);
        sf(metro3, routeM3, mb1.atTime( 8, 0), mb1.atTime( 8,50),  c1, 100, pax);
        sf(metro3, routeM3, mb2.atTime(10, 0), mb2.atTime(10,50),  c2,  80, pax);
        sf(metro3, routeM3,  ma.atTime( 8, 0),  ma.atTime( 8,50),  ca,  50, pax);

        // ── Metro Line 4 Yellow (35-min trip) — 45% 85% 25% | 90% 45% 80% | 70% 55% 40% ──
        sf(metro4, routeM4,  td.atTime( 6, 0),  td.atTime( 6,35), true,  45, pax);
        sf(metro4, routeM4,  td.atTime( 8, 0),  td.atTime( 8,35), true,  85, pax);
        sf(metro4, routeM4,  td.atTime(10, 0),  td.atTime(10,35), true,  25, pax);
        sf(metro4, routeM4,  td.atTime(13, 0),  td.atTime(13,35), false, 90, pax);
        sf(metro4, routeM4,  td.atTime(15,30),  td.atTime(16, 5), false, 45, pax);
        sf(metro4, routeM4,  td.atTime(18, 0),  td.atTime(18,35), false, 80, pax);
        sf(metro4, routeM4, mb1.atTime( 8, 0), mb1.atTime( 8,35),  c1,  70, pax);
        sf(metro4, routeM4, mb2.atTime(11, 0), mb2.atTime(11,35),  c2,  55, pax);
        sf(metro4, routeM4,  ma.atTime( 8, 0),  ma.atTime( 8,35),  ca,  40, pax);

        // ── Metro Line 5 Green (20-min trip) — 75% 55% 85% | 85% 45% 90% | 60% 80% 50% ──
        sf(metro5, routeM5,  td.atTime( 6, 0),  td.atTime( 6,20), true,  75, pax);
        sf(metro5, routeM5,  td.atTime( 8, 0),  td.atTime( 8,20), true,  55, pax);
        sf(metro5, routeM5,  td.atTime(10, 0),  td.atTime(10,20), true,  85, pax);
        sf(metro5, routeM5,  td.atTime(13, 0),  td.atTime(13,20), false, 85, pax);
        sf(metro5, routeM5,  td.atTime(15,30),  td.atTime(15,50), false, 45, pax);
        sf(metro5, routeM5,  td.atTime(18, 0),  td.atTime(18,20), false, 90, pax);
        sf(metro5, routeM5, mb1.atTime( 7,30), mb1.atTime( 7,50),  c1,  60, pax);
        sf(metro5, routeM5, mb2.atTime(11, 0), mb2.atTime(11,20),  c2,  80, pax);
        sf(metro5, routeM5,  ma.atTime( 7,30),  ma.atTime( 7,50),  ca,  50, pax);

        // ── Metro Line 6 Purple (35-min trip) — 50% 65% 85% | 95% 15% 75% | 100% 60% 25% ──
        sf(metro6, routeM6,  td.atTime( 6, 0),  td.atTime( 6,35), true,  50, pax);
        sf(metro6, routeM6,  td.atTime( 8, 0),  td.atTime( 8,35), true,  65, pax);
        sf(metro6, routeM6,  td.atTime(10, 0),  td.atTime(10,35), true,  85, pax);
        sf(metro6, routeM6,  td.atTime(13, 0),  td.atTime(13,35), false, 95, pax);
        sf(metro6, routeM6,  td.atTime(15,30),  td.atTime(16, 5), false, 15, pax);
        sf(metro6, routeM6,  td.atTime(18, 0),  td.atTime(18,35), false, 75, pax);
        sf(metro6, routeM6, mb1.atTime( 7, 0), mb1.atTime( 7,35),  c1, 100, pax);
        sf(metro6, routeM6, mb2.atTime(11, 0), mb2.atTime(11,35),  c2,  60, pax);
        sf(metro6, routeM6,  ma.atTime( 7, 0),  ma.atTime( 7,35),  ca,  25, pax);

        // ── North Train (3h30m trip) — 45% 65% 55% | 15% 35% 70% | 50% 80% 40% ──
        sf(northTr, routeQsm,  td.atTime( 5, 0),  td.atTime( 8,30), true,  45, pax);
        sf(northTr, routeQsm,  td.atTime( 7, 0),  td.atTime(10,30), true,  65, pax);
        sf(northTr, routeQsm,  td.atTime( 9, 0),  td.atTime(12,30), true,  55, pax);
        sf(northTr, routeQsm,  td.atTime(12, 0),  td.atTime(15,30), false, 15, pax);
        sf(northTr, routeQsm,  td.atTime(14, 0),  td.atTime(17,30), false, 35, pax);
        sf(northTr, routeQsm,  td.atTime(16, 0),  td.atTime(19,30), false, 70, pax);
        sf(northTr, routeQsm, mb1.atTime( 7, 0), mb1.atTime(10,30),  c1,  50, pax);
        sf(northTr, routeQsm, mb2.atTime( 7, 0), mb2.atTime(10,30),  c2,  80, pax);
        sf(northTr, routeQsm,  ma.atTime( 7, 0),  ma.atTime(10,30),  ca,  40, pax);

        // ── East Train (4h30m trip) — 75% 45% 25% | 85% 15% 60% | 80% 40% 55% ──
        sf(eastTr, routeDmm,  td.atTime( 4, 0),  td.atTime( 8,30), true,  75, pax);
        sf(eastTr, routeDmm,  td.atTime( 6, 0),  td.atTime(10,30), true,  45, pax);
        sf(eastTr, routeDmm,  td.atTime( 7, 0),  td.atTime(11,30), true,  25, pax);
        sf(eastTr, routeDmm,  td.atTime(11, 0),  td.atTime(15,30), false, 85, pax);
        sf(eastTr, routeDmm,  td.atTime(13, 0),  td.atTime(17,30), false, 15, pax);
        sf(eastTr, routeDmm,  td.atTime(15, 0),  td.atTime(19,30), false, 60, pax);
        sf(eastTr, routeDmm, mb1.atTime( 6, 0), mb1.atTime(10,30),  c1,  80, pax);
        sf(eastTr, routeDmm, mb2.atTime( 6, 0), mb2.atTime(10,30),  c2,  40, pax);
        sf(eastTr, routeDmm,  ma.atTime( 6, 0),  ma.atTime(10,30),  ca,  55, pax);
    }

    private Train seedTrain() {
        Train train = new Train();
        train.setName("Al Haramain High Speed Railway");
        train.setType(TrainType.HIGH_SPEED_INTERCITY);
        train.setSeatConfigurations(new ArrayList<>());

        train.getSeatConfigurations().add(new SeatConfig(null, SeatClass.BUSINESS, 40, 100.0, train));
        train.getSeatConfigurations().add(new SeatConfig(null, SeatClass.ECONOMY,  50,  70.0, train));
        train.getSeatConfigurations().add(new SeatConfig(null, SeatClass.SLEEPER,  20, 200.0, train));

        Train saved = trainRepository.save(train);
        System.out.println("[Demo] Train created: " + saved.getName() + " [" + saved.getTrainNumber() + "]");
        return saved;
    }

    private Route seedRoute() {
        Route route = new Route();
        route.setRouteName("Jeddah-Makkah Route");
        route.setStartStation("King Abdulaziz Station, Jeddah");
        route.setEndStation("Makkah Al Mukarramah Station");
        route.setDistanceKm(200.0);
        route.setStops(2);

        Route saved = routeRepository.save(route);
        System.out.println("[Demo] Route created: " + saved.getRouteName() + " [" + saved.getRouteNumber() + "]");
        return saved;
    }

    private Passenger seedPassenger() {
        Passenger passenger = new Passenger(
                1012345678,
                "Mohammed",
                "Al-Rashidi",
                Gender.MALE,
                "mohammed.alrashidi@example.sa",
                "+966501234567"
        );
        Passenger saved = passengerRepository.save(passenger);
        System.out.println("[Demo] Passenger created: " + saved.getFirstName() + " " + saved.getLastName());
        return saved;
    }

    // ---------- bulk seed helpers ----------

    private Train seedMetroTrain(String name, int businessSeats, int economySeats) {
        Train train = new Train();
        train.setName(name);
        train.setType(TrainType.METRO);
        train.setSeatConfigurations(new ArrayList<>());
        train.getSeatConfigurations().add(new SeatConfig(null, SeatClass.BUSINESS, businessSeats, 25.0, train));
        train.getSeatConfigurations().add(new SeatConfig(null, SeatClass.ECONOMY,  economySeats,  15.0, train));
        Train saved = trainRepository.save(train);
        System.out.println("[Demo] Train created: " + saved.getName() + " [" + saved.getTrainNumber() + "]");
        return saved;
    }

    private Train seedIntercityTrain(String name, int businessSeats, int economySeats, int sleeperSeats) {
        Train train = new Train();
        train.setName(name);
        train.setType(TrainType.INTERCITY);
        train.setSeatConfigurations(new ArrayList<>());
        train.getSeatConfigurations().add(new SeatConfig(null, SeatClass.BUSINESS, businessSeats,  90.0, train));
        train.getSeatConfigurations().add(new SeatConfig(null, SeatClass.ECONOMY,  economySeats,   60.0, train));
        train.getSeatConfigurations().add(new SeatConfig(null, SeatClass.SLEEPER,  sleeperSeats,  150.0, train));
        Train saved = trainRepository.save(train);
        System.out.println("[Demo] Train created: " + saved.getName() + " [" + saved.getTrainNumber() + "]");
        return saved;
    }

    private Route seedRoute(String name, String from, String to, double distanceKm, int stops) {
        Route route = new Route();
        route.setRouteName(name);
        route.setStartStation(from);
        route.setEndStation(to);
        route.setDistanceKm(distanceKm);
        route.setStops(stops);
        Route saved = routeRepository.save(route);
        System.out.println("[Demo] Route created: " + saved.getRouteName() + " [" + saved.getRouteNumber() + "]");
        return saved;
    }

    /** Short alias used in seedDemoData for readability. */
    private void sf(Train train, Route route, LocalDateTime dep, LocalDateTime arr,
                    boolean completed, int fillPct, List<Passenger> passengers) {
        seedScheduleWithFill(train, route, dep, arr, completed, fillPct, passengers);
    }

    private void seedScheduleWithFill(Train train, Route route,
                                      LocalDateTime dep, LocalDateTime arr,
                                      boolean completed, int fillPct,
                                      List<Passenger> passengers) {
        Schedule schedule = new Schedule();
        schedule.setTrain(train);
        schedule.setRoute(route);
        schedule.setDepartureTime(dep);
        schedule.setArrivalTime(arr);
        schedule.setStatus(ScheduleStatus.ON_TIME);
        schedule.setCompleted(completed);
        Schedule saved = scheduleRepository.save(schedule);

        List<SeatConfig> configs = train.getSeatConfigurations();
        int totalSeats = configs.stream().mapToInt(SeatConfig::getTotalSeats).sum();
        int target = Math.min((int) Math.ceil(totalSeats * fillPct / 100.0), totalSeats);
        int[] dist  = distributeProportionally(configs, target, totalSeats);

        int pIdx = 0;
        for (int i = 0; i < configs.size(); i++) {
            SeatConfig cfg   = configs.get(i);
            int booked       = dist[i];
            int available    = cfg.getTotalSeats() - booked;
            seatAvailabilityRepository.save(buildAvailability(
                    saved, cfg.getSeatClass(), cfg.getTotalSeats(), available, booked + 1, cfg.getBasePrice()));
            String prefix = seatPrefix(cfg.getSeatClass());
            for (int j = 1; j <= booked; j++) {
                Booking b = new Booking();
                b.setSeatNumber(prefix + j);
                b.setFinalPrice(cfg.getBasePrice());
                b.setBookedClass(cfg.getSeatClass());
                b.setPassenger(passengers.get(pIdx % passengers.size()));
                b.setSchedule(saved);
                Booking sb = bookingRepository.save(b);
                if (completed) {
                    sb.setStatus(BookingStatus.COMPLETED);
                    bookingRepository.save(sb);
                }
                pIdx++;
            }
        }
        System.out.printf("[Demo] Schedule %s — %d%% fill (%d/%d seats, %s)%n",
                saved.getScheduleNumber(), fillPct, target, totalSeats,
                completed ? "completed" : "upcoming");
    }

    private int[] distributeProportionally(List<SeatConfig> configs, int total, int totalSeats) {
        int n = configs.size();
        int[] result = new int[n];
        int allocated = 0;
        for (int i = 0; i < n; i++) {
            result[i] = (int) Math.round((double) total * configs.get(i).getTotalSeats() / totalSeats);
            result[i] = Math.min(result[i], configs.get(i).getTotalSeats());
            allocated += result[i];
        }
        // Correct any rounding drift on the last class
        int diff = total - allocated;
        for (int i = n - 1; i >= 0 && diff != 0; i--) {
            if (diff > 0) {
                int add = Math.min(diff, configs.get(i).getTotalSeats() - result[i]);
                result[i] += add; diff -= add;
            } else {
                int sub = Math.min(-diff, result[i]);
                result[i] -= sub; diff += sub;
            }
        }
        return result;
    }

    private String seatPrefix(SeatClass sc) {
        return switch (sc) {
            case BUSINESS -> "B";
            case ECONOMY  -> "E";
            case SLEEPER  -> "S";
        };
    }

    private static LocalDateTime ldt(int y, int mo, int d, int h, int min) {
        return LocalDateTime.of(y, mo, d, h, min);
    }

    private void seedPassenger(int idNumber, String firstName, String lastName,
                               Gender gender, String email, String phone) {
        Passenger passenger = new Passenger(idNumber, firstName, lastName, gender, email, phone);
        Passenger saved = passengerRepository.save(passenger);
        System.out.println("[Demo] Passenger created: " + saved.getFirstName() + " " + saved.getLastName());
    }

    // ---------- helpers ----------

    private ScheduleSeatAvailability buildAvailability(
            Schedule schedule, SeatClass seatClass,
            int totalSeats, int availableSeats, int nextSeatNumber, double basePrice) {
        ScheduleSeatAvailability avail = new ScheduleSeatAvailability();
        avail.setSchedule(schedule);
        avail.setSeatClass(seatClass);
        avail.setTotalSeats(totalSeats);
        avail.setAvailableSeats(availableSeats);
        avail.setNextSeatNumber(nextSeatNumber);
        avail.setBasePrice(basePrice);
        return avail;
    }
}
