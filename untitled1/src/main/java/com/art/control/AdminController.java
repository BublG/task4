package com.art.control;

import com.art.entity.User;
import com.art.service.SessionService;
import com.art.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

@Controller
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    @GetMapping("/admin")
    public String userList(Model model, HttpSession session) {
        User user = (User) userService.loadUserByUsername(sessionService.getUsername(session.getId()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        user.setLastLoginDate(sdf.format(new Date()));
        userService.update(user);
        model.addAttribute("allUsers", userService.allUsers());
        model.addAttribute("usr", user.getUsername());
        return "admin";
    }

    @PostMapping("/admin")
    public String processUsers(@RequestParam Map<String, String> form,
                               HttpSession session, Model model) {
        System.out.println(form);
        Iterator<Map.Entry<String, String>> entries = form.entrySet().iterator();
        entries.next();
        boolean b = false;
        switch (entries.next().getKey()) {
            case "b":
                System.out.println("block");
                b = block(entries, session.getId());
                break;
            case "d":
                System.out.println("delete");
                b = delete(entries, session.getId());
                break;
            case "u":
                System.out.println("unblock");
                unblock(entries);
        }
        if (b) {
            return "redirect:/login";
        }
        model.addAttribute("allUsers", userService.allUsers());
        model.addAttribute("usr",
                userService.loadUserByUsername(sessionService.getUsername(session.getId())).getUsername());
        return "admin";
    }

    private boolean block(Iterator<Map.Entry<String, String>> entries, String sessionID) {
        boolean b = false;
        while (entries.hasNext()) {
            User user = userService.findUserById(Long.parseLong(entries.next().getKey()));
            user.setStatus("Blocked");
            userService.update(user);
            if (sessionService.expireUserSessions(user.getUsername(), sessionID)) {
                b = true;
            }
        }
        return b;
    }

    private void unblock(Iterator<Map.Entry<String, String>> entries) {
        while (entries.hasNext()) {
            User user = userService.findUserById(Long.parseLong(entries.next().getKey()));
            user.setStatus("Active");
            userService.update(user);
        }
    }

    private boolean delete(Iterator<Map.Entry<String, String>> entries, String sessionID) {
        boolean b = false;
        while (entries.hasNext()) {
            User user = userService.findUserById(Long.parseLong(entries.next().getKey()));
            userService.deleteUser(user.getId());
            if (sessionService.expireUserSessions(user.getUsername(), sessionID)) {
                b = true;
            }
        }
        return b;
    }
}
