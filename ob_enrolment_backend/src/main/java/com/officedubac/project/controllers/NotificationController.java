package com.officedubac.project.controllers;

import com.officedubac.project.models.Notification;
import com.officedubac.project.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name="File Controller", description = "Endpoints responsables de la gestion des fichiers & documents")
public class NotificationController
{
    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/unseen")
    public List<Notification> getUnseenNotifications()
    {
        return notificationRepository.findBySeenFalse();
    }

    @PostMapping("/{id}/seen")
    public void markAsSeen(@PathVariable String id)
    {
        notificationRepository.findById(id).ifPresent(n -> {
            //n.setSeen(true);
            notificationRepository.delete(n);
        });
    }
}
