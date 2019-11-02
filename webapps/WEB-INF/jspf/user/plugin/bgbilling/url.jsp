<%@ page contentType="text/html; charset=UTF-8"%>

// договор
else if ((m = href.match(/.*contract_(\w+)#(\d+)/)) != null) {
	url = "/user/plugin/bgbilling/contract.do?billingId=" + m[1] + "&id=" + m[2];
	bgcolor = "#EEB9CD";
}
// единый договор
else if ((m = href.match(/.*bgbilling\-commonContract#(\d+)/)) != null) {
	url = "/user/plugin/bgbilling/commonContract.do?&id=" + m[1];
	bgcolor = "#FFB469";
}
