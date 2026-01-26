package com.unimailhub.backend.service;

import com.unimailhub.backend.entity.Job;
import com.unimailhub.backend.entity.Mail;
import com.unimailhub.backend.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // ✅ Create job from email (simple rule-based detection)
    public void processEmailForJob(Mail mail) {
        String subject = mail.getSubject().toLowerCase();
        String content = mail.getMessage().toLowerCase();

        // Simple rule-based detection for job-related emails
        boolean isJobEmail = isJobRelatedEmail(subject, content);

        if (isJobEmail) {
            Job job = extractJobFromEmail(mail);
            if (job != null) {
                // Check if job already exists (avoid duplicates)
                if (!jobExists(job, mail.getToEmail())) {
                    jobRepository.save(job);
                }
            }
        }
    }

    // ✅ Simple rule-based job detection
    private boolean isJobRelatedEmail(String subject, String content) {
        String[] jobKeywords = {
            "job", "position", "opening", "vacancy", "career", "opportunity",
            "hiring", "recruitment", "application", "interview", "software",
            "engineer", "developer", "analyst", "manager", "internship"
        };

        for (String keyword : jobKeywords) {
            if (subject.contains(keyword) || content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // ✅ Extract job information from email
    private Job extractJobFromEmail(Mail mail) {
        String subject = mail.getSubject();
        String content = mail.getMessage();

        Job job = new Job();
        job.setSourceEmail(mail);
        job.setStatus("NEW");

        // Extract job title from subject or content
        job.setJobTitle(extractJobTitle(subject, content));

        // Extract company name
        job.setCompanyName(extractCompanyName(subject, content, mail.getFromEmail()));

        // Determine job type
        job.setJobType(determineJobType(subject, content));

        // Extract application link
        job.setApplicationLink(extractApplicationLink(content));

        // Set date posted (use email received date)
        job.setDatePosted(LocalDateTime.now());

        return job;
    }

    // ✅ Extract job title
    private String extractJobTitle(String subject, String content) {
        // Try to find job title patterns
        Pattern pattern = Pattern.compile("(?:position|job|role)(?:\\s+of\\s+|:\\s*|\\s+for\\s+|\\s*-\\s*)([^\\n,.]{10,50})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(subject + " " + content);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // Fallback: use subject if it looks like a job title
        if (subject.length() > 5 && subject.length() < 50) {
            return subject;
        }

        return "Job Opportunity";
    }

    // ✅ Extract company name
    private String extractCompanyName(String subject, String content, String fromEmail) {
        // Try to extract from email domain
        if (fromEmail.contains("@")) {
            String domain = fromEmail.split("@")[1];
            if (domain.contains(".")) {
                String company = domain.split("\\.")[0];
                return capitalizeFirstLetter(company);
            }
        }

        // Try to find company name patterns
        Pattern pattern = Pattern.compile("(?:at|with|from)\\s+([A-Za-z][A-Za-z0-9\\s&]{2,30})(?:\\s|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return "Company";
    }

    // ✅ Determine job type
    private String determineJobType(String subject, String content) {
        String text = (subject + " " + content).toLowerCase();

        if (text.contains("internship") || text.contains("intern")) {
            return "Internship";
        } else if (text.contains("contract") || text.contains("freelance") || text.contains("temporary")) {
            return "Contract";
        } else {
            return "Full-time";
        }
    }

    // ✅ Extract application link
    private String extractApplicationLink(String content) {
        // Simple URL extraction
        Pattern pattern = Pattern.compile("https?://[^\\s<>\"']+");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(0);
        }

        return null;
    }

    // ✅ Check if job already exists
    private boolean jobExists(Job job, String userEmail) {
        List<Job> existingJobs = jobRepository.findByUserEmail(userEmail);
        for (Job existing : existingJobs) {
            if (existing.getJobTitle().equalsIgnoreCase(job.getJobTitle()) &&
                existing.getCompanyName().equalsIgnoreCase(job.getCompanyName()) &&
                existing.getSourceEmail().getId().equals(job.getSourceEmail().getId())) {
                return true;
            }
        }
        return false;
    }

    // ✅ Get user's jobs
    public List<Job> getUserJobs(String email) {
        return jobRepository.findByUserEmail(email);
    }

    // ✅ Search and filter jobs
    public List<Job> searchJobs(String email, String keyword, String jobType, String dateFilter) {
        LocalDateTime startDate = null;

        if ("24h".equals(dateFilter)) {
            startDate = LocalDateTime.now().minusHours(24);
        } else if ("7d".equals(dateFilter)) {
            startDate = LocalDateTime.now().minusDays(7);
        }

        if ((keyword == null || keyword.trim().isEmpty()) &&
            (jobType == null || jobType.trim().isEmpty()) &&
            startDate == null) {
            return jobRepository.findByUserEmail(email);
        }

        return jobRepository.findByUserEmailWithFilters(email, keyword, jobType, startDate);
    }

    // ✅ Apply to job
    public boolean applyToJob(Long jobId, String userEmail) {
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job != null && job.getSourceEmail().getToEmail().equals(userEmail)) {
            job.setStatus("APPLIED");
            jobRepository.save(job);
            return true;
        }
        return false;
    }

    // ✅ Get job by ID
    public Job getJobById(Long jobId) {
        return jobRepository.findById(jobId).orElse(null);
    }

    // ✅ Helper method
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}