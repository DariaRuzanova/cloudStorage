package com.example.cloudstorage.service;


import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.exception.SessionException;
import com.example.cloudstorage.model.AuthentificationRequest;
import com.example.cloudstorage.model.AuthentificationResponse;
import com.example.cloudstorage.model.Session;
import com.example.cloudstorage.repository.UserRepository;
import com.example.cloudstorage.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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

    public ResponseEntity<AuthentificationResponse> authentificationLogin(AuthentificationRequest authentificationRequest) {
        AuthentificationResponse response = null;
//        List<User>findUsers = userRepository.findAll();

        Optional<User> userFromDataBase = userRepository.findUserByLoginAndPassword(authentificationRequest.getLogin(),
                authentificationRequest.getPassword());
        if (userFromDataBase.isPresent()) {
            Session session = new Session(CommonUtils.createID(), userFromDataBase.get().getId());
            sessions.put(session.getId(), session);
            response = new AuthentificationResponse(session.getId());
            log.info("Пользователь "+authentificationRequest.getLogin()+" авторизован");
        } else {
            log.info("Ошибка авторизации");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok().body(response);
    }


    public ResponseEntity<Void> logout(String authToken) {
        Session sessionResult = sessions.getOrDefault(authToken,null);
        if(sessionResult!=null){
            sessions.remove(sessionResult.getId(),sessionResult);
        }
        else{
            throw new SessionException("Пользователь с таким логином не найден");

        }
        log.info("Пользователь "+authToken+" вышел из сессии");
        return ResponseEntity.ok().body(null);

    }
    public Session getSession(String authToken){
        return sessions.get(authToken);
    }
}
