package ru.bgcrm.plugin.bgbilling;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ru.bgcrm.model.Page;

public class Request {
	private Map<String, Object> data = new HashMap<>();

	public Request() {
	}

	public void setModule(String value) {
		if (value != null) {
			data.put("module", value);
		}
	}

	public void setAction(String value) {
		if (value != null) {
			data.put("action", value);
		}
	}

	public void setModuleID(String value) {
		if (value != null) {
			data.put("mid", value);
		}
	}

	public void setModuleID(int value) {
		data.put("mid", String.valueOf(value));
	}

	public void setContractId(int value) {
		data.put("cid", String.valueOf(value));
	}

	public void setContractId(String value) {
		data.put("cid", value);
	}

	public void setPage(Page page) {
		if (page != null) {
			setPageIndex(page.getPageIndex());
			setPageSize(page.getPageSize());
		}
	}

	public void setPageIndex(int pageIndex) {
		if (pageIndex >= 0) {
			setAttribute("pageIndex", pageIndex);
			setAttribute("page", pageIndex);
		}
	}

	public void setPageSize(int pageSize) {
		if (pageSize >= 0) {
			setAttribute("pageSize", pageSize);
		}
	}

	public void setPeriod(String date1, String date2) {
		if (date1 != null) {
			setAttribute("date1", date1);
		}
		if (date2 != null) {
			setAttribute("date2", date2);
		}
	}

	public void setAttribute(String name, Object value) {
		if (name != null && value != null) {
			data.put(name, value);
		}
	}

	public void setAttribute(String name, long value) {
		if (name != null) {
			data.put(name, String.valueOf(value));
		}
	}

	public void setAttribute(String name, int value) {
		if (name != null) {
			data.put(name, String.valueOf(value));
		}
	}

	public void setAttribute(String name, boolean value) {
		if (name != null) {
			data.put(name, value ? "1" : "0");
		}
	}

	public void clear() {
		data.clear();
	}

	public Object getValue(String key) {
		return key != null ? data.get(key) : null;
	}

	public Set<String> keys() {
		return data.keySet();
	}

	/* public String getVersion()
	 {
	 	return version;
	 }*/

	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (String key : keys()) {
			builder.append(key);
			builder.append("=");
			builder.append(getValue(key));
			builder.append("; ");
		}
		return builder.toString();
	}
}
