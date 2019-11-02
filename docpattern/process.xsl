<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:bgcrm="http://bgcrm.ru/saxon-extension"
	xmlns:t="http://bgcrm.ru/template" exclude-result-prefixes="bgcrm t"
	version="2.0">

	<xsl:variable name="months">
		<month num="01" name="Jan" />
		<month num="02" name="Feb" />
		<month num="03" name="Mar" />
		<month num="04" name="Apr" />
		<month num="05" name="May" />
		<month num="06" name="Jun" />
		<month num="07" name="Jul" />
		<month num="08" name="Aug" />
		<month num="09" name="Sep" />
		<month num="10" name="Oct" />
		<month num="11" name="Nov" />
		<month num="12" name="Dec" />
	</xsl:variable>

	<xsl:template match="/event">
		<data>
			<xsl:variable name="process" select="bgcrm:process(@objectId)" />
			<xsl:copy-of select="$process"/>
			
			<field name="cardNumber">
				<xsl:value-of select="$process/data/parameters/parameter[@id=191]/@value" />
			</field>
			
			<xsl:variable name="element" select="$process/data/parameters/parameter[@id=88]/@value" />
			<field name="element">
					<xsl:value-of select="replace(  $element, '\n' , '&lt;/w:t>&lt;w:br/>&lt;w:t>' )" />
			</field>
			
			<field name="city">
				<xsl:value-of select="$process/data/parameters/parameter[@id=87]/@value" />
			</field>
			
			<xsl:variable name="workPurpose" select="$process/data/parameters/parameter[@id=81]/@value" />
			<field name="workPurpose">
					<xsl:value-of select="replace(  $workPurpose, '\n' , '&lt;/w:t>&lt;w:br/>&lt;w:t>' )" />
			</field>
			
			<xsl:variable name="workList" select="$process/data/parameters/parameter[@id=79]/@value" />
			<field name="workList">
					<xsl:value-of select="replace(  $workList, '\n' , '&lt;/w:t>&lt;w:br/>&lt;w:t>' )" />
			</field>
											
			<xsl:variable name="dateWorkbeginning" select="$process/data/parameters/parameter[@id=58]/@value" />		
		 	<xsl:variable name="day" select="tokenize($dateWorkbeginning, '\s')[3]"/>
			<xsl:variable name="month" select="$months/month[@name=tokenize($dateWorkbeginning, '\s')[2]]/@num"/>
			<xsl:variable name="year" select="tokenize($dateWorkbeginning, '\s')[6]"/>
			<xsl:variable name="time" select="tokenize($dateWorkbeginning, '\s')[4]"/>
			<field name="workbeginning">
				<xsl:value-of select="concat ($day,'.',$month,'.',$year, ' ', $time)" />
			</field>	 

			<xsl:variable name="dateWorkcompletion" select="$process/data/parameters/parameter[@id=59]/@value" />		
			<xsl:variable name="day" select="tokenize($dateWorkcompletion, '\s')[3]"/>
			<xsl:variable name="month" select="$months/month[@name=tokenize($dateWorkcompletion, '\s')[2]]/@num"/>
			<xsl:variable name="year" select="tokenize($dateWorkcompletion, '\s')[6]"/>
			<xsl:variable name="time" select="tokenize($dateWorkcompletion, '\s')[4]"/>
			<field name="workcompletion">
				<xsl:value-of select="concat ($day,'.',$month,'.',$year, ' ', $time)" />
			</field>
			
			<xsl:variable name="timeabsence" select="$process/data/parameters/parameter[@id=266]/@value" />
			<field name="timeabsence">
					<xsl:value-of select="replace(  $timeabsence, '\n' , '&lt;/w:t>&lt;w:br/>&lt;w:t>' )" />
			</field>
			
			<field name="legal">
				<xsl:value-of select="$process/data/parameters/parameter[@id=121]/@value" />
			</field>
			
			<field name="category">
				<xsl:value-of select="$process/data/parameters/parameter[@id=68]/@value" />
			</field>
			
			<field name="phisical">
				<xsl:value-of select="$process/data/parameters/parameter[@id=122]/@value" />
			</field>
			
			<xsl:variable name="timeequipment" select="$process/data/parameters/parameter[@id=100]/@value" />
			<field name="timeequipment">
					<xsl:value-of select="replace(  $timeequipment, '\n' , '&lt;/w:t>&lt;w:br/>&lt;w:t>' )" />
			</field>
			
			<field name="switchamount">
				<xsl:value-of select="$process/data/parameters/parameter[@id=966]/@value" />
			</field>
			
			<xsl:variable name="clients" select="$process/data/parameters/parameter[@id=265]/@value" />
			<field name="listOfClients">
					<xsl:value-of select="replace(  $clients, ', ' , '&lt;/w:t>&lt;w:br/>&lt;w:t>&lt;/w:t>&lt;w:br/>&lt;w:t>' )" />
			</field>

			<xsl:variable name="performer" select="$process/data/parameters/parameter[@id=102]/@value" />
			<field name="performer">
					<xsl:value-of select="replace(  $performer, '\n' , '&lt;/w:t>&lt;w:br/>&lt;w:t>' )" />
			</field>
			
			<field name="executorComments">
				<xsl:value-of select="$process/data/parameters/parameter[@id=904]/@value" />
			</field>
			
			<field name="liable">
				<xsl:value-of select="$process/data/parameters/parameter[@id=183]/@value" />
			</field>
			
			<field name="checkingAlgorithm">
				<xsl:value-of select="$process/data/parameters/parameter[@id=746]/@value" />
			</field>
			
			<field name="verifyingNetwork">
				<xsl:value-of select="$process/data/parameters/parameter[@id=103]/@value" />
			</field>
			
			<field name="notification">
				<xsl:value-of select="$process/data/parameters/parameter[@id=104]/@value" />
			</field>
			
			<field name="service">
				<xsl:value-of select="$process/data/parameters/parameter[@id=986]/@value" />
			</field>
			
			<xsl:variable name="finalizers" select="$process/data/parameters/parameter[@id=263]/@value" />
			<!-- DocX -->
			<field name="listOfFinalizers">
					<xsl:value-of select="replace(  $finalizers, ', ' , '&lt;/w:t>&lt;w:br/>&lt;w:t>&lt;/w:t>&lt;w:br/>&lt;w:t>' )" />
			</field>
		</data>
	</xsl:template>
</xsl:transform>