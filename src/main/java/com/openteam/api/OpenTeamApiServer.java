package com.openteam.api;

import static spark.Spark.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openteam.model.Activity;
import com.openteam.model.Announcement;
import com.openteam.model.Deployment;
import com.openteam.model.TargetDate;
import com.openteam.repository.ActivityRepository;
import com.openteam.repository.AnnouncementRepository;
import com.openteam.repository.DeploymentRepository;
import com.openteam.repository.TargetDateRepository;

/**
 * Simple SparkJava based REST API exposing CRUD endpoints for the
 * existing repository layer. This allows a React front end to reuse
 * the same Java backend used by the JavaFX client.
 */
public class OpenTeamApiServer {
    public static void main(String[] args) {
        port(8080);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

        DeploymentRepository deploymentRepo = new DeploymentRepository();
        TargetDateRepository targetDateRepo = new TargetDateRepository();
        ActivityRepository activityRepo = new ActivityRepository();
        AnnouncementRepository announcementRepo = new AnnouncementRepository();

        // ----- Deployment endpoints -----
        get("/api/deployments", (req, res) -> {
            res.type("application/json");
            return mapper.writeValueAsString(deploymentRepo.findAll());
        });

        get("/api/deployments/:id", (req, res) -> {
            res.type("application/json");
            return deploymentRepo.findById(Long.valueOf(req.params(":id")))
                    .map(mapper::writeValueAsString)
                    .orElseGet(() -> {
                        res.status(404);
                        return "{}";
                    });
        });

        post("/api/deployments", (req, res) -> {
            Deployment deployment = mapper.readValue(req.body(), Deployment.class);
            Deployment saved = deploymentRepo.save(deployment);
            res.status(201);
            res.type("application/json");
            return mapper.writeValueAsString(saved);
        });

        put("/api/deployments/:id", (req, res) -> {
            Deployment deployment = mapper.readValue(req.body(), Deployment.class);
            deployment.setId(Long.valueOf(req.params(":id")));
            Deployment saved = deploymentRepo.save(deployment);
            res.type("application/json");
            return mapper.writeValueAsString(saved);
        });

        delete("/api/deployments/:id", (req, res) -> {
            deploymentRepo.deleteById(Long.valueOf(req.params(":id")));
            res.status(204);
            return "";
        });

        // ----- Target date endpoints -----
        get("/api/target-dates", (req, res) -> {
            res.type("application/json");
            return mapper.writeValueAsString(targetDateRepo.findAll());
        });

        get("/api/target-dates/:id", (req, res) -> {
            res.type("application/json");
            return targetDateRepo.findById(Long.valueOf(req.params(":id")))
                    .map(mapper::writeValueAsString)
                    .orElseGet(() -> {
                        res.status(404);
                        return "{}";
                    });
        });

        post("/api/target-dates", (req, res) -> {
            TargetDate targetDate = mapper.readValue(req.body(), TargetDate.class);
            TargetDate saved = targetDateRepo.save(targetDate);
            res.status(201);
            res.type("application/json");
            return mapper.writeValueAsString(saved);
        });

        put("/api/target-dates/:id", (req, res) -> {
            TargetDate targetDate = mapper.readValue(req.body(), TargetDate.class);
            targetDate.setId(Long.valueOf(req.params(":id")));
            TargetDate saved = targetDateRepo.save(targetDate);
            res.type("application/json");
            return mapper.writeValueAsString(saved);
        });

        delete("/api/target-dates/:id", (req, res) -> {
            targetDateRepo.deleteById(Long.valueOf(req.params(":id")));
            res.status(204);
            return "";
        });

        // ----- Activity endpoints -----
        get("/api/activities", (req, res) -> {
            res.type("application/json");
            return mapper.writeValueAsString(activityRepo.findAllActive());
        });

        get("/api/activities/:id", (req, res) -> {
            res.type("application/json");
            return activityRepo.findById(Long.valueOf(req.params(":id")))
                    .map(mapper::writeValueAsString)
                    .orElseGet(() -> {
                        res.status(404);
                        return "{}";
                    });
        });

        post("/api/activities", (req, res) -> {
            Activity activity = mapper.readValue(req.body(), Activity.class);
            Activity saved = activityRepo.save(activity);
            res.status(201);
            res.type("application/json");
            return mapper.writeValueAsString(saved);
        });

        put("/api/activities/:id", (req, res) -> {
            Activity activity = mapper.readValue(req.body(), Activity.class);
            activity.setId(Long.valueOf(req.params(":id")));
            Activity saved = activityRepo.save(activity);
            res.type("application/json");
            return mapper.writeValueAsString(saved);
        });

        delete("/api/activities/:id", (req, res) -> {
            activityRepo.deleteById(Long.valueOf(req.params(":id")));
            res.status(204);
            return "";
        });

        // ----- Announcement endpoints -----
        get("/api/announcements", (req, res) -> {
            res.type("application/json");
            return mapper.writeValueAsString(announcementRepo.findAllActive());
        });

        get("/api/announcements/:id", (req, res) -> {
            res.type("application/json");
            return announcementRepo.findById(Long.valueOf(req.params(":id")))
                    .map(mapper::writeValueAsString)
                    .orElseGet(() -> {
                        res.status(404);
                        return "{}";
                    });
        });

        post("/api/announcements", (req, res) -> {
            Announcement announcement = mapper.readValue(req.body(), Announcement.class);
            Announcement saved = announcementRepo.save(announcement);
            res.status(201);
            res.type("application/json");
            return mapper.writeValueAsString(saved);
        });

        put("/api/announcements/:id", (req, res) -> {
            Announcement announcement = mapper.readValue(req.body(), Announcement.class);
            announcement.setId(Long.valueOf(req.params(":id")));
            Announcement saved = announcementRepo.save(announcement);
            res.type("application/json");
            return mapper.writeValueAsString(saved);
        });

        delete("/api/announcements/:id", (req, res) -> {
            announcementRepo.deleteById(Long.valueOf(req.params(":id")));
            res.status(204);
            return "";
        });
    }
}
