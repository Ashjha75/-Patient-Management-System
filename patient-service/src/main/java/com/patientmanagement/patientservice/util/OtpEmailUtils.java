package com.patientmanagement.patientservice.util;

public class OtpEmailUtils {

    // Generates a random 6-digit OTP as a String
    public static String generateOtp() {
        int otp = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }

    // Builds a styled HTML email body with the given OTP
    public static String buildOtpEmailHtml(String otp) {
        return """
                <html>
                <head>
                    <style>
                        .container {
                            max-width: 400px;
                            margin: auto;
                            padding: 24px;
                            background: #f9f9f9;
                            border-radius: 8px;
                            font-family: Arial, sans-serif;
                            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                        }
                        .otp {
                            font-size: 2em;
                            color: #007bff;
                            letter-spacing: 8px;
                            font-weight: bold;
                            margin: 16px 0;
                        }
                        .footer {
                            font-size: 0.9em;
                            color: #888;
                            margin-top: 24px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>Welcome to Patient Management System!</h2>
                        <p>Your One-Time Password (OTP) for registration is:</p>
                        <div class="otp">%s</div>
                        <p>Please enter this OTP to complete your registration. This code is valid for 10 minutes.</p>
                        <div class="footer">
                            If you did not request this, please ignore this email.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(otp);
    }
}