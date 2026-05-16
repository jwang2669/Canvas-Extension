package com.oiloncanvas.backend.entity;
import java.util.List;
import jakarta.persistence.*;

/**
 * Represents a unique canvas instance and its base URL
 */
@Entity
@Table(name = "canvas_instance")
public class CanvasInstance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "canvas_id")
    private Integer canvasId;

    @Column(name = "base_url", nullable = false)
    private String baseURL;

    @OneToMany(mappedBy = "canvasInstance", fetch = FetchType.LAZY)
    private List<CanvasConnection> canvasConnections;

    public Integer getCanvasId() {
        return canvasId;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public List<CanvasConnection> getCanvasConnections() {
        return canvasConnections;
    }
    
    public void setCanvasConnections(List<CanvasConnection> canvasConnections) {
        this.canvasConnections = canvasConnections;
    }
}
