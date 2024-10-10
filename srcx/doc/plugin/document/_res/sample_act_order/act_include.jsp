<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<p>Акт сдачи-приёмки работ № ${process.id} от ${tu.format(curdate, 'ymd')} по договору: ${contractLink.linkObjectTitle}</p>
<p>Заказчик: ${contractInfo.comment}</p>
<p>Адрес: ${paramDao.getParamAddress(processId, PROCESS_PARAM_ADDRESS, 1).value}</p>
<p>Вид работ: ${process.type.title}</p>
<p>Исполнители: ${u.getObjectTitles(ctxUserList, process.getExecutorIds())}</p>

<p>Производимые работы (оказываемые услуги):</p>
<table style="width: 100%;">
	<tr>
		<td>№ п/п</td>
		<td>Описание работы, наименование материала</td>
		<td>Ед. изм.</td>
		<td>Кол-во</td>
		<td>Цена, руб.</td>
		<td>Сумма, руб.</td>
	</tr>
	<tr>
		<td>1</td>
		<td>Настройка приложения "Камеры АТЕЛ"</td>
		<td>-</td>
		<td>-</td>
		<td>-</td>
		<td>&nbsp;</td>
	</tr>
	<c:forEach var="i" begin="1" end="5">
		<tr>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
	</c:forEach>
	<tr>
		<td colspan="5" style="text-align: right;">Итого:</td>
		<td>&nbsp;</td>
	</tr>
 	</table>

<p>Вышеперечисленные услуги выполнены полностью, заказчик претензий по объему, качеству и срокам работ не имеет.</p>
<p>Исполнитель: ________________________________  Заказчик:______________________________</p>
