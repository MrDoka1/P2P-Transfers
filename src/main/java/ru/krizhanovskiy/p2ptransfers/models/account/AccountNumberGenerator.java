package ru.krizhanovskiy.p2ptransfers.models.account;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {
    private final SecureRandom random = new SecureRandom();

    // Префикс счёта: 40817 — физлицо, 810 — рубли
    private static final String PREFIX = "40817810";

    public String generateAccountNumber() {
        String uniquePart = String.format("%010d", random.nextLong(1_000_000_0000L));
        String accountWithoutChecksum = PREFIX + "00" + uniquePart; // временно "00" вместо контрольной суммы

        String checksum = calculateChecksum(accountWithoutChecksum);
        return PREFIX + checksum + uniquePart;
    }

    private String calculateChecksum(String raw) {
        int sum = 0;
        for (char ch : raw.toCharArray()) {
            sum += ch - '0';
        }
        int checkSum = (100 - (sum % 100)) % 100;
        return String.format("%02d", checkSum);
    }
}
