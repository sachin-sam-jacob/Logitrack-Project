package com.edutech.logisticsmanagementandtrackingsystem.service;

import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    /**
     * Send tracking email to customer when cargo is created
     */
    public void sendTrackingEmail(Cargo cargo) {
        if (cargo.getCustomerEmail() == null || cargo.getCustomerEmail().trim().isEmpty()) {
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(cargo.getCustomerEmail());
            helper.setSubject("LogiTrack | Your Cargo Tracking Number");

            String htmlContent = buildTrackingEmail(cargo);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to send tracking email", e);
        }
    }

    /**
     * NEW: Send delivery OTP to customer
     */
    public void sendDeliveryOtpEmail(Cargo cargo, String otp) {
        if (cargo.getCustomerEmail() == null || cargo.getCustomerEmail().trim().isEmpty()) {
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(cargo.getCustomerEmail());
            helper.setSubject("LogiTrack | Delivery Verification OTP");

            String htmlContent = buildOtpEmail(cargo, otp);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
        }
    }

    public void sendCargoAssignmentEmail(Cargo cargo, Driver driver) {
        if (driver.getEmail() == null || driver.getEmail().trim().isEmpty()) {
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(driver.getEmail());
            helper.setSubject("LogiTrack | New Cargo Assignment");

            String htmlContent = buildAssignmentEmail(cargo, driver);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            
        } catch (Exception e) {
            System.err.println("Error sending assignment email: " + e.getMessage());
        }
    }

    public void sendCargoAcceptedEmail(Cargo cargo) {
        // Send to BOTH customer and business
        if (cargo.getCustomerEmail() != null && !cargo.getCustomerEmail().trim().isEmpty()) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                
                helper.setTo(cargo.getCustomerEmail());
                helper.setSubject("LogiTrack | Driver Accepted Your Cargo");

                String htmlContent = buildAcceptedEmailForCustomer(cargo);
                helper.setText(htmlContent, true);
                
                mailSender.send(mimeMessage);
                
            } catch (Exception e) {
                System.err.println("Error sending accepted email to customer: " + e.getMessage());
            }
        }
        
        // ADDED: Send to business
        if (cargo.getBusiness() != null && cargo.getBusiness().getEmail() != null) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                
                helper.setTo(cargo.getBusiness().getEmail());
                helper.setSubject("LogiTrack | Driver Accepted Cargo Assignment");

                String htmlContent = buildAcceptedEmailForBusiness(cargo);
                helper.setText(htmlContent, true);
                
                mailSender.send(mimeMessage);
                
            } catch (Exception e) {
                System.err.println("Error sending accepted email to business: " + e.getMessage());
            }
        }
    }

    public void sendCargoRejectedEmail(Cargo cargo, String reason) {
        if (cargo.getBusiness() == null || cargo.getBusiness().getEmail() == null) {
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(cargo.getBusiness().getEmail());
            helper.setSubject("LogiTrack | Cargo Assignment Rejected");

            String htmlContent = buildRejectedEmail(cargo, reason);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            
        } catch (Exception e) {
            System.err.println("Error sending rejection email: " + e.getMessage());
        }
    }

    public void sendStatusUpdateEmail(Cargo cargo) {
        if (cargo.getCustomerEmail() == null || cargo.getCustomerEmail().trim().isEmpty()) {
            return;
        }

        // Don't send status updates for ASSIGNED, ACCEPTED, REJECTED
        if ("ASSIGNED".equals(cargo.getStatus()) || 
            "ACCEPTED".equals(cargo.getStatus()) || 
            "REJECTED".equals(cargo.getStatus())) {
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(cargo.getCustomerEmail());
            helper.setSubject("LogiTrack | Cargo Status Updated");

            String htmlContent = buildStatusUpdateEmail(cargo);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            
        } catch (Exception e) {
            System.err.println("Error sending status update email: " + e.getMessage());
        }
    }

    /**
     * NEW: Send delivery completion notification to business
     */
    public void sendDeliveryCompletionToBusiness(Cargo cargo) {
        if (cargo.getBusiness() == null || cargo.getBusiness().getEmail() == null) {
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(cargo.getBusiness().getEmail());
            helper.setSubject("LogiTrack | Cargo Delivered Successfully");

            String htmlContent = buildDeliveryCompletionEmail(cargo);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            
        } catch (Exception e) {
            System.err.println("Error sending delivery completion email to business: " + e.getMessage());
        }
    }

    // ==================== EMAIL TEMPLATES ====================

    private String buildOtpEmail(Cargo cargo, String otp) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='margin:0; padding:0; font-family: Arial, sans-serif;'>" +
            "<div style='background:#f4f6f8; padding:20px;'>" +
            "<div style='max-width:600px; background:#ffffff; padding:30px; margin:auto; border-radius:8px;'>" +
            "<h2 style='color:#2c3e50; margin-top:0;'>🔒 Delivery Verification OTP</h2>" +
            "<p>Hello <strong>" + cargo.getCustomerName() + "</strong>,</p>" +
            "<p>Your cargo is ready for delivery. Please share this OTP with the driver to confirm delivery:</p>" +
            "<div style='background:#e8f4f8; padding:30px; border-radius:8px; margin:20px 0; text-align:center;'>" +
            "<h1 style='color:#3498db; font-size:48px; margin:0; letter-spacing:8px;'>" + otp + "</h1>" +
            "<p style='color:#7f8c8d; margin-top:10px;'>This OTP is valid for this delivery only</p>" +
            "</div>" +
            "<div style='background:#fff3cd; padding:15px; border-left:4px solid #f59e0b; border-radius:4px; margin:20px 0;'>" +
            "<p style='margin:0; color:#856404;'><strong>⚠️ Security Note:</strong> Only share this OTP with your driver upon receiving the cargo.</p>" +
            "</div>" +
            "<p><strong>Cargo Details:</strong></p>" +
            "<ul>" +
            "<li>Tracking Number: " + cargo.getTrackingNumber() + "</li>" +
            "<li>Content: " + cargo.getContent() + "</li>" +
            "<li>Driver: " + (cargo.getDriver() != null ? cargo.getDriver().getName() : "N/A") + "</li>" +
            "</ul>" +
            "<hr style='border:none; border-top:1px solid #e5e7eb; margin:20px 0;'/>" +
            "<p style='font-size:12px; color:#aaa; text-align:center;'>© LogiTrack | Smart Logistics Platform</p>" +
            "</div></div>" +
            "</body></html>";
    }

    private String buildTrackingEmail(Cargo cargo) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='margin:0; padding:0; font-family: Arial, sans-serif;'>" +
            "<div style='background:#f4f6f8; padding:20px;'>" +
            "<div style='max-width:600px; background:#ffffff; padding:30px; margin:auto; border-radius:8px;'>" +
            "<h2 style='color:#2c3e50; margin-top:0;'>📦 Your Cargo Has Been Created</h2>" +
            "<p>Hello <strong>" + cargo.getCustomerName() + "</strong>,</p>" +
            "<p>Your cargo has been successfully created with LogiTrack.</p>" +
            "<div style='background:#e8f4f8; padding:20px; border-radius:8px; margin:20px 0;'>" +
            "<h3 style='margin-top:0; color:#2c3e50;'>Tracking Details</h3>" +
            "<p><strong>Tracking Number:</strong><br/>" +
            "<span style='font-size:24px; color:#3498db; font-weight:700;'>" + 
            (cargo.getTrackingNumber() != null ? cargo.getTrackingNumber() : "Generating...") + 
            "</span></p>" +
            "<p><strong>Content:</strong> " + cargo.getContent() + "</p>" +
            "<p><strong>From:</strong> " + cargo.getSourceLocation() + "</p>" +
            "<p><strong>To:</strong> " + cargo.getDestinationLocation() + "</p>" +
            "<p><strong>Status:</strong> <span style='color:#27ae60; font-weight:600;'>" + 
            cargo.getStatus() + "</span></p>" +
            "</div>" +
            "<p>You can track your cargo anytime using this tracking number.</p>" +
            "<hr style='border:none; border-top:1px solid #e5e7eb; margin:20px 0;'/>" +
            "<p style='font-size:12px; color:#aaa; text-align:center;'>© LogiTrack | Smart Logistics Platform</p>" +
            "</div></div>" +
            "</body></html>";
    }

    private String buildAssignmentEmail(Cargo cargo, Driver driver) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='margin:0; padding:0; font-family: Arial, sans-serif;'>" +
            "<div style='background:#f4f6f8; padding:20px;'>" +
            "<div style='max-width:600px; background:#ffffff; padding:30px; margin:auto; border-radius:8px;'>" +
            "<h2 style='color:#2c3e50; margin-top:0;'>🚚 New Cargo Assignment</h2>" +
            "<p>Hello <strong>" + driver.getName() + "</strong>,</p>" +
            "<p>A new cargo has been assigned to you.</p>" +
            "<div style='background:#fff3cd; padding:20px; border-radius:8px; margin:20px 0;'>" +
            "<h3 style='margin-top:0; color:#856404;'>Cargo Details</h3>" +
            "<p><strong>Tracking Number:</strong> " + cargo.getTrackingNumber() + "</p>" +
            "<p><strong>Content:</strong> " + cargo.getContent() + "</p>" +
            "<p><strong>Pickup Location:</strong> " + cargo.getSourceLocation() + "</p>" +
            "<p><strong>Delivery Location:</strong> " + cargo.getDestinationLocation() + "</p>" +
            "<p><strong>Customer:</strong> " + cargo.getCustomerName() + 
            " (" + cargo.getCustomerContact() + ")</p>" +
            "</div>" +
            "<p style='background:#fef3c7; padding:12px; border-left:4px solid #f59e0b; border-radius:4px;'>" +
            "<strong>⚠️ Action Required:</strong> Please login to your account to accept or reject this assignment.</p>" +
            "<hr style='border:none; border-top:1px solid #e5e7eb; margin:20px 0;'/>" +
            "<p style='font-size:12px; color:#aaa; text-align:center;'>© LogiTrack | Smart Logistics Platform</p>" +
            "</div></div>" +
            "</body></html>";
    }

    private String buildAcceptedEmailForCustomer(Cargo cargo) {
        String driverName = cargo.getDriver() != null ? cargo.getDriver().getName() : "Driver";
        String driverContact = cargo.getDriver() != null ? cargo.getDriver().getContactNumber() : "N/A";
        
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='margin:0; padding:0; font-family: Arial, sans-serif;'>" +
            "<div style='background:#f4f6f8; padding:20px;'>" +
            "<div style='max-width:600px; background:#ffffff; padding:30px; margin:auto; border-radius:8px;'>" +
            "<h2 style='color:#2c3e50; margin-top:0;'>✅ Driver Accepted Your Cargo</h2>" +
            "<p>Hello <strong>" + cargo.getCustomerName() + "</strong>,</p>" +
            "<p>Great news! Your cargo has been accepted by the driver.</p>" +
            "<div style='background:#d4edda; padding:20px; border-radius:8px; margin:20px 0;'>" +
            "<h3 style='margin-top:0; color:#155724;'>Delivery Information</h3>" +
            "<p><strong>Tracking Number:</strong> " + cargo.getTrackingNumber() + "</p>" +
            "<p><strong>Driver:</strong> " + driverName + "</p>" +
            "<p><strong>Contact:</strong> " + driverContact + "</p>" +
            "<p><strong>Status:</strong> <span style='color:#27ae60; font-weight:600;'>ACCEPTED</span></p>" +
            "</div>" +
            "<p>Your cargo will be picked up soon. You will receive an OTP when it's out for delivery.</p>" +
            "<hr style='border:none; border-top:1px solid #e5e7eb; margin:20px 0;'/>" +
            "<p style='font-size:12px; color:#aaa; text-align:center;'>© LogiTrack | Smart Logistics Platform</p>" +
            "</div></div>" +
            "</body></html>";
    }

    // ADDED: Acceptance email for business
    private String buildAcceptedEmailForBusiness(Cargo cargo) {
        String driverName = cargo.getDriver() != null ? cargo.getDriver().getName() : "Driver";
        
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='margin:0; padding:0; font-family: Arial, sans-serif;'>" +
            "<div style='background:#f4f6f8; padding:20px;'>" +
            "<div style='max-width:600px; background:#ffffff; padding:30px; margin:auto; border-radius:8px;'>" +
            "<h2 style='color:#2c3e50; margin-top:0;'>✅ Driver Accepted Cargo Assignment</h2>" +
            "<p>Hello,</p>" +
            "<p>The driver has accepted the cargo assignment.</p>" +
            "<div style='background:#d4edda; padding:20px; border-radius:8px; margin:20px 0;'>" +
            "<h3 style='margin-top:0; color:#155724;'>Assignment Details</h3>" +
            "<p><strong>Tracking Number:</strong> " + cargo.getTrackingNumber() + "</p>" +
            "<p><strong>Content:</strong> " + cargo.getContent() + "</p>" +
            "<p><strong>Driver:</strong> " + driverName + "</p>" +
            "<p><strong>Customer:</strong> " + cargo.getCustomerName() + "</p>" +
            "<p><strong>Route:</strong> " + cargo.getSourceLocation() + " → " + cargo.getDestinationLocation() + "</p>" +
            "</div>" +
            "<p>The cargo is now in the driver's possession and on its way to the customer.</p>" +
            "<hr style='border:none; border-top:1px solid #e5e7eb; margin:20px 0;'/>" +
            "<p style='font-size:12px; color:#aaa; text-align:center;'>© LogiTrack | Smart Logistics Platform</p>" +
            "</div></div>" +
            "</body></html>";
    }

    // ADDED: Delivery completion email for business
    private String buildDeliveryCompletionEmail(Cargo cargo) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='margin:0; padding:0; font-family: Arial, sans-serif;'>" +
            "<div style='background:#f4f6f8; padding:20px;'>" +
            "<div style='max-width:600px; background:#ffffff; padding:30px; margin:auto; border-radius:8px;'>" +
            "<h2 style='color:#2c3e50; margin-top:0;'>🎉 Cargo Delivered Successfully</h2>" +
            "<p>Hello,</p>" +
            "<p>Great news! Your cargo has been successfully delivered and verified by the customer.</p>" +
            "<div style='background:#d1fae5; padding:20px; border-radius:8px; margin:20px 0;'>" +
            "<h3 style='margin-top:0; color:#065f46;'>Delivery Confirmation</h3>" +
            "<p><strong>Tracking Number:</strong> " + cargo.getTrackingNumber() + "</p>" +
            "<p><strong>Content:</strong> " + cargo.getContent() + "</p>" +
            "<p><strong>Customer:</strong> " + cargo.getCustomerName() + "</p>" +
            "<p><strong>Delivered To:</strong> " + cargo.getDestinationLocation() + "</p>" +
            "<p><strong>Delivery Date:</strong> " + (cargo.getActualDeliveryDate() != null ? cargo.getActualDeliveryDate() : "Just now") + "</p>" +
            "<p><strong>Status:</strong> <span style='color:#059669; font-weight:700;'>✅ DELIVERED & VERIFIED</span></p>" +
            "</div>" +
            "<p>The customer has verified the delivery with their OTP. This shipment is now complete.</p>" +
            "<hr style='border:none; border-top:1px solid #e5e7eb; margin:20px 0;'/>" +
            "<p style='font-size:12px; color:#aaa; text-align:center;'>© LogiTrack | Smart Logistics Platform</p>" +
            "</div></div>" +
            "</body></html>";
    }

    private String buildRejectedEmail(Cargo cargo, String reason) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='margin:0; padding:0; font-family: Arial, sans-serif;'>" +
            "<div style='background:#f4f6f8; padding:20px;'>" +
            "<div style='max-width:600px; background:#ffffff; padding:30px; margin:auto; border-radius:8px;'>" +
            "<h2 style='color:#e74c3c; margin-top:0;'>❌ Cargo Assignment Rejected</h2>" +
            "<p>The driver has rejected the cargo assignment.</p>" +
            "<div style='background:#f8d7da; padding:20px; border-radius:8px; margin:20px 0;'>" +
            "<p><strong>Tracking Number:</strong> " + cargo.getTrackingNumber() + "</p>" +
            "<p><strong>Content:</strong> " + cargo.getContent() + "</p>" +
            "<p><strong>Reason:</strong> " + (reason != null ? reason : "Not specified") + "</p>" +
            "</div>" +
            "<p>Please assign this cargo to another driver.</p>" +
            "<hr style='border:none; border-top:1px solid #e5e7eb; margin:20px 0;'/>" +
            "<p style='font-size:12px; color:#aaa; text-align:center;'>© LogiTrack | Smart Logistics Platform</p>" +
            "</div></div>" +
            "</body></html>";
    }

    private String buildStatusUpdateEmail(Cargo cargo) {
        String statusMessage = "";
        
        switch (cargo.getStatus()) {
            case "PICKED_UP":
                statusMessage = "Your cargo has been picked up by the driver.";
                break;
            case "IN_TRANSIT":
                statusMessage = "Your cargo is now in transit. You will receive an OTP shortly for delivery verification.";
                break;
            case "AWAITING_OTP":
                statusMessage = "Your cargo is ready for delivery. Please check your email for the delivery OTP.";
                break;
            case "DELIVERED":
                statusMessage = "Your cargo has been successfully delivered!";
                break;
            default:
                statusMessage = "Your cargo status has been updated.";
        }
        
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='margin:0; padding:0; font-family: Arial, sans-serif;'>" +
            "<div style='background:#f4f6f8; padding:20px;'>" +
            "<div style='max-width:600px; background:#ffffff; padding:30px; margin:auto; border-radius:8px;'>" +
            "<h2 style='color:#2c3e50; margin-top:0;'>📍 Cargo Status Updated</h2>" +
            "<p>Hello <strong>" + cargo.getCustomerName() + "</strong>,</p>" +
            "<p>" + statusMessage + "</p>" +
            "<div style='background:#e8f4f8; padding:20px; border-radius:8px; margin:20px 0;'>" +
            "<p><strong>Tracking Number:</strong> " + cargo.getTrackingNumber() + "</p>" +
            "<p><strong>New Status:</strong><br/>" +
            "<span style='color:#3498db; font-size:18px; font-weight:600;'>" + 
            cargo.getStatus() + "</span></p>" +
            (cargo.getDeliveryNotes() != null ? 
                "<p><strong>Notes:</strong> " + cargo.getDeliveryNotes() + "</p>" : "") +
            "</div>" +
            "<p>Track your cargo anytime using the tracking number above.</p>" +
            "<hr style='border:none; border-top:1px solid #e5e7eb; margin:20px 0;'/>" +
            "<p style='font-size:12px; color:#aaa; text-align:center;'>© LogiTrack | Smart Logistics Platform</p>" +
            "</div></div>" +
            "</body></html>";
    }
}