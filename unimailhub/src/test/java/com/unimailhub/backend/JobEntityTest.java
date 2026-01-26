package com.unimailhub.backend;

import com.unimailhub.backend.entity.Job;
import com.unimailhub.backend.entity.Mail;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class JobEntityTest {

    @Test
    public void testJobEntityFields() {
        Job job = new Job();

        // Test setting and getting jobTitle
        job.setJobTitle("Software Engineer");
        assertEquals("Software Engineer", job.getJobTitle());

        // Test setting and getting companyName
        job.setCompanyName("Tech Corp");
        assertEquals("Tech Corp", job.getCompanyName());

        // Test setting and getting jobType
        job.setJobType("Full-time");
        assertEquals("Full-time", job.getJobType());

        // Test setting and getting datePosted
        LocalDateTime now = LocalDateTime.now();
        job.setDatePosted(now);
        assertEquals(now, job.getDatePosted());

        // Test setting and getting applicationLink
        job.setApplicationLink("https://example.com/apply");
        assertEquals("https://example.com/apply", job.getApplicationLink());

        // Test setting and getting status
        job.setStatus("APPLIED");
        assertEquals("APPLIED", job.getStatus());

        // Test sourceEmail relationship
        Mail mail = new Mail();
        job.setSourceEmail(mail);
        assertEquals(mail, job.getSourceEmail());

        System.out.println("✅ All Job entity fields are accessible and working correctly!");
    }
}