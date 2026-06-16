package de.mainwetten.catchentry;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CatchOverviewService {

    private final CatchEntryRepository catchEntryRepository;

    public CatchOverviewService(CatchEntryRepository catchEntryRepository) {
        this.catchEntryRepository = catchEntryRepository;
    }

    @Transactional(readOnly = true)
    public List<CatchSpeciesGroup> getGroupedCatches(Long betId) {
        List<CatchEntry> entries = catchEntryRepository.findByBetIdOrderByCaughtAtDescCreatedAtDesc(betId);

        Map<String, List<CatchEntry>> entriesBySpecies = entries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getFishSpecies().getName()));

        List<CatchSpeciesGroup> speciesGroups = new ArrayList<>();

        for (Map.Entry<String, List<CatchEntry>> speciesEntry : entriesBySpecies.entrySet()) {
            BigDecimal overallBestLength = speciesEntry.getValue()
                    .stream()
                    .map(CatchEntry::getLengthCm)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            Map<String, List<CatchEntry>> entriesByUser = speciesEntry.getValue()
                    .stream()
                    .collect(Collectors.groupingBy(entry -> entry.getUser().getUsername()));

            List<CatchUserGroup> userGroups = new ArrayList<>();

            for (Map.Entry<String, List<CatchEntry>> userEntry : entriesByUser.entrySet()) {
                List<CatchEntry> userEntries = userEntry.getValue()
                        .stream()
                        .sorted(
                                Comparator.comparing(CatchEntry::getLengthCm, Comparator.reverseOrder())
                                        .thenComparing(CatchEntry::getCaughtAt, Comparator.reverseOrder())
                        )
                        .toList();

                CatchEntry bestEntry = userEntries.getFirst();
                boolean overallBestForSpecies = bestEntry.getLengthCm().compareTo(overallBestLength) == 0;

                userGroups.add(new CatchUserGroup(
                        userEntry.getKey(),
                        bestEntry,
                        userEntries,
                        overallBestForSpecies
                ));
            }

            userGroups.sort(
                    Comparator.comparing(
                                    (CatchUserGroup group) -> group.getBestEntry().getLengthCm()
                            )
                            .reversed()
                            .thenComparing(CatchUserGroup::getUsername)
            );

            speciesGroups.add(new CatchSpeciesGroup(
                    speciesEntry.getKey(),
                    userGroups
            ));
        }

        speciesGroups.sort(Comparator.comparing(CatchSpeciesGroup::getFishSpeciesName));

        return speciesGroups;
    }
}