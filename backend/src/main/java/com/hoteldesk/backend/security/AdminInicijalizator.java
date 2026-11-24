package com.hoteldesk.backend.security;

import com.hoteldesk.backend.model.Admin;
import com.hoteldesk.backend.repository.AdminRepository;
import com.hoteldesk.backend.repository.KorisnikRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInicijalizator implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final KorisnikRepository korisnikRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.password}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (adminRepository.count() > 0) {
            return;
        }
        if (password == null || password.isBlank()) {
            log.warn("Ne postoji admin nalog. Postavite ADMIN_PASSWORD da bi se admin kreirao pri pokretanju.");
            return;
        }
        String normalizovan = email.trim().toLowerCase();
        if (korisnikRepository.existsByEmail(normalizovan)) {
            log.warn("Admin nije kreiran jer korisnik sa email-om {} vec postoji", normalizovan);
            return;
        }
        Admin admin = new Admin();
        admin.setIme("Admin");
        admin.setPrezime("HotelDesk");
        admin.setEmail(normalizovan);
        admin.setPassword(passwordEncoder.encode(password));
        adminRepository.save(admin);
        log.info("Kreiran admin nalog {}", normalizovan);
    }
}
