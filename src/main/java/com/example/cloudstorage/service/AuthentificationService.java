package com.example.cloudstorage.service;

import com.example.cloudstorage.DTO.AuthentificationRequest;
import com.example.cloudstorage.DTO.AuthentificationResponse;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.model.Session;
import com.example.cloudstorage.repository.UserRepository;
import com.example.cloudstorage.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service

@Slf4j
public class AuthentificationService {
    private final ConcurrentMap<String, Session> sessions;
    private final UserRepository userRepository;

    public AuthentificationService(UserRepository userRepository) {
        this.sessions = new ConcurrentHashMap<>();
        this.userRepository = userRepository;
    }

    public AuthentificationResponse authentificationLogin(AuthentificationRequest authentificationRequest) {
        AuthentificationResponse response;

        Optional<User> userFromDataBase = userRepository.findUserByLoginAndPassword(authentificationRequest.getLogin(),
                authentificationRequest.getPassword());
        if (userFromDataBase.isPresent()) {
            Session session = new Session(CommonUtils.createID(), userFromDataBase.get().getId());
            sessions.put(session.getId(), session);
            response = new AuthentificationResponse(session.getId());
            log.info("Пользователь " + authentificationRequest.getLogin() + " авторизован");
        } else {
            log.error("Ошибка авторизации");
            response = null;
        }
        return response;
    }


    public boolean logout(String authToken) {
        Session sessionResult = sessions.getOrDefault(authToken, null);
        boolean flag;
        if (sessionResult != null) {
            sessions.remove(sessionResult.getId(), sessionResult);
            flag = true;
            log.info("Пользователь " + authToken + " вышел из сессии");
        } else {
            log.warn("В сессии нет такого пользователя!");
            flag = false;
        }
        return flag;

    }

    public Session getSession(String authToken) {
        return sessions.get(authToken);
    }
}
