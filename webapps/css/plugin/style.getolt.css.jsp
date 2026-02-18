<%@ page contentType="text/css; charset=UTF-8"%>

/* Plugin GetOLT */
.getolt-container {
	padding: 10px;
}
.getolt-error {
	color: #dc3545;
	padding: 20px;
	text-align: center;
	font-size: 14px;
}
.getolt-info {
	color: #6c757d;
	padding: 20px;
	text-align: center;
	font-size: 14px;
}
.getolt-onu-card {
	border: 1px solid #ddd;
	border-radius: 4px;
	margin-bottom: 15px;
	background: #fff;
}
.getolt-onu-header {
	background: #f8f9fa;
	padding: 10px 15px;
	border-bottom: 1px solid #ddd;
	display: flex;
	justify-content: space-between;
	align-items: center;
}
.getolt-onu-header-left {
	display: flex;
	flex-direction: column;
	gap: 2px;
}
.getolt-onu-header .getolt-mac {
	font-family: monospace;
	font-weight: bold;
	font-size: 14px;
}
.getolt-onu-header-left .getolt-update-time {
	font-size: 11px;
	color: #6c757d;
}
.getolt-onu-header-right {
	display: flex;
	align-items: center;
	gap: 12px;
}
.getolt-update-time {
	font-size: 11px;
	color: #6c757d;
	cursor: help;
}
.getolt-onu-status {
	padding: 3px 10px;
	border-radius: 12px;
	font-size: 12px;
	font-weight: bold;
}
.getolt-onu-status.getolt-online {
	background: #d4edda;
	color: #155724;
}
.getolt-onu-status.getolt-offline {
	background: #f8d7da;
	color: #721c24;
}
.getolt-onu-body {
	padding: 15px;
}
.getolt-onu-row {
	display: flex;
	flex-wrap: wrap;
	margin: 0 -10px;
}
.getolt-onu-col {
	flex: 0 0 50%;
	padding: 0 10px;
	margin-bottom: 10px;
}
.getolt-onu-col.getolt-full {
	flex: 0 0 100%;
}
.getolt-onu-label {
	font-size: 11px;
	color: #6c757d;
	margin-bottom: 2px;
}
.getolt-onu-value {
	font-size: 13px;
	font-weight: 500;
}
.getolt-signal-good {
	color: #28a745;
}
.getolt-signal-medium {
	color: #e6a000;
}
.getolt-signal-poor {
	color: #dc3545;
}
.getolt-signal-overexposed {
	color: #fd7e14;
}
.getolt-macs-list {
	font-family: monospace;
	font-size: 12px;
	max-height: 100px;
	overflow-y: auto;
	background: #f8f9fa;
	padding: 8px;
	border-radius: 4px;
}
.getolt-macs-list .getolt-mac-item {
	margin-bottom: 2px;
}
.getolt-neighbors-table {
	width: 100%;
	font-size: 12px;
	border-collapse: collapse;
}
.getolt-neighbors-table th,
.getolt-neighbors-table td {
	padding: 6px 8px;
	text-align: left;
	border-bottom: 1px solid #eee;
}
.getolt-neighbors-table th {
	background: #f8f9fa;
	font-weight: 600;
}
.getolt-neighbors-table .getolt-mac {
	font-family: monospace;
}
.getolt-contract-info {
	background: #e7f1ff;
	padding: 8px 12px;
	border-radius: 4px;
	margin-bottom: 15px;
	font-size: 13px;
}
.getolt-contract-info span {
	margin-right: 20px;
}
.getolt-neighbors-table .getolt-target-row {
	background: #e9ecef;
	font-weight: 600;
}
.getolt-neighbors-table .getolt-target-row td {
	border-bottom: 2px solid #6c757d;
}
/* Signal-based row highlighting for neighbors */
.getolt-neighbors-table .getolt-signal-row-poor {
	background: #f8d7da;
}
.getolt-neighbors-table .getolt-signal-row-medium {
	background: #fff3cd;
}
.getolt-neighbors-table .getolt-signal-row-overexposed {
	background: #ffe5d0;
}
/* Refresh button styles */
.getolt-btn-refresh {
	background: #007bff;
	color: #fff;
	border: none;
	padding: 5px 12px;
	border-radius: 4px;
	font-size: 12px;
	cursor: pointer;
	transition: background 0.2s;
}
.getolt-btn-refresh:hover {
	background: #0056b3;
}
.getolt-btn-refresh:disabled {
	background: #6c757d;
	cursor: not-allowed;
}
.getolt-btn-refresh .getolt-btn-port-num {
	background: rgba(255,255,255,0.3);
	padding: 2px 6px;
	border-radius: 3px;
	font-weight: bold;
	margin-left: 4px;
	display: inline-block;
	vertical-align: middle;
	line-height: 1;
}
/* Refresh status bar */
.getolt-refresh-status-bar {
	background: #e7f1ff;
	padding: 10px 15px;
	border-radius: 0 0 4px 4px;
	display: none;
	align-items: center;
	gap: 10px;
	border-top: 1px solid #b8daff;
}
.getolt-refresh-status-bar.getolt-active {
	display: flex;
}
.getolt-refresh-status-bar .getolt-spinner {
	width: 16px;
	height: 16px;
	border: 2px solid #007bff;
	border-top-color: transparent;
	border-radius: 50%;
	animation: getolt-spin 1s linear infinite;
}
@keyframes getolt-spin {
	to { transform: rotate(360deg); }
}
.getolt-refresh-status-bar .getolt-status-text {
	flex: 1;
	font-size: 13px;
	color: #004085;
}
.getolt-refresh-status-bar.getolt-success {
	background: #d4edda;
	border-top-color: #c3e6cb;
}
.getolt-refresh-status-bar.getolt-success .getolt-status-text {
	color: #155724;
}
.getolt-refresh-status-bar.getolt-error {
	background: #f8d7da;
	border-top-color: #f5c6cb;
}
.getolt-refresh-status-bar.getolt-error .getolt-status-text {
	color: #721c24;
}
/* Reboot button styles */
.getolt-btn-reboot {
	background: #dc3545;
	color: #fff;
	border: none;
	padding: 5px 12px;
	border-radius: 4px;
	font-size: 12px;
	cursor: pointer;
	transition: background 0.2s;
}
.getolt-btn-reboot:hover:not(:disabled) {
	background: #c82333;
}
.getolt-btn-reboot:disabled {
	background: #6c757d;
	cursor: not-allowed;
}
.getolt-btn-reboot.getolt-cooldown {
	background: #adb5bd;
	color: #495057;
	min-width: 120px;
}
/* Activation form styles */
.getolt-activation-section {
	background: #f8f9fa;
	border: 1px solid #dee2e6;
	border-radius: 4px;
	padding: 15px;
	margin-bottom: 15px;
}
.getolt-activation-section h4 {
	margin: 0 0 10px 0;
	font-size: 14px;
	font-weight: 600;
	color: #495057;
}
.getolt-activation-form {
	display: flex;
	gap: 10px;
	align-items: flex-end;
	flex-wrap: wrap;
}
.getolt-activation-form .getolt-form-group {
	flex: 1;
	min-width: 200px;
}
.getolt-activation-form label {
	display: block;
	font-size: 12px;
	color: #6c757d;
	margin-bottom: 4px;
}
.getolt-activation-form input[type="text"] {
	width: 100%;
	padding: 8px 12px;
	border: 1px solid #ced4da;
	border-radius: 4px;
	font-family: monospace;
	font-size: 14px;
	text-transform: uppercase;
}
.getolt-activation-form input[type="text"]:focus {
	border-color: #80bdff;
	outline: none;
	box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
}
.getolt-activation-form input[type="text"].getolt-is-invalid {
	border-color: #dc3545;
}
.getolt-btn-activate {
	background: #28a745;
	color: #fff;
	border: none;
	padding: 8px 20px;
	border-radius: 4px;
	font-size: 14px;
	cursor: pointer;
	transition: background 0.2s;
	white-space: nowrap;
}
.getolt-btn-activate:hover:not(:disabled) {
	background: #218838;
}
.getolt-btn-activate:disabled {
	background: #6c757d;
	cursor: not-allowed;
}
.getolt-activation-status {
	width: 100%;
	padding: 10px;
	border-radius: 4px;
	font-size: 13px;
	margin-top: 10px;
	display: none;
}
.getolt-activation-status.getolt-active {
	display: block;
}
.getolt-activation-status.getolt-success {
	background: #d4edda;
	color: #155724;
	border: 1px solid #c3e6cb;
}
.getolt-activation-status.getolt-error {
	background: #f8d7da;
	color: #721c24;
	border: 1px solid #f5c6cb;
}
.getolt-activation-status.getolt-processing {
	background: #e7f1ff;
	color: #004085;
	border: 1px solid #b8daff;
}
/* Service selection modal */
.getolt-service-modal-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0,0,0,0.5);
	display: none;
	justify-content: center;
	align-items: center;
	z-index: 10000;
}
.getolt-service-modal-overlay.getolt-active {
	display: flex;
}
.getolt-service-modal {
	background: #fff;
	border-radius: 8px;
	width: 90%;
	max-width: 500px;
	max-height: 80vh;
	overflow: hidden;
	box-shadow: 0 4px 20px rgba(0,0,0,0.3);
}
.getolt-service-modal-header {
	background: #f8f9fa;
	padding: 15px 20px;
	border-bottom: 1px solid #dee2e6;
	display: flex;
	justify-content: space-between;
	align-items: center;
}
.getolt-service-modal-header h3 {
	margin: 0;
	font-size: 16px;
}
.getolt-service-modal-close {
	background: none;
	border: none;
	font-size: 24px;
	cursor: pointer;
	color: #6c757d;
	line-height: 1;
}
.getolt-service-modal-body {
	padding: 20px;
	max-height: 60vh;
	overflow-y: auto;
}
.getolt-service-list {
	list-style: none;
	padding: 0;
	margin: 0;
}
.getolt-service-list li {
	padding: 12px;
	border: 1px solid #dee2e6;
	border-radius: 4px;
	margin-bottom: 8px;
	cursor: pointer;
	transition: all 0.2s;
}
.getolt-service-list li:hover {
	background: #e9ecef;
	border-color: #adb5bd;
}
.getolt-service-list li:last-child {
	margin-bottom: 0;
}
.getolt-service-list .getolt-service-title {
	font-weight: 500;
	margin-bottom: 4px;
}
.getolt-service-list .getolt-service-mac {
	font-family: monospace;
	font-size: 12px;
	color: #6c757d;
}
