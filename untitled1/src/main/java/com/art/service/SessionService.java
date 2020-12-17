package com.art.service;

import com.art.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service()
public class SessionService {

    private SessionRegistry sessionRegistry;

    @Autowired
    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public boolean expireUserSessions(String username, String sessionID) {
        boolean b = false;
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof User) {
                UserDetails userDetails = (UserDetails) principal;
                if (userDetails.getUsername().equals(username)) {
                    for (SessionInformation information : sessionRegistry
                            .getAllSessions(userDetails, true)) {
                        if (information.getSessionId().equals(sessionID)) {
                            b = true;
                        }
                        information.expireNow();
                        killExpiredSessionForSure(information.getSessionId());
                    }
                }
            }
        }
        return b;
    }

    public String getUsername(String sessionID) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof User) {
                UserDetails userDetails = (UserDetails) principal;
                for (SessionInformation information : sessionRegistry
                        .getAllSessions(userDetails, true)) {
                    if (information.getSessionId().equals(sessionID)) {
                        return userDetails.getUsername();
                    }
                }
            }
        }
        return null;
    }

    public void killExpiredSessionForSure(String id) {
        try {
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Cookie", "JSESSIONID=" + id);
            HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
            RestTemplate rt = new RestTemplate();
            rt.exchange("https://first-spring-app1.herokuapp.com/login", HttpMethod.GET,
                    requestEntity, String.class);
        } catch (Exception ex) {
        }
    }
}
