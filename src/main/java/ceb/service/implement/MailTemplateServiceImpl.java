package ceb.service.implement;

import java.time.Instant;

import org.springframework.stereotype.Service;

import ceb.service.service.MailTemplateService;

@Service
public class MailTemplateServiceImpl implements MailTemplateService {

    @Override
    public String buildOtpEmail(String otp, Instant expiresAt, long resendCooldownSeconds) {
        return baseTemplate(
                "Ma OTP Xac Thuc",
                "Xac thuc tai khoan cua ban",
                """
                <p style="margin:0 0 14px;color:#334155;font-size:15px;line-height:1.7;">
                  He thong da nhan duoc yeu cau xac thuc tai khoan cua ban.
                </p>
                <p style="margin:0 0 22px;color:#334155;font-size:15px;line-height:1.7;">
                  Vui long su dung ma OTP duoi day de tiep tuc. Khong chia se ma nay cho bat ky ai.
                </p>
                <div style="margin:0 0 24px;padding:26px 20px;border-radius:20px;background:#f8fafc;border:1px solid #dbe4dd;text-align:center;">
                  <div style="margin:0 0 10px;font-size:11px;letter-spacing:2.6px;text-transform:uppercase;color:#64748b;font-weight:700;">
                    Verification Code
                  </div>
                  <div style="font-size:36px;line-height:1;font-weight:800;letter-spacing:10px;color:#14532d;font-family:'Segoe UI',Arial,sans-serif;">
                    %s
                  </div>
                </div>
                <div style="margin:0 0 18px;padding:16px 18px;border-radius:16px;background:#ffffff;border:1px solid #e2e8f0;">
                  <p style="margin:0 0 8px;color:#0f172a;font-size:14px;font-weight:700;">Luu y bao mat</p>
                  <p style="margin:0;color:#475569;font-size:14px;line-height:1.7;">
                    Neu ban chua nhan duoc ma moi, ban co the yeu cau gui lai sau %s giay.
                  </p>
                </div>
                <p style="margin:0;color:#64748b;font-size:13px;line-height:1.7;">
                  Neu ban khong thuc hien yeu cau nay, vui long bo qua email nay de dam bao an toan cho tai khoan.
                </p>
                """.formatted(otp, resendCooldownSeconds));
    }

    @Override
    public String buildResetPasswordEmail(String fullName, String resetLink, String resetToken) {
        String safeFullName = fullName == null || fullName.isBlank() ? "ban" : fullName;

        return baseTemplate(
                "Dat Lai Mat Khau",
                "Yeu cau dat lai mat khau",
                """
                <p style="margin:0 0 16px;color:#475569;font-size:15px;line-height:1.7;">
                  Xin chao <strong style="color:#0f172a;">%s</strong>,
                </p>
                <p style="margin:0 0 18px;color:#475569;font-size:15px;line-height:1.7;">
                  He thong vua nhan duoc yeu cau dat lai mat khau cho tai khoan cua ban. Bam nut ben duoi de tiep tuc.
                </p>
                <div style="margin:28px 0;text-align:center;">
                  <a href="%s" style="display:inline-block;padding:14px 28px;border-radius:999px;background:linear-gradient(135deg,#16a34a 0%%,#0f766e 100%%);color:#ffffff;text-decoration:none;font-weight:700;font-size:15px;">
                    Dat Lai Mat Khau
                  </a>
                </div>
                <div style="margin:0 0 18px;padding:16px 18px;border-radius:14px;background:#f8fafc;border:1px solid #e2e8f0;">
                  <p style="margin:0 0 10px;color:#0f172a;font-size:14px;"><strong>Neu nut khong hoat dong, dung link sau:</strong></p>
                  <p style="margin:0 0 14px;word-break:break-all;color:#2563eb;font-size:13px;">%s</p>
                  <p style="margin:0 0 8px;color:#0f172a;font-size:14px;"><strong>Hoac dung token truc tiep:</strong></p>
                  <p style="margin:0;word-break:break-all;color:#334155;font-size:13px;">%s</p>
                </div>
                <p style="margin:0;color:#64748b;font-size:13px;line-height:1.7;">
                  Neu ban khong thuc hien yeu cau nay, vui long bo qua email nay. Mat khau hien tai cua ban se khong bi thay doi.
                </p>
                """.formatted(safeFullName, resetLink, resetLink, resetToken));
    }

    private String baseTemplate(String title, String subtitle, String bodyHtml) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>%s</title>
                </head>
                <body style="margin:0;padding:0;background:#eef4f1;font-family:'Segoe UI',Arial,sans-serif;">
                  <div style="padding:32px 16px;background:
                    radial-gradient(circle at top,#d9f99d 0%%,rgba(217,249,157,0) 26%%),
                    linear-gradient(180deg,#f8fafc 0%%,#eef4f1 100%%);">
                    <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" style="max-width:680px;margin:0 auto;">
                      <tr>
                        <td style="padding-bottom:18px;text-align:center;">
                          <div style="display:inline-block;padding:10px 18px;border-radius:999px;background:#ffffff;border:1px solid #dbe4dd;color:#166534;font-weight:800;letter-spacing:1px;font-size:12px;text-transform:uppercase;">
                            TreePlant
                          </div>
                        </td>
                      </tr>
                      <tr>
                        <td style="background:#ffffff;border:1px solid #dbe4dd;border-radius:28px;overflow:hidden;box-shadow:0 20px 60px rgba(15,23,42,0.08);">
                          <div style="padding:32px;background:
                            linear-gradient(135deg,#052e16 0%%,#166534 52%%,#15803d 100%%);
                            color:#ffffff;">
                            <div style="font-size:12px;letter-spacing:2px;text-transform:uppercase;opacity:0.78;margin-bottom:10px;">Secure Message</div>
                            <h1 style="margin:0 0 10px;font-size:30px;line-height:1.2;font-weight:800;">%s</h1>
                            <p style="margin:0;font-size:15px;line-height:1.7;color:rgba(255,255,255,0.86);">%s</p>
                          </div>
                          <div style="padding:30px 32px 12px;">
                            %s
                          </div>
                          <div style="padding:0 32px 28px;">
                            <div style="height:1px;background:#e2e8f0;margin-bottom:18px;"></div>
                            <p style="margin:0;color:#94a3b8;font-size:12px;line-height:1.7;">
                              Day la email tu dong tu he thong TreePlant. Vui long khong tra loi truc tiep email nay.
                            </p>
                          </div>
                        </td>
                      </tr>
                    </table>
                  </div>
                </body>
                </html>
                """.formatted(title, title, subtitle, bodyHtml);
    }
}
