package ru.bgcrm.plugin.bgbilling.proto.model.limit;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.bgcrm.model.Id;

public class LimitChangeTask
		extends Id {
	private Date date;
	private String user;
	private BigDecimal limitChange;

	static Map<String,String> statusMap = new HashMap<>();
	static {
		statusMap.put("", "Все" );
		statusMap.put( "on", "Активная" );
		statusMap.put( "off", "Выполненая" );
		statusMap.put( "cancel", "Отмененная" );
	}

	public String getStatusString() {
		return statusMap.get(status);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	private String status;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@JsonProperty("userName")
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@JsonProperty("value")
	public BigDecimal getLimitChange() {
		return limitChange;
	}

	public void setLimitChange(BigDecimal limitChange) {
		this.limitChange = limitChange;
	}
}
