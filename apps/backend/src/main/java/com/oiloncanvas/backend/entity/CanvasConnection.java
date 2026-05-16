package com.oiloncanvas.backend.entity;
import jakarta.persistence.*;

/**
 * Represents relationship between Oil on Canvas user and a canvas instance
 * Stores access token for the connection
 */
@Entity
@Table(name = "canvas_connection")
public class CanvasConnection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "connection_id")
    private Integer connectionId;

    @ManyToOne(fetch = FetchType.LAZY) // using lazy to not fetch from DB unnecessarily
    @JoinColumn(name = "ooc_id", nullable = false)
    private OocUser oocUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canvas_id", nullable = false)
    private CanvasInstance canvasInstance;

    @Column(name = "access_token", nullable = false)
    private String accessToken;

    public Integer getConnectionId() {
        return connectionId;
    }

    public OocUser getOocUser() {
        return oocUser;
    }

    public void setOocUser(OocUser oocUser) {
        this.oocUser = oocUser;
    }

    public CanvasInstance getCanvasInstance() {
        return canvasInstance;
    }

    public void setCanvasInstance(CanvasInstance canvasInstance) {
        this.canvasInstance = canvasInstance;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
