package com.tk.fcmb.utils;

import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Repositories.UserRepository;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class GetAuthenticatedUser {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;


    public User getAuthenticatedUser(HttpServletRequest request){
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() != null){
                return userRepository.findByEmail(username);
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    public String getUserToken(HttpServletRequest request){
        final String authorizationHeader = request.getHeader("Authorization");

        String jwt;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        }else{
            return null;
        }
        return jwt;
    }

}
