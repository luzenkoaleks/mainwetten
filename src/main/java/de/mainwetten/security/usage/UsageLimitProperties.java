package de.mainwetten.security.usage;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "app.usage-limits")
public class UsageLimitProperties {

    @Min(1)
    private int betCreationsPer24Hours;

    @Min(1)
    private int maxActiveCreatedBets;

    @Min(1)
    private int catchCreationsPer24Hours;

    public int getBetCreationsPer24Hours() {
        return betCreationsPer24Hours;
    }

    public void setBetCreationsPer24Hours(
            int betCreationsPer24Hours
    ) {
        this.betCreationsPer24Hours =
                betCreationsPer24Hours;
    }

    public int getMaxActiveCreatedBets() {
        return maxActiveCreatedBets;
    }

    public void setMaxActiveCreatedBets(
            int maxActiveCreatedBets
    ) {
        this.maxActiveCreatedBets =
                maxActiveCreatedBets;
    }

    public int getCatchCreationsPer24Hours() {
        return catchCreationsPer24Hours;
    }

    public void setCatchCreationsPer24Hours(
            int catchCreationsPer24Hours
    ) {
        this.catchCreationsPer24Hours =
                catchCreationsPer24Hours;
    }
}
