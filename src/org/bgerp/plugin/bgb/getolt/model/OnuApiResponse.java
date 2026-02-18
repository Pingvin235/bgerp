package org.bgerp.plugin.bgb.getolt.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for GetOLT API response.
 * Handles the new API format: {data: {target: OnuData, portNeighbors: [...]}}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnuApiResponse {

    @JsonProperty("data")
    private OnuApiData data;

    public OnuApiData getData() {
        return data;
    }

    public void setData(OnuApiData data) {
        this.data = data;
    }

    /**
     * Inner data structure with target ONU and port neighbors.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OnuApiData {

        @JsonProperty("target")
        private OnuData target;

        @JsonProperty("portNeighbors")
        private List<OnuData.PortNeighbor> portNeighbors;

        public OnuData getTarget() {
            return target;
        }

        public void setTarget(OnuData target) {
            this.target = target;
        }

        public List<OnuData.PortNeighbor> getPortNeighbors() {
            return portNeighbors;
        }

        public void setPortNeighbors(List<OnuData.PortNeighbor> portNeighbors) {
            this.portNeighbors = portNeighbors;
        }
    }
}
