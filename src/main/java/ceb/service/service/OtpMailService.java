package ceb.service.service;

import ceb.domain.res.SendOtpResponse;

public interface OtpMailService {

    SendOtpResponse sendOtp(String email);

    void verifyOtp(String email, String otp);
}
