<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:bgcrm="http://bgcrm.ru/saxon-extension"
	xmlns:t="http://bgcrm.ru/template" exclude-result-prefixes="bgcrm t"
	version="2.0">

<xsl:template match="/event">  	
<div style="font-family: times; font-size: 14pt; text-align: left; width: 800px;">
	<div>
		<xsl:variable name="process" select="bgcrm:process(@objectId)"/>
		
		<!-- отладочный вывод исходной XML -->
    	<!-- <xsl:copy-of select="$process"/> -->
	
		<!-- извлечение из описания ФИО и контактов 
		ФИО: Тестов Тест Тестович
		Телефон: 33333 3333
		Остаток: -970.00
		-->
		<xsl:variable name="tokens" select="tokenize( substring-before( $process/data/process/@description, '----------' ), ':|\n' )"/>
		
		<div style="font-size: 30pt; text-align: center;">«СОФИТ КТВ»</div>
		<div style="font-size: 14pt; text-align: center;">Общество с ограниченной ответственностью</div>
		<div style="font-size: 24pt; font-weight: bold; text-align: center;">Заявка на отключение кабельного телевидения. № <xsl:value-of select="$process/data/process/@id"/></div>
		<div style="font-size: 17pt; font-weight: bold; text-align: left;">Ф.И.О.: <xsl:value-of select="$tokens[2]"/></div>
		<div>Адрес: <xsl:value-of select="$process/data/parameters/parameter[@id=1]/@value"/>	Код: </div>
		<div>Телефоны: <xsl:value-of select="$tokens[4]"/></div>
		<div>Причина: долг</div>
		<div>Примечания: <xsl:value-of select="substring-after( $process/data/process/@description, '----------' )"/></div>
		<div>Заявка принята:  подпись:	______________________________________</div>
		<div>Заявку выполнил: __"__"____ г.  подпись:	______________________________________</div>
		<div>Мастер: ________________________</div>
	</div>
	
	<div style="page-break-before: always; padding-top: 20px;"> 	
		<div style="font-size: 23pt; text-align: center;">ИЗВЕЩЕНИЕ по договору №</div><br/>
		<div style="text-align: center;">____<xsl:value-of select="/data/task/@name"/>_______</div><br/>
		<div style="font-weight: bold; text-align: left;">ООО «СОФИТ КТВ» извещает Вас о 
		приостановлении доступа к сети связи (кабельное телевидение) с «____»____________________2014г. В связи с возникшей задолженностью в размере _____________руб. на «_____»___________________2014г.</div>
		<div>Для возобновления доступа к сети связи (кабельное телевидение) Вам необходимо погасить задолженность.</div>
		<div>Для оперативного подключения оплату следует произвести в Абонентском отделе ООО «СОФИТ КТВ», расположенных по адресам: г. Магадан, ул. Новая, д. 31/10 (бывшая «Рембыттехника») 1 этаж, г. Магадан, ул. Пролетарская, д. 66 (супермаркет «Идея») 1 этаж.</div>
		<div>Подключение производится в следующие сроки:</div>
		<div>*Понедельник-Пятница - при оплате  в абонентском отделе  до 12.00 часов подключение производится  текущим днем, при оплате после 12.00 часов подключение производится  на следующий день.</div><br/>
		<div>*Суббота, Воскресенье – при оплате подключение производится в течении суток.</div><br/>
		<div>*При оплате через банк, отделения связи, терминалы Вам необходимо сообщить  о произведенной оплате в Абонентский отдел по тел. 622-081, 601-222, 608-880.</div><br/>
		
		<div style="font-weight: bold; text-align: left;">Часы работы Абонентского отдела:</div>
		<table style="font-weight: bold; vertical-align: top;">
			<tr>
				<td width="50%">ул.Новая, д.31/10</td>
				<td width="50%">Понедельник - пятница 10.00 - 19.00<br/>Суббота, воскресенье 11.00 - 17.00</td>
			</tr>
			<tr>
				<td>ул.Пролетарская, д.66</td>
				<td>Понедельник - пятница 10.00 - 19.00<br/>Пятница, суббота 12.00 - 20.00<br/>Воскресенье 12.00 - 17.00</td>
			</tr>
		</table>									
		<div>В случае неоплаты задолженности в течение шести месяцев ООО «СОФИТ КТВ», согласно п. 3.1.5 Договора об оказании услуг кабельного телевидения, вправе расторгнут с Вами договор в одностороннем порядке.</div>
		<div>При непогашении образовавшейся задолженности, будем вынуждены обратиться за взысканием долга в суд.</div>
		<div>Дополнительно сообщаем, что в случае взыскания долга в судебном порядке:</div>
		<div>- с вас будет взыскана неустойка в размере 1% стоимости неоплаченных, оплаченных не в полном объеме или несвоевременно оплаченных услуг связи за каждый день просрочки вплоть до дня погашения задолженности, </div>
		<div>- на вас дополнительно лягут расходы по оплате государственной пошлины, </div>
		<div>- расходы на представителя в судебном процессе, </div>
		<div>- почтовые расходы, которые понесет общество, соблюдая досудебный порядок рассмотрения споров.</div>
		<div style="font-size: 16pt; font-weight: bold; text-align: left;">Обращаем Ваше внимание, что сумма этих расходов может значительно превышать сумму вашего долга.</div>
		<div style="font-size: 14pt; font-weight: bold; text-align: center;">Для сведения сообщаем:</div><br/>
		<div style="font-size: 14pt; font-weight: bold; text-align: center;">За самовольное подключение к сети связи предусмотрена ответственность:</div>
		<div style="font-size: 14pt; font-weight: bold; text-align: center;">Административная	1. КоАП РФ</div><br/>
		<div>«Статья 13.2 Самовольное подключение к сети электрической связи оконечного оборудования»
		Подключение без специального разрешения к сети электрической связи оконечного оборудования влечет предупреждение или наложение административного штрафа на граждан в размере от трех до пяти минимальных размеров оплаты труда с конфискацией оконечного оборудования или без таковой; на должностных лиц от пяти до десяти минимальных размеров оплаты труда с конфискацией оконечного оборудования или без таковой; на юридических лиц от пятидесяти до ста минимальных размеров оплаты труда с конфискацией оконечного оборудования или без таковой.</div>
		<div style="font-size: 14pt; font-weight: bold; text-align: center;">Уголовная 2. УК РФ</div>
		<div style="font-size: 14pt;">«Статья 165. Причинение имущественного ущерба путем обмана или злоупотребления доверием»
		Причинение имущественного ущерба собственнику или иному владельцу имущества путем обмана или злоупотребления доверием при отсутствии признаков хищения наказывается штрафом в размере до восьмидесяти тысяч рублей или в размере заработной платы или иного дохода осужденного за период до двух месяцев, либо обязательными работами на срок от ста двадцати до ста восьмидесяти часов, либо исправительными работами на срок до одного года, либо арестом на срок до четырех месяцев, либо лишением свободы на срок до двух лет.</div>
	</div>
</div>
</xsl:template>
</xsl:transform>