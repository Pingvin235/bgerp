<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div class="getolt-container" id="${uiid}">
	<%-- Error message --%>
	<c:if test="${not empty frd.error}">
		<div class="getolt-error">${frd.error}</div>
	</c:if>

	<%-- Search result --%>
	<c:if test="${not empty frd.searchResult}">
		<%-- Contract info header --%>
		<div class="getolt-contract-info">
			<span><strong>Договор:</strong> ${frd.contractNumber}</span>
			<span><strong>CID:</strong> ${frd.cid}</span>
			<span><strong>Оператор:</strong> ${frd.operator}</span>
		</div>

		<%-- Activation form (requires permission) --%>
		<c:if test="${ctxUser.checkPerm('/user/plugin/getolt/getolt:activateOnu')}">
			<div class="getolt-activation-section">
				<h4>Активация / Замена ONU</h4>
				<div class="getolt-activation-form">
					<div class="getolt-form-group">
						<label>MAC-адрес новой ONU</label>
						<input type="text" class="getolt-new-mac-input"
								placeholder="XX:XX:XX:XX:XX:XX"
								maxlength="17"
								data-process-id="${form.param.processId}"
								onkeyup="$$.getolt.formatMacInput(this)"
								onpaste="$$.getolt.handleMacPaste(event, this)">
					</div>
					<button type="button" class="getolt-btn-activate"
							onclick="$$.getolt.activateOnu(this)">
						Активировать
					</button>
				</div>
				<div class="getolt-activation-status"></div>
			</div>

			<%-- Service selection modal --%>
			<div class="getolt-service-modal-overlay">
				<div class="getolt-service-modal">
					<div class="getolt-service-modal-header">
						<h3>Выберите услугу</h3>
						<button type="button" class="getolt-service-modal-close" onclick="$$.getolt.closeServiceModal(this)">&times;</button>
					</div>
					<div class="getolt-service-modal-body">
						<p>У договора несколько интернет-услуг. Выберите услугу для замены MAC:</p>
						<ul class="getolt-service-list"></ul>
					</div>
				</div>
			</div>
		</c:if>

		<c:choose>
			<c:when test="${frd.searchResult.success and frd.searchResult.hasOnus()}">
				<%-- Loop through ONUs --%>
				<c:forEach var="onu" items="${frd.searchResult.onus}">
					<div class="getolt-onu-card">
						<div class="getolt-onu-header">
							<div class="getolt-onu-header-left">
								<span class="getolt-mac">${onu.mac}</span>
								<c:if test="${not empty onu.lastUpdate}">
									<span class="getolt-update-time" title="${onu.lastUpdateFormatted}">
										Обновлено: ${onu.lastUpdateAgo}
									</span>
								</c:if>
							</div>
							<div class="getolt-onu-header-right">
								<span class="getolt-onu-status ${onu.online ? 'getolt-online' : 'getolt-offline'}">
									${onu.online ? 'ONLINE' : 'OFFLINE'}
								</span>
								<c:if test="${ctxUser.checkPerm('/user/plugin/getolt/getolt:refreshPort')}">
									<c:if test="${not empty onu.oltIp and onu.port > 0}">
										<c:set var="refreshKey" value="${onu.oltIp.replace('.', '_')}_${onu.port}" />
										<button type="button" class="getolt-btn-refresh"
												data-olt-ip="${onu.oltIp}"
												data-port="${onu.port}"
												data-process-id="${form.param.processId}"
												data-refresh-key="${refreshKey}"
												onclick="$$.getolt.startPortRefresh(this)">
											Обновить порт <span class="getolt-btn-port-num">${onu.port}</span>
										</button>
									</c:if>
								</c:if>
								<c:if test="${ctxUser.checkPerm('/user/plugin/getolt/getolt:rebootOnu')}">
									<c:if test="${not empty onu.oltId and onu.port > 0 and onu.onuId > 0}">
										<c:set var="rebootKey" value="${onu.oltId}_${onu.port}_${onu.onuId}" />
										<button type="button" class="getolt-btn-reboot"
												data-olt-id="${onu.oltId}"
												data-port="${onu.port}"
												data-onu-id="${onu.onuId}"
												data-mac="${onu.mac}"
												data-process-id="${form.param.processId}"
												data-reboot-key="${rebootKey}"
												onclick="$$.getolt.startOnuReboot(this)">
											Перезагрузить ONU
										</button>
									</c:if>
								</c:if>
							</div>
						</div>
						<c:if test="${not empty onu.oltIp and onu.port > 0}">
							<c:set var="refreshKey" value="${onu.oltIp.replace('.', '_')}_${onu.port}" />
							<div class="getolt-refresh-status-bar" data-refresh-key="${refreshKey}" style="display:none;">
								<div class="getolt-spinner"></div>
								<span class="getolt-status-text">Обновление порта...</span>
							</div>
						</c:if>
						<div class="getolt-onu-body">
							<div class="getolt-onu-row">
								<div class="getolt-onu-col">
									<div class="getolt-onu-label">OLT IP</div>
									<div class="getolt-onu-value">${onu.oltIp}</div>
								</div>
								<div class="getolt-onu-col">
									<div class="getolt-onu-label">Порт / ONU ID</div>
									<div class="getolt-onu-value">${onu.port} / ${onu.onuId}</div>
								</div>
							</div>

							<div class="getolt-onu-row">
								<div class="getolt-onu-col">
									<div class="getolt-onu-label">Сигнал RX</div>
									<div class="getolt-onu-value">
										<c:if test="${not empty onu.rxSignal}">
											<span class="${onu.getQualityClass(onu.rxQuality)}">
												${onu.rxSignal} dBm
											</span>
										</c:if>
										<c:if test="${empty onu.rxSignal}">-</c:if>
									</div>
								</div>
								<div class="getolt-onu-col">
									<div class="getolt-onu-label">Сигнал TX</div>
									<div class="getolt-onu-value">
										<c:if test="${not empty onu.txSignal}">
											<span class="${onu.getQualityClass(onu.txQuality)}">
												${onu.txSignal} dBm
											</span>
										</c:if>
										<c:if test="${empty onu.txSignal}">-</c:if>
									</div>
								</div>
							</div>

							<div class="getolt-onu-row">
								<div class="getolt-onu-col">
									<div class="getolt-onu-label">Дистанция</div>
									<div class="getolt-onu-value">
										<c:if test="${not empty onu.distance}">${onu.distance} м</c:if>
										<c:if test="${empty onu.distance}">-</c:if>
									</div>
								</div>
								<div class="getolt-onu-col">
									<div class="getolt-onu-label">Температура</div>
									<div class="getolt-onu-value">
										<c:if test="${not empty onu.temperature}">${onu.temperature} °C</c:if>
										<c:if test="${empty onu.temperature}">-</c:if>
									</div>
								</div>
							</div>

							<div class="getolt-onu-row">
								<div class="getolt-onu-col">
									<div class="getolt-onu-label">Напряжение</div>
									<div class="getolt-onu-value">
										<c:if test="${not empty onu.voltage}">${onu.voltage} V</c:if>
										<c:if test="${empty onu.voltage}">-</c:if>
									</div>
								</div>
								<div class="getolt-onu-col">
									<div class="getolt-onu-label">Статус</div>
									<div class="getolt-onu-value">${onu.status}</div>
								</div>
							</div>

							<%-- MACs behind ONU --%>
							<c:if test="${not empty onu.macsBehind}">
								<div class="getolt-onu-row">
									<div class="getolt-onu-col getolt-full">
										<div class="getolt-onu-label">MAC за ONU (${onu.macsBehind.size()})</div>
										<div class="getolt-macs-list">
											<c:forEach var="mac" items="${onu.macsBehind}">
												<div class="getolt-mac-item">${mac}</div>
											</c:forEach>
										</div>
									</div>
								</div>
							</c:if>

							<%-- Port neighbors --%>
							<c:if test="${not empty onu.neighbors}">
								<div class="getolt-onu-row">
									<div class="getolt-onu-col getolt-full">
										<div class="getolt-onu-label">Соседи по порту</div>
										<table class="getolt-neighbors-table">
											<thead>
												<tr>
													<th>MAC</th>
													<th>ONU ID</th>
													<th>RX (dBm)</th>
													<th>Статус</th>
													<th>Договор</th>
													<th>Обновлено</th>
												</tr>
											</thead>
											<tbody>
												<c:forEach var="neighbor" items="${onu.neighbors}">
													<tr class="${neighbor.contractNumber eq frd.contractNumber ? 'getolt-target-row' : neighbor.rowClass}">
														<td class="getolt-mac">${neighbor.mac}</td>
														<td>${neighbor.onuId}</td>
														<td><c:if test="${not empty neighbor.rxOptical}">${neighbor.rxOptical}</c:if><c:if test="${empty neighbor.rxOptical}">-</c:if></td>
														<td>${neighbor.status}</td>
														<td>${neighbor.contractNumber}</td>
														<td class="getolt-update-time" title="${neighbor.lastUpdateFormatted}"><c:if test="${not empty neighbor.lastUpdateAgo}">${neighbor.lastUpdateAgo}</c:if><c:if test="${empty neighbor.lastUpdateAgo}">-</c:if></td>
													</tr>
												</c:forEach>
											</tbody>
										</table>
									</div>
								</div>
							</c:if>
						</div>
					</div>
				</c:forEach>
			</c:when>
			<c:otherwise>
				<div class="getolt-info">${frd.searchResult.errorMessage}</div>
			</c:otherwise>
		</c:choose>
	</c:if>
</div>

<script>
$$.getolt.init('${uiid}');
</script>
