/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.*;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Creates authenticated sessions using the Mojang Yggdrasil login protocol.
 */
@RequiredArgsConstructor
public class YggdrasilLoginService implements LoginService {

    private final URL authUrl;
    private final String clientId;

    public Session login(String id)
            throws IOException, InterruptedException, AuthenticationException {
        //AuthenticatePayload payload = new AuthenticatePayload(new Agent("Minecraft"), id, clientId);

        return call(id);
    }

    @Override
    public Session restore(SavedSession savedSession)
    {
        return call(savedSession.getUsername());
    }

    private Session call(String id)
    {
    	//bufferimage = ImageIO.read(new URL("https://minotar.net/avatar/" + perfil.name + "/32.png"));
    	
    	Profile profile = new Profile();
    	profile.uuid = "2e9d879a3d9541c7bda9d587e64a86c8";
    	profile.name = id;
    	profile.legacy = false;
    	
    	try{
    		//https://minemu.es/tools/api/mlauncher.php?u=alextititoto
    		URL mapi = new URL("https://minemu.es/tools/api/mlauncher.php?u=" + id);
    		mapi.openConnection();
    		
    		BufferedImage bufferimage = ImageIO.read(new URL("https://minotar.net/avatar/" + id + "/32.png"));
        	ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(bufferimage, "jpg", output );
            profile.avatarImage  = output.toByteArray();
            
            System.out.println("CORRECTO, RETURN PROFILE");
        	
            return profile;    		
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	System.out.println("ERROR, RETURN NO IMAGE");
    	return profile;
    	/*
        if (req.getResponseCode() != 200) {
            ErrorResponse error = req.returnContent().asJson(ErrorResponse.class);

            throw new AuthenticationException(error.getErrorMessage(), true);
        } else {
            AuthenticateResponse response = req.returnContent().asJson(AuthenticateResponse.class);
            Profile profile = response.getSelectedProfile();

            if (profile == null) return null; // Minecraft not owned

            if (previous != null && previous.getAvatarImage() != null) {
                profile.setAvatarImage(previous.getAvatarImage());
            }

            // DEPRECEATION: minecraft services API no longer accepts yggdrasil tokens
            // login still works though. until it doesn't, this class will remain

            return profile;
        }
        */
    }

    @Data
    private static class Agent {
        private final String name;
        private final int version = 1;
    }

    @Data
    private static class AuthenticatePayload {
        private final Agent agent;
        private final String username;
        private final String password;
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
    private static class Profile implements Session {
        @JsonProperty("id") private String uuid;
        private String name;
        private boolean legacy;
        private byte[] avatarImage;
        @JsonIgnore private final Map<String, String> userProperties = Collections.emptyMap();
        @JsonBackReference private AuthenticateResponse response;

        @Override
        @JsonIgnore
        public String getSessionToken() {
        	return "xddd";
        }

        @Override
        @JsonIgnore
        public String getAccessToken() {
            return "xddd";
        }

        @Override
        @JsonIgnore
        public UserType getUserType() {
            return UserType.MOJANG;
        }

        @Override
        public boolean isOnline() {
            return true;
        }
    }

}
