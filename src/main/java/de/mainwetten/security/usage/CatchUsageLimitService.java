package de.mainwetten.security.usage;

import de.mainwetten.catchentry.CatchRecordRepository;
import de.mainwetten.user.AppUser;
import de.mainwetten.user.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class CatchUsageLimitService {

    private final CatchRecordRepository catchRecordRepository;
    private final AppUserRepository appUserRepository;
    private final UsageLimitProperties usageLimitProperties;

    public CatchUsageLimitService(
            CatchRecordRepository catchRecordRepository,
            AppUserRepository appUserRepository,
            UsageLimitProperties usageLimitProperties
    ) {
        this.catchRecordRepository =
                catchRecordRepository;
        this.appUserRepository =
                appUserRepository;
        this.usageLimitProperties =
                usageLimitProperties;
    }

    @Transactional
    public AppUser lockUserAndCheckLimit(Long userId) {
        AppUser lockedUser = appUserRepository
                .findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User nicht gefunden"
                        )
                );

        OffsetDateTime caughtAfter =
                OffsetDateTime.now().minusHours(24);

        long recentCatchCount =
                catchRecordRepository
                        .countCreatedByUserSince(
                                userId,
                                caughtAfter
                        );

        int catchLimit =
                usageLimitProperties
                        .getCatchCreationsPer24Hours();

        if (recentCatchCount >= catchLimit) {
            throw new UsageLimitExceededException(
                    "Du kannst innerhalb von 24 Stunden "
                            + "maximal "
                            + catchLimit
                            + " "
                            + catchLabel(catchLimit)
                            + " eintragen."
            );
        }

        return lockedUser;
    }

    private String catchLabel(int number) {
        return number == 1
                ? "Fang"
                : "Fänge";
    }
}
