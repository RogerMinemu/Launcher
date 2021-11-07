/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.*;
import com.skcraft.launcher.auth.microsoft.MinecraftServicesAuthorizer;
import com.skcraft.launcher.auth.microsoft.model.McProfileResponse;
import com.skcraft.launcher.auth.skin.MinecraftSkinService;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

/**
 * Creates authenticated sessions using the Mojang Yggdrasil login protocol.
 */
@RequiredArgsConstructor
public class YggdrasilLoginService implements LoginService {

    private final URL authUrl;
    private final String clientId;

    
    
    public Session login(String id)
            throws IOException, InterruptedException, AuthenticationException {
        AuthenticatePayload payload = new AuthenticatePayload(new Agent("Minecraft"), id, clientId);
        //AuthenticatePayload payload = new AuthenticatePayload(new Agent("Minecraft"), id, password, clientId);

        return call(id);
        //return call(this.authUrl, payload, null);
    }

    @Override
    public Session restore(SavedSession savedSession)
            throws IOException, InterruptedException, AuthenticationException {
        RefreshPayload payload = new RefreshPayload(savedSession.getAccessToken(), clientId);

        return call(savedSession.getUsername());
        //return call(new URL(this.authUrl, "/refresh"), payload, savedSession);
    }

    private Session call(String username) throws IOException, InterruptedException, AuthenticationException {
    	
    	ObjectMapper mapper = new ObjectMapper();
    	MojangUser userProfile;
    	try
    	{
    		userProfile = mapper.readValue(new URL("https://api.mojang.com/users/profiles/minecraft/" + username), MojangUser.class);
    	} catch(Exception e)
    	{
    		userProfile = mapper.readValue(new URL("https://api.mojang.com/users/profiles/minecraft/steve"), MojangUser.class);
    	}
    	
    	Profile perfil = new Profile();
    	perfil.name = username;
    	perfil.uuid = userProfile.getUuid();
    	perfil.legacy = true;
    	
    	BufferedImage bufferimage;
    	try {
    		bufferimage = ImageIO.read(new URL("https://minotar.net/avatar/" + perfil.name + "/32.png"));
    	}catch(Exception e)
    	{
    		bufferimage = ImageIO.read(new URL("https://minotar.net/avatar/steve/32.png"));
    	}
    	
    	//BufferedImage bufferimage = ImageIO.read(new URL("https://visage.surgeplay.com/face/32/" + perfil.uuid + ".png"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(bufferimage, "jpg", output );
        perfil.avatarImage = output.toByteArray();
        
        try
        {
        	URL murl = new URL("https://minemu.es/tools/api/mlauncher.php?u=" + username);
        	HttpRequest req = HttpRequest.get(murl).execute();
        } catch(Exception e)
        {
        	System.out.println("No hemos podido conectar con los servicios de MineMu :(");
        }

        return perfil;
        
    }
    
    /*
     * private Session call(URL url, Object payload, SavedSession previous)
            throws IOException, InterruptedException, AuthenticationException {
        HttpRequest req = HttpRequest
                .post(url)
                .bodyJson(payload)
                .execute();

        if (req.getResponseCode() != 200) {
            ErrorResponse error = req.returnContent().asJson(ErrorResponse.class);

            throw new AuthenticationException(error.getErrorMessage());
        } else {
            AuthenticateResponse response = req.returnContent().asJson(AuthenticateResponse.class);
            Profile profile = response.getSelectedProfile();

            if (previous != null && previous.getAvatarImage() != null) {
                profile.setAvatarImage(previous.getAvatarImage());
            } else {
                McProfileResponse skinProfile = MinecraftServicesAuthorizer
                        .getUserProfile("Bearer " + response.getAccessToken());

                profile.setAvatarImage(MinecraftSkinService.fetchSkinHead(skinProfile));
            }

            return profile;
        }
    }
     */

    @Data
    private static class Agent {
        private final String name;
        private final int version = 1;
    }

    @Data
    private static class AuthenticatePayload {
        private final Agent agent;
        private final String username;
        private final String clientToken;
    }

    @Data
    private static class RefreshPayload {
        private final String accessToken;
        private final String clientToken;
        private boolean requestUser = true;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AuthenticateResponse {
        private String accessToken;
        private String clientToken;
        @JsonManagedReference private Profile selectedProfile;
    }

    @Data
    private static class ErrorResponse {
        private String error;
        private String errorMessage;
        private String cause;
    }

    /**
     * Return in the list of available profiles.
     */
    @Data
    @ToString(exclude = "response")
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Profile implements Session {
        @JsonProperty("id") String uuid;
        String name;
        boolean legacy;
        public byte[] avatarImage;
        @JsonIgnore private final Map<String, String> userProperties = Collections.emptyMap();
        @JsonBackReference private AuthenticateResponse response;

        @Override
        @JsonIgnore
        public String getSessionToken() {
            return uuid;
        }

        @Override
        @JsonIgnore
        public String getAccessToken() {
            return uuid;
        }

        @Override
        @JsonIgnore
        public UserType getUserType() {
            return legacy ? UserType.LEGACY : UserType.MOJANG;
        }

        @Override
        public boolean isOnline() {
            return true;
        }
    }

}


