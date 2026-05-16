package com.oiloncanvas.backend.dto.canvasapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * maps to the Canvas LMS User object returned by 
 * GET /api/v1/users/self.
 * https://developerdocs.instructure.com/services/canvas/resources/users
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CanvasUser {

    // only some fields are here, if need others, find them at the link

    /** The user's unique ID in Canvas. */
    private long id;

    /** Full display name. */
    private String name;

    /** Shortened name  */
    @JsonProperty("short_name")
    private String shortName;

    /** Login username */
    @JsonProperty("login_id")
    private String loginId;

    /** Email address. */
    private String email;

    /** Link to the user's profile picture. */
    @JsonProperty("avatar_url")
    private String avatarUrl;

    public CanvasUser() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
