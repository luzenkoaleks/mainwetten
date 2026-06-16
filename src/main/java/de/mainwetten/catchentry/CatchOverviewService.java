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

    private final CatchAssignmentRepository catchAssignmentRepository;

    public CatchOverviewService(CatchAssignmentRepository catchAssignmentRepository) {
        this.catchAssignmentRepository = catchAssignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<CatchSpeciesGroup> getGroupedCatches(Long betId) {
        List<CatchRecord> records = catchAssignmentRepository
                .findByBetIdWithDetailsOrderByCaughtAtDesc(betId)
                .stream()
                .map(CatchAssignment::getCatchRecord)
                .toList();

        Map<String, List<CatchRecord>> recordsBySpecies = records.stream()
                .collect(Collectors.groupingBy(record -> record.getFishSpecies().getName()));

        List<CatchSpeciesGroup> speciesGroups = new ArrayList<>();

        for (Map.Entry<String, List<CatchRecord>> speciesEntry : recordsBySpecies.entrySet()) {
            BigDecimal overallBestLength = speciesEntry.getValue()
                    .stream()
                    .map(CatchRecord::getLengthCm)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            Map<String, List<CatchRecord>> recordsByUser = speciesEntry.getValue()
                    .stream()
                    .collect(Collectors.groupingBy(record -> record.getUser().getUsername()));

            List<CatchUserGroup> userGroups = new ArrayList<>();

            for (Map.Entry<String, List<CatchRecord>> userEntry : recordsByUser.entrySet()) {
                List<CatchRecord> userRecords = userEntry.getValue()
                        .stream()
                        .sorted(
                                Comparator.comparing(CatchRecord::getLengthCm, Comparator.reverseOrder())
                                        .thenComparing(CatchRecord::getCaughtAt, Comparator.reverseOrder())
                        )
                        .toList();

                CatchRecord bestEntry = userRecords.getFirst();
                boolean overallBestForSpecies = bestEntry.getLengthCm().compareTo(overallBestLength) == 0;

                userGroups.add(new CatchUserGroup(
                        userEntry.getKey(),
                        bestEntry,
                        userRecords,
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