package ceb.domain.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SendOtpResponse {

    private String message;
    private long expiresInSeconds;
    private long resendAvailableInSeconds;
    private int remainingResends;
}
